var tag = "";
var isMyTurn = true;
var canSuggest = false;
var canEndTurn = false;

function establishWebsocket() {
    // if user is running mozilla then use it's built-in WebSocket
    window.WebSocket = window.WebSocket || window.MozWebSocket;

  //establish connection at localhost:3000
    var connection = new WebSocket('ws://127.0.0.1:3000');

  //takes care of any initial action upon opening of websocket
    connection.onopen = function () {
      console.log('connected');
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

// if a room is passed in, nothing changes
// if a hallway is passed in and the names are reversed (e.g. Hallway_Study instead
// of Study_Hallway, then it reverses them)
function getLocationName(location) {
  if (location.includes("_")) {
    if (document.getElementById(location) == null) {
      var pair = location.split("_");
      location = pair[1] + "_" + pair[0];
    }
  }
  return location;
}

// moves character HTML element into destination HTML element
// refer to style.css for names
// eg
// moveTo('col_mustard', 'study');
function moveTo(character, destination){
  destination = getLocationName(destination);
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

function showHand(handEvent) {
  var handChat = handEvent.author + ': Your hand contains - [' + handEvent.cards.toString() + ']';
  sendToChatBox(handChat);

  document.getElementById("hand").innerHTML = "";
  for (var i = 0; i < handEvent.cards.length; i++) {
    addCard(handEvent.cards[i]);
  }
}

function highlightCard(cardName){
  var card = document.getElementById(cardName + "_card");
  if(card !== null){
    card.style.borderColor = "yellow";
    card.onclick =  sendCardName(cardName);
  }
}

function handleEvidenceProvided(evidence){
  alert(evidence.author + " says that the crime did not involve " + evidence.evidence + ".");
};

function handleAllowTurnEnd(){
  canEndTurn = true;
};

function handlePreventTurnEnd(){
  canEndTurn = false;
};

function handleAllowSuggestion(){
  canSuggest = true;
};

function handlePreventSuggestion(){
  canSuggest = false;
};



function provideEvidenceNotification(evidence){
  alert("Please provide your evidence!");
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
      console.log("provide evidence");
      provideEvidenceNotification(event);
      break;
    case "SUGGESTION_NOTIFICATION":
    	suggestionChat(event);
    	console.log("suggestion");
    break;
    case "GAME_START_NOTIFICATION":
      sendToChatBox(event.author + ': Get a Clue!! The game is starting...NOW!')
      break;
    case "HAND_NOTIFICATION":
      showHand(event);
      break;
    case "TURN_NOTIFICATION":
      handleTurnNotification(event);
      break;
    case "EVIDENCE_PROVIDED_NOTIFICATION":
      handleEvidenceProvided(event);
      break;
    case "MOVE_NOTIFICATION":
      handleMoveNotification(event);
      break;
    case "ALLOW_TURN_END":
      handleAllowTurnEnd();
      break;
    case "PREVENT_TURN_END":
      handlePreventTurnEnd();
    case "ALLOW_SUGGEST":
      handleAllowSuggestion();
      break;
    case "PREVENT_SUGGEST":
      handlePreventSuggestion();
    break;
    case "ACCUSATION_NOTIFICATION":
      handleAcccusation(event);
      break;
    case "SECRET_CARD_NOTIFICATION":
      handleSecretCard(event);
      break;
    case "ACCUSATION_OUTCOME_NOTIFICATION":
      handleAccusationOutcome(event);
      break;
    default:
      console.log("Invalid eventType received");
      break;
  }
}

function handleAcccusation(accusation){
 sendToChatBox(accusation.accuser +
              " has accused " +
              accusation.accused +
              " in the " +
              accusation.room +
              " with the " +
              accusation.weapon + ".");
};

function handleAccusationOutcome(outcome){
  if(outcome.outcome === "true"){
    alert(outcome.accuser + " has won the game!");
  }else{
    alert(outcome.accuser + " has lost.");
  }
}

function handleSecretCard(secretCard){
  alert("It was in fact, " +
        secretCard.accused +
        " in the " +
        secretCard.room +
        " with the " +
        secretCard.weapon + ".");
}

function handleMoveNotification(notification) {
  sendToChatBox(notification.suspect + " moved to " + notification.location);

  moveTo(notification.suspect, notification.location);
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
    let name = String(elementIds[i]);

    // the event listener when element is clicked - send MOVE_REQUEST, and then remove on click from
    // ALL elements passed into this function
    function onClick(event) {
      websocket.send(JSON.stringify({eventType: "MOVE_REQUEST", location: name}));
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

  // make sure all the valid moves have the correct ids
  notification.validMoves.forEach(function(val, ind, arr) {
    arr[ind] = getLocationName(val);
  });

  if (notification.playerTag === tag) {
    // make valid locations clickable and enable all turn buttons
    console.log(notification.validMoves);
    addMoveRequestOnClickTo(notification.validMoves);
    document.getElementById("suggest_button").disabled = false;
    document.getElementById("accuse_button").disabled = false;
    document.getElementById("end_turn_button").disabled = false;
  }
}

// called when End Turn button is clicked
function endTurn() {
  if(canEndTurn){
    websocket.send(JSON.stringify({eventType: "END_TURN_REQUEST"}));
    document.getElementById("suggest_button").disabled = true;
    document.getElementById("accuse_button").disabled = true;
    document.getElementById("end_turn_button").disabled = true;
  }
}

function notPlayerTurn(){
  var event = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "It's not your turn."};
  handleEvent(event);
}

function alreadyDidThat(){
       var alreadyDidThat = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "Move or end your turn!"}
       handleEvent(alreadyDidThat);
}

function accuse(){
  if (isMyTurn){
    var accusation = {eventType: "ACCUSATION_REQUEST", room: "", weapon: "", suspect:""}
    var accuseFormElements = document.getElementById("accuse_form").elements;
    accusation.suspect = accuseFormElements[0].value;
    accusation.weapon = accuseFormElements[1].value;
    accusation.room = accuseFormElements[2].value;
    websocket.send(JSON.stringify(accusation));
  }else{
    notPlayerTurn();
  }
};

function suggest(){
  if (isMyTurn){
    if(canSuggest){
      var suggestion = {eventType: "SUGGESTION_REQUEST", suspect: "", weapon: ""};
      var suggestFormElements = document.getElementById("suggest_form").elements;
      suggestion.suspect = suggestFormElements[0].value;
      suggestion.weapon = suggestFormElements[1].value;
      websocket.send(JSON.stringify(suggestion));
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
  var card = document.createElement("div");
  card.innerHTML += cardName;
  card.className += "card";
  card.id = cardName + "_card";

  var img = document.createElement("img");
  img.setAttribute("src", "images/" + cardName.replace(" ", "").replace(".", "") + ".png");
  img.setAttribute("width", "100%");
  card.appendChild(img);

  hand.appendChild(card);
}
