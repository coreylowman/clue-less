package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.Hallway;
import edu.jhu.server.data.Suspect;

import edu.jhu.server.data.ILocation;

import edu.jhu.server.data.Room;
import edu.jhu.server.data.Weapon;

public class Game {
  private static enum EventType {
    TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_REQUEST, TURN_NOTIFICATION,
    INVALID_REQUEST_NOTIFICATION, PROVIDE_EVIDENCE_REQUEST, JOIN_REQUEST, END_TURN_REQUEST,
    MOVE_NOTIFICATION, SUGGESTION_NOTIFICATION, JOIN_NOTIFICATION, ACCUSATION_REQUEST,
    ACCUSATION_NOTIFICATION, SECRET_CARD_NOTIFICATION, ACCUSATION_OUTCOME_NOTIFICATION,
    MOVE_REQUEST
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
    private static final String ACCUSER = "accuser";
    private static final String OUTCOME = "outcome";
    private static final String LOCATION = "location";
    
    private static final int START_GAME_AFTER_MS = 5 * 60 * 1000;
  }

  private int currentTurnIndex;
  private List<Player> players;
  private CaseFile secretCards;
  private Board board;
  private boolean gameStarted;
  private Timer timer;
  private boolean playerHasMoved;

  private List<Suspect> remainingSuspects;

  public Game() {
    this.currentTurnIndex = 0;
    this.players = new ArrayList<Player>();
    this.board = new Board();
    this.gameStarted = false;
    this.remainingSuspects = new ArrayList<>(Suspect.getAll());
  }

  public boolean isStarted() {
    return this.gameStarted;
  }

  public boolean isFull() {
    return this.players.size() == 6;
  }

  public void start() {
  	// If the timer is set, we need to cancel anything it had scheduled
  	if (timer != null) {
  		timer.cancel();
  		timer.purge();
  	}
  	
  	// Send game start notification
  	notifyPlayers(makeGameStartNotification());
  	
  	// Initialize stuff
    this.currentTurnIndex = 0;
    this.gameStarted = true;
    this.secretCards = CardShuffler.shuffleAndDealCards(players);
    this.board.initialize();
    
    // Send first turn notification
    sendTurnNotification();
  }

  public void addPlayer(Player player) {
    this.players.add(player);
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
  
  private JSONObject makeGameStartNotification() {
  	JSONObject gameStart = new JSONObject();
  	gameStart.put(Constants.EVENT_TYPE, EventType.GAME_START_NOTIFICATION);
  	gameStart.put(Constants.AUTHOR, Constants.GAME_AUTHOR);
  	return gameStart;
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
    move.put(Constants.SUSPECT, suspect.getName());
    move.put(Constants.LOCATION, location.toString());
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
      case ACCUSATION_REQUEST:
        handleAccusationRequest(event, player);
        break;
      case MOVE_REQUEST:
        handleMoveRequest(event, player);
        break;
      default:
        System.out.println("invalid event type");
        break;
    }
  }
  
  private void handleMoveRequest(JSONObject request, Player player) {
    // a player may move only if it is their turn
    if (!isPlayersTurn(player)) {
      handleEvent(makeInvalidRequestMessage("It is not your turn."), player);
      return;
    }
    
    // a player cannot move if they have already moved
    if (playerHasMoved) {
      handleEvent(makeInvalidRequestMessage("You have already moved."), player);
      return;
    }
    
    // get the destination room/hallway from the info in the request
    final String string = request.getString(Constants.LOCATION);
    ILocation dest = Room.get(string);
    if (dest == null) {
      dest = Hallway.get(string);
    }
    
    // check the validity of the move
    if (dest == null || !board.isMoveValid(player.getSuspect(), dest)) {
      handleEvent(makeInvalidRequestMessage("Invalid move."), player);
      return;
    }
    
    // execute the move
    board.movePiece(player.getSuspect(), dest);
    playerHasMoved = true;
    
    // send move notification to all players
    final JSONObject moveNotification = makeMoveNotification(player.getSuspect(), dest);
    notifyPlayers(moveNotification);
  }
  
  private boolean isPlayersTurn(Player player) {
    if (player == null)
      throw new IllegalArgumentException("player was null");
    
    return player.equals(players.get(currentTurnIndex));
  }
  
