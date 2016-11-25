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

  public void handleEvent(JSONObject event) {
	  String eventType = event.getString("eventType");
	  switch (EventType.valueOf(eventType)) {
	  	case TEST:
	  		System.out.println("test event");
	  		break;

	  	case CHAT_NOTIFICATION:
	  		notifyPlayers(event);
	  		break;
	  	case SUGGESTION:
	  		System.out.println("Suggestion!");
	  		break;
	  	default:
	  		System.out.println("invalid event type");
	  		break;
	  }
  }
}
