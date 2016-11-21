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
  websocket.send(JSON.stringify({test:'test'}));
};


// moves character HTML element into destination HTML element
// refer to style.css for names
// eg
// moveTo('col_mustard', 'study');

function moveTo(character, destination){
  document.getElementById(destination).appendChild(document.getElementById(character));
}

function genDestinationIdFunc(roomId){
  var roomIdFunc = function(event){
    console.log(roomId)
  }
  return roomIdFunc;
};


function setDestinationIdFuncs(destinationClass) {
  var destinations = document.getElementsByClassName(destinationClass);
  for(var i = 0; i < destinations.length; i++){
    var destinationIdFunc = genDestinationIdFunc(destinations[i].id);
    destinations[i].addEventListener("click", destinationIdFunc);
  }
};

function init(){
  setDestinationIdFuncs("room");
  setDestinationIdFuncs("horizontal_hallway");
  setDestinationIdFuncs("vert_hallway");
}

init();