  public void handleAccusationRequest(JSONObject request, Player player) {
    // a player may make an accusation at any time as long as it is their turn
    if (!isPlayersTurn(player)) {
      handleEvent(makeInvalidRequestMessage("It is not your turn."), player);
      return;
    }
    
    // get the case file components from the info in the request
    final Room room = Room.get(request.getString(Constants.ROOM));
    final Suspect suspect = Suspect.get(request.getString(Constants.SUSPECT));
    final Weapon weapon = Weapon.get(request.getString(Constants.WEAPON));
    
    if (room == null || suspect == null || weapon == null) {
      handleEvent(makeInvalidRequestMessage("Invalid Case File."), player);
      return;
    }
    
    // send accusation notification to all players
    final JSONObject accusationNotification = new JSONObject();
    accusationNotification.put(Constants.EVENT_TYPE, EventType.ACCUSATION_NOTIFICATION);
    accusationNotification.put(Constants.ACCUSER, player.getTag());
    accusationNotification.put(Constants.ROOM, room.getName());
    accusationNotification.put(Constants.ACCUSED, suspect.getName());
    accusationNotification.put(Constants.WEAPON, weapon.getName());
    notifyPlayers(accusationNotification);
    
    // send secret card notification to player who made the accusation
    final JSONObject secretCardNotification = new JSONObject();
    secretCardNotification.put(Constants.EVENT_TYPE, EventType.SECRET_CARD_NOTIFICATION);
    secretCardNotification.put(Constants.ROOM, secretCards.getRoom().getName());
    secretCardNotification.put(Constants.ACCUSED, secretCards.getSuspect().getName());
    secretCardNotification.put(Constants.WEAPON, secretCards.getWeapon().getName());
    player.sendEvent(secretCardNotification);
    
    // create case file and check it against the secret cards
    final CaseFile caseFile = new CaseFile(room, suspect, weapon);
    boolean outcome = secretCards.equals(caseFile);
    
    // send accusation outcome notification to all players
    final JSONObject accusationOutcomeNotification = new JSONObject();
    accusationOutcomeNotification.put(Constants.EVENT_TYPE, EventType.ACCUSATION_OUTCOME_NOTIFICATION);
    accusationNotification.put(Constants.ACCUSER, player.getTag());
    accusationNotification.put(Constants.OUTCOME, String.valueOf(outcome));
    notifyPlayers(accusationOutcomeNotification);
    
    if (outcome) {
      // player wins, end the game
      
      // TODO: actually end the game somehow
      
      // for now, just prevent all other players from taking additional turns
      for (Player p : players) {
        if (!p.equals(player)) {
          player.setHasLost(true);
        }
      }
    } else {
      // player loses, prevent them from taking additional turns
      player.setHasLost(true);
      
      // this player's turn is now over
      final JSONObject endTurnRequest = new JSONObject();
      endTurnRequest.put(Constants.EVENT_TYPE, EventType.END_TURN_REQUEST);
      handleEvent(endTurnRequest, player);
    }
  }

  public void handleEndTurnRequest(JSONObject request) {
    // keep going until we get to a player that is allowed additional turns
    do {
      currentTurnIndex = (currentTurnIndex + 1) % players.size();
    } while (players.get(currentTurnIndex).getHasLost());
    
    playerHasMoved = false;
    
    sendTurnNotification();
  }

  private void handleJoinRequest(JSONObject request, Player author) {
	  
	  author.setTag(request.getString(Constants.PLAYER_TAG));

    JSONObject joinNotification = new JSONObject();

    joinNotification.put(Constants.EVENT_TYPE, EventType.JOIN_NOTIFICATION);
    joinNotification.put(Constants.PLAYER_TAG, author.getTag());
    joinNotification.put(Constants.PLAYER_SUSPECT, author.getSuspect().toString());

    notifyPlayers(joinNotification);
    
    // Start game if it's full now
  	if (isFull()) {
  		start();
  	}
  	
  	// Once we reach 3 players, we can start the game. So start a 5 minute
  	//	timer!
  	if (this.players.size() == 3 && timer == null) {
  		timer = new Timer();
  		
  		// In 5 minutes, start the game.
  		timer.schedule(new TimerTask() {
			    @Override
			    public void run() {
			        start();
			    }
			}, Constants.START_GAME_AFTER_MS);
  	}
  }
}
