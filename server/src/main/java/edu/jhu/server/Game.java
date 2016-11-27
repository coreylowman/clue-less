package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;

public class Game {
	private enum EventType {
		TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_NOTIFICATION, TURN_NOTIFICATION
	}
  private int currentTurnIndex;
  private List<Player> players;
  private CaseFile secretCards;
  private Board board;
  private boolean gameStarted;
  private Timer timer;

  public Game() {
    this.currentTurnIndex = 0;
    this.players = new ArrayList<Player>();
    this.board = new Board();
    this.gameStarted = false;
    this.timer = new Timer();
    // todo call timer.schedule() here for starting the game
  }

  public boolean isStarted() {
    return this.gameStarted;
  }

  public boolean isFull() {
    return this.players.size() == 6;
  }
  
  // This breaks things if you don't give it a valid tag
  private Player getPlayerByTag(String tag) {
	  for (Player player : players) {
		  if (player.getTag() == tag) {
			  return player;
		  }
	  }
	  return null;
  }

  public void start() {
    this.currentTurnIndex = 0;
    this.gameStarted = true;
    CardShuffler.shuffleAndDealCards(players);
  }

  public void addPlayer(Player player) {
    players.add(player);
    player.setGame(this);
  }

  public void notifyPlayers(JSONObject event) {
    for (Player player : players) {
      player.sendEvent(event);
    }
  }
  
  private JSONObject makeChatMessage(String body) {
	  JSONObject chat = new JSONObject();
	  chat.put("eventType", "CHAT");
	  chat.put("author", "Game");
	  chat.put("body", body);
	  return chat;
  }
  
  private JSONObject makeInvalidRequestMessage(String player, String reason){
	  JSONObject invalidRequest = new JSONObject();
	  invalidRequest.put("eventType", "INVALIDREQUEST");
	  invalidRequest.put("author", "Game");
	  invalidRequest.put("reason", reason);
	  invalidRequest.put("player", player);
	  return invalidRequest;
  }

  public void handleEvent(JSONObject event) {
	  String eventType = event.getString("eventType");
	  switch (EventType.valueOf(eventType)) {
	  	case TEST:
	  		System.out.println("test event");
	  		break;
	  	case CHAT_NOTIFICATION:
	  		System.out.println(event.getString("body"));
	  		notifyPlayers(event);
	  		break;
	  	case SUGGESTION:
	  		Player suggester = getPlayerByTag(event.getString("author"));
	  		if (board.isSuggestionValid(suggester)) {
	  			JSONObject chat = makeChatMessage(suggester.getTag() +
	  					" suggests that it was " + 
	  					event.get("suspect") + 
	  					" with the " + 
	  					event.get("weapon") +
	  					" in the " +
	  					board.getPieceLocation(suggester.getSuspect()).toString());
	  			handleEvent(chat);
	  		} else {
	  			handleEvent(makeInvalidRequestMessage(event.getString("author"), "You are not in a room."));
	  		}
	  		break;
	  	case INVALIDREQUEST:
	  		getPlayerByTag(event.getString("player")).sendEvent(event);
	  	default:
	  		System.out.println("invalid event type");
	  		break;
	  }
  }
}
