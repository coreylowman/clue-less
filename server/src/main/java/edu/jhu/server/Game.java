package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.Suspect;

public class Game {
  private enum EventType {
    TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_NOTIFICATION, END_TURN_REQUEST,
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
    this.secretCards = CardShuffler.shuffleAndDealCards(players);
    this.board.initialize();
  }

  public void addPlayer(Player player) {
    players.add(player);
    player.setGame(this);
    player.setSuspect(Suspect.get("colonel_mustard"));
  }

  public void sendTurnNotification() {
    Player player = players.get(currentTurnIndex);

    JSONArray validMoves = new JSONArray();
    board.getValidMoves(player.getSuspect()).forEach(loc -> validMoves.put(loc.toString()));

    JSONObject notification = new JSONObject();
    notification.put("eventType", "TURN_NOTIFICATION");
    notification.put("playerTag", player.getTag());
    notification.put("validMoves", validMoves);

    notifyPlayers(notification);
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
        System.out.println(event.getString("body"));
        notifyPlayers(event);
        break;
      case END_TURN_REQUEST:
        handleEndTurnRequest(event);
        break;
      default:
        System.out.println("invalid event type");
        break;
    }
  }

  public void handleEndTurnRequest(JSONObject request) {
    currentTurnIndex = (currentTurnIndex + 1) % players.size();
    sendTurnNotification();
  }
}
