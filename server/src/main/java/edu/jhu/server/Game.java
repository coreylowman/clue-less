package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.Suspect;

public class Game {
  private enum EventType {
    TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_NOTIFICATION, TURN_NOTIFICATION, JOIN_REQUEST,
  }

  private int currentTurnIndex;
  private List<Player> players;
  private CaseFile secretCards;
  private Board board;
  private boolean gameStarted;
  private Timer timer;

  private List<Suspect> remainingSuspects;

  public Game() {
    this.currentTurnIndex = 0;
    this.players = new ArrayList<Player>();
    this.board = new Board();
    this.gameStarted = false;
    this.timer = new Timer();
    // todo call timer.schedule() here for starting the game

    this.remainingSuspects = Suspect.getAll();
  }

  public boolean isStarted() {
    return this.gameStarted;
  }

  public boolean isFull() {
    return this.players.size() == 6;
  }

  private Player getPlayerByTag(String tag) {
    for (Player player : players) {
      if (player.toString() == tag) {
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
    player.setSuspect(this.remainingSuspects.remove(0));

    // note: don't handle player joining here. handle when a JOIN_REQUEST is sent.
    // the WebSocket session might not be set up here, and JOIN_REQUEST allows
    // the player to set their tag.
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
      case JOIN_REQUEST:
        handleJoinRequest(event);
      default:
        System.out.println("invalid event type");
        break;
    }
  }

  private void handleJoinRequest(JSONObject request) {
    Player author = this.getPlayerByTag(request.getString("author"));
    author.setTag(request.getString("playerTag"));

    JSONObject joinNotification = new JSONObject();

    joinNotification.put("eventType", "JOIN_NOTIFICATION");
    joinNotification.put("playerTag", author.toString());
    joinNotification.put("playerSuspect", author.getSuspect().toString());

    notifyPlayers(joinNotification);
  }
}
