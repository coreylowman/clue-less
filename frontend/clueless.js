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
      var tag = prompt("Please enter your name");
      var joinRequest = {eventType: "JOIN_REQUEST", playerTag: tag };
      connection.send(JSON.stringify(joinRequest));
    };

    connection.onerror = function (error) {
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
}

init();

function sendChat(){
  var chatEvent = {eventType: "CHAT_NOTIFICATION", body: ""};
  var chatInput = document.getElementById("chat_input");
  chatEvent.body = chatInput.value;
  chatInput.value = '';
  websocket.send(JSON.stringify(chatEvent));
}

function addChatText(text) {
  document.getElementById("chat_text").value += text + "\n";
}

function handleEvent(event){
  switch(event.eventType){
    case "TEST":
      console.log("this is only a test")
      break;
    case "CHAT_NOTIFICATION":
      addChatText(event.author + ': ' + event.body);
      break;
    case "JOIN_NOTIFICATION":
      addChatText(event.playerTag + " (" + event.playerSuspect + ") has joined!");
      break;
    default:
      console.log("Invalid eventType received");
      break;
  }
}
