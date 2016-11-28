package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.ILocation;
import edu.jhu.server.data.Room;
import edu.jhu.server.data.Suspect;
import edu.jhu.server.data.Weapon;

public class Game {
	private enum EventType {
		TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_REQUEST, TURN_NOTIFICATION, INVALID_REQUEST_NOTIFICATION
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
	  chat.put("eventType", "CHAT_NOTIFICATION");
	  chat.put("author", "Game");
	  chat.put("body", body);
	  return chat;
  }
  
  private JSONObject makeInvalidRequestMessage(String player, String reason){
	  JSONObject invalidRequest = new JSONObject();
	  invalidRequest.put("eventType", "INVALID_REQUEST_NOTIFICATION");
	  invalidRequest.put("author", "Game");
	  invalidRequest.put("reason", reason);
	  invalidRequest.put("player", player);
	  return invalidRequest;
  }
  
  private JSONObject makeMoveNotification(Suspect suspect, ILocation location){
	  JSONObject move = new JSONObject();
	  move.put("eventType", "MOVE_NOTIFICATION");
	  return move;
  }
  
  
  
  private void provideEvidence(CaseFile casefile, Player suggester) {
	  Player playerWithEvidence = null;
	  for (Player player : players) {
		  if(player.getTag() != suggester.getTag() && 
			(player.hasCard(casefile.getRoom()) || 
			player.hasCard(casefile.getSuspect()) ||
			player.hasCard(casefile.getWeapon()))) {
			  playerWithEvidence = player;
			  break;
		  }  
	  }
	  if (playerWithEvidence == null) {
		  JSONObject chat = makeChatMessage("Nobody could provide evidence against this suggestion!");
		  handleEvent(chat);
	  }
  }
  
  private void handleSuggestion(JSONObject accusation, Player suggester) {
	  ILocation suggestedRoom = board.getLocationOf(suggester.getSuspect());
	  Suspect theAccused = Suspect.get(accusation.get("suspect").toString());
	  Weapon theWeapon = Weapon.get("knife");
	  CaseFile casefile = new CaseFile((Room) suggestedRoom, theAccused, theWeapon);
	  if (suggestedRoom instanceof Room) {
			JSONObject chat = makeChatMessage(suggester.getTag() +
					" suggests that it was " + 
					theAccused.toString() + 
					" with the " + 
					accusation.get("weapon") +
					" in the " +
					suggestedRoom.toString());
			JSONObject move = makeMoveNotification(theAccused, suggestedRoom);
			handleEvent(chat);
			board.movePiece(theAccused, suggestedRoom);
			notifyPlayers(move);
			provideEvidence(casefile, suggester);
			
		} else {
			handleEvent(makeInvalidRequestMessage(accusation.getString("author"), "You are not in a room."));
		}
  }
  
  public void handleEvent(JSONObject event) {
	  String eventType = event.getString("eventType");
	  switch (EventType.valueOf(eventType)) {
	  	case TEST:
	  		System.out.println("test event");
	  		break;
	  	case CHAT_NOTIFICATION:
	  		notifyPlayers(event);
	  		break;
	  	case SUGGESTION_REQUEST:
	  		board.initialize();
	  		Player suggester = getPlayerByTag(event.getString("author"));
	  		suggester.setSuspect(Suspect.get("miss_scarlet"));
	  		board.movePiece(suggester.getSuspect(), Room.get("study"));
	  		handleSuggestion(event, suggester);
	  		
	  		break;
	  	case INVALID_REQUEST_NOTIFICATION:
	  		getPlayerByTag(event.getString("player")).sendEvent(event);
	  		break;
	  	default:
	  		System.out.println("invalid event type");
	  		break;
	  }
  }
}
