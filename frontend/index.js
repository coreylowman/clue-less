function establishWebsocket() {
  // if user is running mozilla then use it's built-in WebSocket
  window.WebSocket = window.WebSocket || window.MozWebSocket;

  //establish connection at localhost:3000
  var connection = new WebSocket('ws://127.0.0.1:3000');

  //takes care of any initial action upon opening of websocket
  connection.onopen = function () {
    console.log('onopen');
    goto("lobby.html");
  };

  connection.onerror = function (error) {
    console.log('onerror');
  };

  connection.onmessage = function(message){
    console.log('onmessage: ' + message.data);
  };

  return connection;
}

// sets up global websocket for use in game & lobby
var websocket = establishWebsocket();

function goto(path) {
  var frame = document.getElementById("iframe");
  frame.src = path;
}
