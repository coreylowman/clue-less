var tag = "";
var isMyTurn = true;
var canSuggest = true;

function establishWebsocket() {
    // if user is running mozilla then use it's built-in WebSocket
    window.WebSocket = window.WebSocket || window.MozWebSocket;

  //establish connection at localhost:3000
    var connection = new WebSocket('ws://127.0.0.1:3000');

  //takes care of any initial action upon opening of websocket
    connection.onopen = function () {
      console.log('opened');
      //webpage will update status from Connecting to Connected
      document.getElementById('status').innerHTML = 'Connected';
      // connection is opened and ready to use

      connection.send(JSON.stringify({eventType:'TEST'}));

      // send JOIN_REQUEST
      tag = prompt("Please enter your name");
      var joinRequest = {eventType: "JOIN_REQUEST", playerTag: tag };
      connection.send(JSON.stringify(joinRequest));
    };

    connection.onerror = function (error) {
      console.log('websocket messed up');
        // an error occurred when sending/receiving data
    };

    connection.onmessage = function(message){
      console.log(message);
      handleEvent(JSON.parse(message.data));
    };

  return connection;
}

var websocket = establishWebsocket();

// moves character HTML element into destination HTML element
// refer to style.css for names
// eg
// moveTo('col_mustard', 'study');

function moveTo(character, destination){
  document.getElementById(destination).appendChild(document.getElementById(character));
}

// generates an id specific function for each element
// presently console logs name of element
// TODO send id to websocket

function genElementIdFunc(elementId){
  var elementIdFunc = function(event){
    console.log(elementId)
  }
  return elementIdFunc;
};

// add id-specific onclick functionality to HTML elements by classname

function addIdSpecificOnclickByClass(classname) {
  var elements = document.getElementsByClassName(classname);
  for(var i = 0; i < elements.length; i++){
    var elementIdFunc = genElementIdFunc(elements[i].id);
    elements[i].addEventListener("click", elementIdFunc);
  }
};



function init(){
  addIdSpecificOnclickByClass("room");
  addIdSpecificOnclickByClass("horizontal_hallway");
  addIdSpecificOnclickByClass("vert_hallway");
  addCard("Mrs. Peacock");
}

init();

function sendChat(){
  var chatEvent = {eventType: "CHAT_NOTIFICATION", body: ""};
  var chatInput = document.getElementById("chat_input");
  chatEvent.body = chatInput.value;
  chatInput.value = "";
  //can't send empty messages
  if (chatEvent.body !== ""){
    websocket.send(JSON.stringify(chatEvent));
  }
}

function sendToChatBox(message){
  document.getElementById("chat_text").value += message + '\n';
}

function suggestionChat(suggestion){
  var suggestionChat = "Game: " +
    suggestion.suggester + " suggests that it was " +
    suggestion.accused + " with the " + suggestion.weapon + " in the " +
    suggestion.room + ".";
  sendToChatBox(suggestionChat);
}


function highlightCard(cardName){
  var card = document.getElementById(cardName + "_card");
  if(card !== null){
    card.style.borderColor = "yellow";
    card.onclick =  sendCardName(cardName);
  }
}

function provideEvidenceNotification(evidence){
  alert("Please provide your evidence!");
  isEvidenceSelectionTime = true;
  highlightCard(evidence.suspect);
  highlightCard(evidence.room);
  highlightCard(evidence.weapon);

}

function handleEvent(event){
  switch(event.eventType){
    case "TEST":
      console.log("this is only a test")
      break;
    case "CHAT_NOTIFICATION":
      sendToChatBox(event.author + ': ' + event.body);
      break;
    case "JOIN_NOTIFICATION":
    sendToChatBox(event.playerTag + " (" + event.playerSuspect + ") has joined!");
    break;
    case "INVALID_REQUEST_NOTIFICATION":
    alert("You cannot do that. " + event.reason);
    break;
    case "PROVIDE_EVIDENCE_NOTIFICATION":
    provideEvidenceNotification(event);
    alert("Please provide your evidence!");
    isEvidenceSelectionTime = true;
    break;
    case "SUGGESTION_NOTIFICATION":
    suggestionChat(event);
    console.log("suggestion");
    break;
    case "TURN_NOTIFICATION":
    handleTurnNotification(event);
    break;
    default:
    console.log("Invalid eventType received");
    break;
  }
}

