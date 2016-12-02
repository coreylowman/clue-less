package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.Suspect;

import edu.jhu.server.data.ILocation;

import edu.jhu.server.data.Room;
import edu.jhu.server.data.Weapon;

public class Game {
  private static enum EventType {
    TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_REQUEST, TURN_NOTIFICATION,
    INVALID_REQUEST_NOTIFICATION, PROVIDE_EVIDENCE_REQUEST, JOIN_REQUEST, END_TURN_REQUEST,
    MOVE_NOTIFICATION, SUGGESTION_NOTIFICATION, JOIN_NOTIFICATION
  }
  
  private static class Constants {
    private static final String EVENT_TYPE = "eventType";
    private static final String PLAYER_TAG = "playerTag";
    private static final String VALID_MOVES = "validMoves";
    private static final String AUTHOR = "author";
    private static final String GAME_AUTHOR = "Game";
    private static final String BODY = "body";
    private static final String REASON = "reason";
    private static final String SUSPECT = "suspect";
    private static final String SUGGESTER = "suggester";
    private static final String ACCUSED = "accused";
    private static final String WEAPON = "weapon";
    private static final String ROOM = "room";
    private static final String PLAYER_SUSPECT = "playerSuspect";
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

    this.remainingSuspects = new ArrayList<>(Suspect.getAll());
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
    player.setSuspect(this.remainingSuspects.remove(0));

    // note: don't handle player joining here. handle when a JOIN_REQUEST is sent.
    // the WebSocket session might not be set up here, and JOIN_REQUEST allows
    // the player to set their tag.
  }

  public void sendTurnNotification() {
    Player player = players.get(currentTurnIndex);

    JSONArray validMoves = new JSONArray();
    board.getValidMoves(player.getSuspect()).forEach(loc -> validMoves.put(loc.toString()));

    JSONObject notification = new JSONObject();
    notification.put(Constants.EVENT_TYPE, EventType.TURN_NOTIFICATION);
    notification.put(Constants.PLAYER_TAG, player.getTag());
    notification.put(Constants.VALID_MOVES, validMoves);

    notifyPlayers(notification);
  }

  public void notifyPlayers(JSONObject event) {
    for (Player player : players) {
      player.sendEvent(event);
    }
  }

  private JSONObject makeChatMessage(String body) {
    JSONObject chat = new JSONObject();
    chat.put(Constants.EVENT_TYPE, EventType.CHAT_NOTIFICATION);
    chat.put(Constants.AUTHOR, Constants.GAME_AUTHOR);
    chat.put(Constants.BODY, body);
    return chat;
  }

  private JSONObject makeInvalidRequestMessage(String reason) {
    JSONObject invalidRequest = new JSONObject();
    invalidRequest.put(Constants.EVENT_TYPE, EventType.INVALID_REQUEST_NOTIFICATION);
    invalidRequest.put(Constants.AUTHOR, Constants.GAME_AUTHOR);
    invalidRequest.put(Constants.REASON, reason);
    return invalidRequest;
  }

  private JSONObject makeMoveNotification(Suspect suspect, ILocation location) {
    JSONObject move = new JSONObject();
    move.put(Constants.EVENT_TYPE, EventType.MOVE_NOTIFICATION);
    return move;
  }

  private void provideEvidence(CaseFile casefile, Player suggester) {
    Player playerWithEvidence = null;
    for (Player player : players) {
      if (player.getTag() != suggester.getTag() && (player.hasCard(casefile.getRoom())
          || player.hasCard(casefile.getSuspect()) || player.hasCard(casefile.getWeapon()))) {
        playerWithEvidence = player;
        break;
      }
    }
    if (playerWithEvidence == null) {
      // FIXME: shouldn't this be a provide evidence outcome notification message?
      JSONObject chat = makeChatMessage("Nobody could provide evidence against this suggestion!");
      handleEvent(chat, null);
    }
  }

  private void handleSuggestion(JSONObject accusation, Player suggester) {
    ILocation suggestedRoom = board.getLocationOf(suggester.getSuspect());
    Suspect theAccused = Suspect.get(accusation.get(Constants.SUSPECT).toString());
    Weapon theWeapon = Weapon.KNIFE;
    CaseFile casefile = new CaseFile((Room) suggestedRoom, theAccused, theWeapon);
    if (suggestedRoom instanceof Room) {
      JSONObject suggestion = new JSONObject();
      suggestion.put(Constants.EVENT_TYPE, EventType.SUGGESTION_NOTIFICATION);
      suggestion.put(Constants.SUGGESTER, suggester.getTag());
      suggestion.put(Constants.ACCUSED, theAccused.toString());
      suggestion.put(Constants.WEAPON, theWeapon.toString());
      suggestion.put(Constants.ROOM, suggestedRoom.toString());
      notifyPlayers(suggestion);
      JSONObject move = makeMoveNotification(theAccused, suggestedRoom);

      board.movePiece(theAccused, suggestedRoom);
      notifyPlayers(move);
      provideEvidence(casefile, suggester);

    } else {
      handleEvent(makeInvalidRequestMessage("You are not in a room."), suggester);
    }
  }

  public void handleEvent(JSONObject event, Player player) {
    String eventType = event.getString(Constants.EVENT_TYPE);
    switch (EventType.valueOf(eventType)) {
      case TEST:
        System.out.println("test event");
        break;
      case CHAT_NOTIFICATION:
        notifyPlayers(event);
        break;
      case END_TURN_REQUEST:
        handleEndTurnRequest(event);
        break;
      case SUGGESTION_REQUEST:
        // FIXME: what is this?
        board.initialize();
        Player suggester = player;
        suggester.setSuspect(Suspect.MISS_SCARLET);
        board.movePiece(suggester.getSuspect(), Room.STUDY);
        handleSuggestion(event, suggester);
        break;
      case INVALID_REQUEST_NOTIFICATION:
        player.sendEvent(event);
        break;
      case JOIN_REQUEST:
        handleJoinRequest(event, player);
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

  private void handleJoinRequest(JSONObject request, Player author) {
    author.setTag(request.getString(Constants.PLAYER_TAG));

    JSONObject joinNotification = new JSONObject();

    joinNotification.put(Constants.EVENT_TYPE, EventType.JOIN_NOTIFICATION);
    joinNotification.put(Constants.PLAYER_TAG, author.getTag());
    joinNotification.put(Constants.PLAYER_SUSPECT, author.getSuspect().toString());

    notifyPlayers(joinNotification);
  }
}
