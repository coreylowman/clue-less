// TODO store this when joining game
var tag = "player0";

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
}

init();

function sendChat(){
  var chatEvent = {eventType: "CHAT_NOTIFICATION", body: ""};
  var chatInput = document.getElementById("chat_input");
  chatEvent.body = chatInput.value;
  chatInput.value = '';
  websocket.send(JSON.stringify(chatEvent));
}

websocket.onmessage = function(message){
  console.log(message);
  handleEvent(JSON.parse(message.data));
}

function handleEvent(event){
  switch(event.eventType){
    case "TEST":
    console.log("this is only a test")
    break;
    case "CHAT_NOTIFICATION":
    document.getElementById("chat_text").value += event.author + ': ' + event.body + "\n";
    console.log(chat_text);
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
  // TODO replace with addChatMessage function when PR gets merged
  document.getElementById("chat_text").value += "It is " + notification.playerTag + "'s turn.\n";

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
