var playerTag = parent.tag;
var isMyTurn = true;
var canSuggest = false;
var canEndTurn = false;

// this script is loaded within an iframe, so grab the websocket from the parent
var websocket = parent.websocket;

websocket.onmessage = function(message){
  console.log('game onmessage: ' + message.data);
  handleEvent(JSON.parse(message.data));
};

// now that the we're able to handle server notifications, send the join request
var joinRequest = {eventType: "JOIN_REQUEST", playerTag: parent.tag, game: parent.name };
websocket.send(JSON.stringify(joinRequest));

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
  document.getElementById("suggest_button").disabled = false;
  canSuggest = true;
};

function handlePreventSuggestion(){
  document.getElementById("suggest_button").disabled = true;
  canSuggest = false;
};

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
    case "START_TIME_NOTIFICATION":
      sendToChatBox(tag("Game") + "Starting in " + bold(event.minutes) + "m!");
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
      handleProvideEvidenceNotification(event);
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

  if (notification.playerTag === playerTag) {
    // make valid locations clickable and enable all turn buttons
    isMyTurn = true;
    notification.validMoves.forEach(function(val) {
      highlightLocation(getLocationName(val));
    });

    document.getElementById("suggest_button").disabled = false;
    document.getElementById("accuse_button").disabled = false;
    document.getElementById("end_turn_button").disabled = false;
  }
}

function highlightLocation(locationName) {
  var location = document.getElementById(locationName);
  if(location !== null){
    location.className += " selectable";
    location.onclick =  sendLocationName(locationName);
  }
}

function sendLocationName(locationName) {
  return function() {
    websocket.send(JSON.stringify({eventType: "MOVE_REQUEST", location: locationName}));
    resetLocations();
  }
}

function resetLocations() {
  var classnames = ["room", "horizontal_hallway", "vert_hallway"];
  classnames.forEach(function (classname) {
    var locations = document.getElementsByClassName(classname);
    for (var i = 0;i < locations.length;i++) {
      locations[i].className = locations[i].className.replace(" selectable", "");
      locations[i].onclick = function() {
        return false;
      }
    }
  });
}

// called when End Turn button is clicked
function endTurn() {
  if(canEndTurn){
    isMyTurn = false;
    websocket.send(JSON.stringify({eventType: "END_TURN_REQUEST"}));
    document.getElementById("suggest_button").disabled = true;
    document.getElementById("accuse_button").disabled = true;
    document.getElementById("end_turn_button").disabled = true;
  }else{
    notTimeForThat();
  }
}

function notPlayerTurn(){
  var event = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "It's not your turn."};
  handleEvent(event);
}

function notTimeForThat(){
  var notTimeForThat = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "It's not time for that right now!"}
  handleEvent(notTimeForThat);
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
      resetLocations();
      var suggestion = {eventType: "SUGGESTION_REQUEST", suspect: "", weapon: ""};
      var suggestFormElements = document.getElementById("suggest_form").elements;
      suggestion.suspect = suggestFormElements[0].value;
      suggestion.weapon = suggestFormElements[1].value;
      websocket.send(JSON.stringify(suggestion))
      document.getElementById("suggest_button").disabled = true;
     }else{
       notTimeForThat();
     }
  }else{
    notPlayerTurn();
  }
};

function handleProvideEvidenceNotification(evidence){
  alert("Please provide your evidence!");
  highlightCard(evidence.suspect);
  highlightCard(evidence.room);
  highlightCard(evidence.weapon);
}

function highlightCard(cardName){
  var card = document.getElementById(cardName + "_card");
  if(card !== null){
    card.className += " selectable";
    card.onclick =  sendCardName(cardName);
  }
}

function sendCardName(cardName){
  return function(){
    var evidenceRequest = {eventType: "PROVIDE_EVIDENCE_REQUEST", evidence: cardName};
    websocket.send(JSON.stringify(evidenceRequest));
    resetCards();
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

function showHand(handEvent) {
  document.getElementById("hand").innerHTML = "";
  for (var i = 0; i < handEvent.cards.length; i++) {
    addCard(handEvent.cards[i]);
  }
}

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
