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

var websocket = establishWebsocket();

function createGame() {
  var name = document.getElementById("createName").value;
  document.getElementById("createName").value = "";

  websocket.send(JSON.stringify({eventType: "CREATE_GAME_REQUEST", name: name}));
}

function joinGame(name) {
  sessionStorage.gameName = name;
  window.location.href = "clueless.html"
}

function createGameElement(name) {
  var game = document.createElement("li");
  game.className = "game";
  game.setAttribute("id", "game_" + name);

  var nameDiv = document.createElement("div");
  nameDiv.className = "gameName";
  nameDiv.innerHTML = name;
  game.appendChild(nameDiv);

  var join = document.createElement("button");
  join.className = "join";
  join.innerHTML = "join";
  join.setAttribute("onClick", "joinGame(\"" + name + "\")");
  game.appendChild(join);

  return game;
}

function handleEvent(event) {
  var gameList = document.getElementById("gameList");
  var creationNode = document.getElementById("create");
  switch(event.eventType){
    case "GAMES_NOTIFICATION":
      for(var i = 0;i < event.games.length;i++) {
        var game = createGameElement(event.games[i]);
        gameList.insertBefore(game, creationNode);
      }
      break;
    case "GAME_NOTIFICATION":
      var game = createGameElement(event.name);
      gameList.insertBefore(game, creationNode);
      break;
    case "GAME_REMOVED_NOTIFICATION":
      gameList.removeChild(document.getElementById(event.name));
      break;
    default:
      break;
  }
}