function establishWebsocket() {
    // if user is running mozilla then use it's built-in WebSocket
    window.WebSocket = window.WebSocket || window.MozWebSocket;

  //establish connection at localhost:3000
    var connection = new WebSocket('ws://127.0.0.1:3000');

    //takes care of any initial action upon opening of websocket
    connection.onopen = function () {
      console.log('opened');

      var gameRequest = {eventType: "GAMES_REQUEST"};
      connection.send(JSON.stringify(gameRequest));
    };

    connection.onerror = function (error) {
      console.log('websocket messed up');
    };

    connection.onmessage = function(message){
      console.log(message);
      handleEvent(JSON.parse(message.data));
    };
  return connection;
}

var webSocket = establishWebsocket();
sessionStorage.webSocket = webSocket;

function handleEvent(event) {
  switch(event.eventType){
    case "GAMES_NOTIFICATION":
      for(var i = 0;i < event.games;i++) {
        
      }
      break;
    case "GAME_NOTIFICATION":
      break;
    case "GAME_REMOVED_NOTIFICATION":
      break;
    default:
      break;
  }
}