var playerTag = "";
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
      playerTag = prompt("Please enter your name");
      var joinRequest = {eventType: "JOIN_REQUEST", playerTag: playerTag };
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

function tag(message) {
  return "[" + message + "] ";
}

function bold(message, attrs = "") {
  return "<b style=\"" + attrs + "\">" + message + "</b>";
}

function sendToChatBox(message){
  var text = document.getElementById("chat_text");
  text.innerHTML += message + "</br>";
  text.scrollTop = text.scrollHeight;
}

function suggestionChat(suggestion){
  var suggestionChat = tag("Game") +
    bold(suggestion.suggester) + " suggests that it was " +
    bold(suggestion.accused) + " with the " + bold(suggestion.weapon) + " in the " +
    bold(suggestion.room) + ".";
  sendToChatBox(suggestionChat);
}

function showHand(handEvent) {
  document.getElementById("hand").innerHTML = "";
  for (var i = 0; i < handEvent.cards.length; i++) {
    addCard(handEvent.cards[i]);
  }
}

function highlightCard(cardName){
  var card = document.getElementById(cardName + "_card");
  if(card !== null){
    card.className += " selectable";
    card.onclick =  sendCardName(cardName);
  }
}

function handleEvidenceProvided(evidence){
  canEndTurn = true;
  sendToChatBox(tag("Game") + bold(evidence.author) + " says that the crime did not involve " + bold(evidence.evidence) + ".");
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

function handleJoinNotification(event) {
  sendToChatBox(tag("Game") + bold(event.playerTag + " (" + event.playerSuspect + ")") + " has joined!");

  var div = document.createElement("div");
  div.setAttribute("id", "players_" + event.playerTag);
  div.innerHTML = "";
  if (playerTag === event.playerTag)
    div.innerHTML += "> ";
  div.innerHTML += event.playerTag + " (" + event.playerSuspect + ")";

  var players = document.getElementById("players");
  players.appendChild(div);
}

function handleEvent(event){
  console.log(event);
  switch(event.eventType){
    case "TEST":
      console.log("this is only a test")
      break;
    case "CHAT_NOTIFICATION":
      sendToChatBox(tag(event.author) + event.body);
      break;
    case "JOIN_NOTIFICATION":
      handleJoinNotification(event);
      break;
    case "INVALID_REQUEST_NOTIFICATION":
      alert("You cannot do that. " + event.reason);
      break;
    case "PROVIDE_EVIDENCE_NOTIFICATION":
      provideEvidenceNotification(event);
      break;
    case "SUGGESTION_NOTIFICATION":
      suggestionChat(event);
      break;
    case "GAME_START_NOTIFICATION":
      sendToChatBox(tag("Game") + 'Get a Clue!! The game is starting...NOW!');
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
 sendToChatBox(tag("Game") + bold(accusation.accuser) +
              " has accused " +
              bold(accusation.accused) +
              " in the " +
              bold(accusation.room) +
              " with the " +
              bold(accusation.weapon) + ".");
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
  sendToChatBox(tag("Game")  + bold(notification.suspect) + " moved to " + bold(notification.location));

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
  sendToChatBox(tag("Game") + "It's " + bold(notification.playerTag) + "'s turn.");

  // update player turn element
  var takingTurn = document.getElementsByClassName("takingTurn");
  for(var i = 0; i < takingTurn.length; i++){
    takingTurn[i].className = "";
  }
  document.getElementById("players_" + notification.playerTag).className = "takingTurn";

  // make sure all the valid moves have the correct ids
  notification.validMoves.forEach(function(val, ind, arr) {
    arr[ind] = getLocationName(val);
  });

  if (notification.playerTag === playerTag) {
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
  var alreadyDidThat = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "You already did that this turn!"}
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
    cards[i].className = cards[i].className.replace(" selectable", "");
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
