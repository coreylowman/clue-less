// this script is loaded within an iframe, so grab the websocket from the parent
var websocket = parent.websocket;

websocket.onmessage = function(message){
  console.log("lobby onmessage: " + message.data);
  handleEvent(JSON.parse(message.data));
};

// websocket has alreayd been opened at this point
var gameRequest = {eventType: "GAMES_REQUEST"};
websocket.send(JSON.stringify(gameRequest));

function createGame() {
  var name = document.getElementById("createName").value;
  document.getElementById("createName").value = "";

  websocket.send(JSON.stringify({eventType: "CREATE_GAME_REQUEST", name: name}));
}

function joinGame(name) {
  parent.tag = prompt("Please enter your name");
  parent.name = name;
  parent.goto("game.html");
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
