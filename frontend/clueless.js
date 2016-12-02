var isMyTurn = true;
var isEvidenceSelectionTime = false;

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
    };

    connection.onerror = function (error) {
      console.log('websocket messed up');
        // an error occurred when sending/receiving data
    };

    connection.onmessage = function (message) {
        // try to decode json (I assume that each message from server is json)
        try {
            var json = JSON.parse(message.data);
          console.log(json);
        } catch (e) {
            console.log('This doesn\'t look like a valid JSON: ', message.data);
            return;
        }
        // handle incoming message
    };

  return connection;
}

var websocket = establishWebsocket();

//example of sending JSON object to the server
websocket.onopen = function(){
  websocket.send(JSON.stringify({eventType:'TEST'}));
  suggest();
};


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

websocket.onmessage = function(message){
  console.log(message);
  handleEvent(JSON.parse(message.data));
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
    case "INVALID_REQUEST_NOTIFICATION":
    alert("You cannot do that. " + event.reason);
    break;
    case "PROVIDE_EVIDENCE_NOTIFICATION":
    provideEvidenceNotification(event);

    break;
    case "SUGGESTION_NOTIFICATION":
    suggestionChat(event);
    console.log("suggestion");
    break;
    default:
    console.log("Invalid eventType received");
    break;
  }
}

function notPlayerTurn(){
  var event = {eventType: "INVALID_REQUEST_NOTIFICATION", reason: "It's not your turn."};
  handleEvent(event);
}

function suggest(){
  if (isMyTurn){
    var suggestion = {eventType: "SUGGESTION_REQUEST", suspect: "", weapon: ""};
    var suggestFormElements = document.getElementById("suggest_form").elements
    suggestion.suspect = suggestFormElements[0].value;
    suggestion.weapon = suggestFormElements[1].value;
    websocket.send(JSON.stringify(suggestion));
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