// adds on click event listeners to all elements passed in
// when an element is clicked, it sends a move request to the server,
// and then removed all the event listeners that were added
// TODO name this better?
function addMoveRequestOnClickTo(elementIds) {
  // removes all the on click events that are added
  // this function is specific to addMoveRequestOnClickTo(), which is why its an
  // inner function
  function removeOnClickFrom(elementIds, func) {
    for (var i = 0;i < elementIds.length;i++) {
      var ele = document.getElementById(elementIds[i]);
      ele.removeEventListener("click", func);
      ele.className = ele.className.replace(" selectable", "");
    }
  }

  for (var i = 0;i < elementIds.length;i++) {
    var ele = document.getElementById(elementIds[i]);

    // the event listener when element is clicked - send MOVE_REQUEST, and then remove on click from
    // ALL elements passed into this function
    function onClick(event) {
      websocket.send(JSON.stringify({eventType: "MOVE_REQUEST", location: elementIds[i]}));
      removeOnClickFrom(elementIds, onClick);
    }

    ele.addEventListener("click", onClick);
    ele.className += " selectable";
  }
}

// handle a turn notification from the server
// if its our turn then display valid moves & let the player suggest/accuse/end turn
function handleTurnNotification(notification) {
  sendToChatBox("It is " + notification.playerTag + "'s turn.");

  if (notification.playerTag === tag) {
    // make valid locations clickable and enable all turn buttons
    addMoveRequestOnClickTo(notification.validMoves);
    document.getElementById("suggest_button").disabled = false;
    document.getElementById("accuse_button").disabled = false;
    document.getElementById("end_turn_button").disabled = false;
  }
}

// called when End Turn button is clicked
function endTurn() {
  websocket.send(JSON.stringify({eventType: "END_TURN_REQUEST"}));
  document.getElementById("suggest_button").disabled = true;
  document.getElementById("accuse_button").disabled = true;
  document.getElementById("end_turn_button").disabled = true;
}

function notPlayerTurn(){
  var event = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "It's not your turn."};
  handleEvent(event);
}

function alreadyDidThat(){
       var alreadyDidThat = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "You already did that this turn!"}
       handleEvent(alreadyDidThat);
}

function suggest(){
  if (isMyTurn){
    if(canSuggest){
      var suggestion = {eventType: "SUGGESTION_REQUEST", suspect: "", weapon: ""};
      var suggestFormElements = document.getElementById("suggest_form").elements;
      suggestion.suspect = suggestFormElements[0].value;
      suggestion.weapon = suggestFormElements[1].value;
      websocket.send(JSON.stringify(suggestion));
      canSuggest = false;
     }else{
       alreadyDidThat();
     }
  }else{
    notPlayerTurn();
  }
};


// remove event listener and change border color back
function resetCards(){
  var cards = document.getElementsByClassName("card");
  for(var i = 0; i < cards.length; i++){
    cards[i].style.borderColor = "red";
    cards[i].onclick = function() {
      return false;
    }
  }
}

function sendCardName(cardName){
  return function(){
    var evidenceRequest = {eventType: "PROVIDE_EVIDENCE_REQUEST", evidence: cardName};
    websocket.send(JSON.stringify(evidenceRequest));
    resetCards();
  }

};

function addCard(cardName){
  var hand = document.getElementById("hand");
  var card = document.createElement("card");
  card.innerHTML += cardName;
  card.className += "card";
  card.id = cardName + "_card";

  hand.appendChild(card);
}
