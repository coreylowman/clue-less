package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.ICard;
import edu.jhu.server.data.Hallway;
import edu.jhu.server.data.Suspect;

import edu.jhu.server.data.ILocation;

import edu.jhu.server.data.Room;
import edu.jhu.server.data.Weapon;

public class Game implements PlayerHolder {
  private static enum EventType {
    TEST, CHAT_NOTIFICATION, GAME_START_NOTIFICATION, SUGGESTION_REQUEST, TURN_NOTIFICATION,
    INVALID_REQUEST_NOTIFICATION, PROVIDE_EVIDENCE_REQUEST, END_TURN_REQUEST,
    MOVE_NOTIFICATION, SUGGESTION_NOTIFICATION, JOIN_NOTIFICATION, ACCUSATION_REQUEST,
    ACCUSATION_NOTIFICATION, SECRET_CARD_NOTIFICATION, ACCUSATION_OUTCOME_NOTIFICATION,
    MOVE_REQUEST, PROVIDE_EVIDENCE_NOTIFICATION, ALLOW_TURN_END, EVIDENCE_PROVIDED_NOTIFICATION,
    HAND_NOTIFICATION, PREVENT_TURN_END, ALLOW_SUGGEST, PREVENT_SUGGEST
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
    private static final String CARDS = "cards";
    private static final String MINUTES = "minutes";
    
    private static final int START_GAME_AFTER_MS = 2 * 60 * 1000;
    private static final int MINUTE_MS = 60 * 1000;
  }
  
  // a simple event sends an event with only its type
  private void handleSimpleEvent(Player player, EventType eventType) {
	  JSONObject simpleEvent = new JSONObject();
	  simpleEvent.put(Constants.EVENT_TYPE, eventType);
	  player.sendEvent(simpleEvent);
  }
  
  
  private int currentTurnIndex;
  private List<Player> players;
  private CaseFile secretCards;
  private Board board;
  private boolean playerHasMoved;
  
  private int timeToStart;
  private boolean gameStarted;
  private Timer timer;
  

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
  	
  	// Someone left while timer was going, we can't start yet
  	if (this.players.size() < 3) {
  	  return;
  	}
  	
  	// Send game start notification
  	notifyPlayers(makeGameStartNotification());
  	
  	// Initialize stuff
    this.currentTurnIndex = 0;
    this.gameStarted = true;

    this.secretCards = CardShuffler.shuffleAndDealCards(players);
    this.board.initialize();
    
    // Send hands to players and then first turn notification
    sendHandsToPlayers();
    sendTurnNotification();
  }
  
  public void removePlayer(Player player) {
    // todo
  }

  public void addPlayer(Player newPlayer) {
    this.players.add(newPlayer);
    newPlayer.setPlayerHolder(this);
    newPlayer.setSuspect(this.remainingSuspects.remove(0));

    JSONObject joinNotification;
    // message the player who just joined who is already in the game
    for (Player player : players) {
      if (player == newPlayer)
        continue;
      joinNotification = new JSONObject();
      joinNotification.put(Constants.EVENT_TYPE, EventType.JOIN_NOTIFICATION);
      joinNotification.put(Constants.PLAYER_TAG, player.getTag());
      joinNotification.put(Constants.PLAYER_SUSPECT, player.getSuspect().toString());
      newPlayer.sendEvent(joinNotification);
    }

    joinNotification = new JSONObject();
    joinNotification.put(Constants.EVENT_TYPE, EventType.JOIN_NOTIFICATION);
    joinNotification.put(Constants.PLAYER_TAG, newPlayer.getTag());
    joinNotification.put(Constants.PLAYER_SUSPECT, newPlayer.getSuspect().toString());
    notifyPlayers(joinNotification);
    
    // Once we reach 3 players, we can start the game. So start a 5 minute
    //  timer!
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
    // Start game if it's full now
    if (isFull()) {
        start();
    }
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

  private void sendHandsToPlayers() {
  	for (Player player : players) {
  		// The basics
  		JSONObject handNotification = new JSONObject();
  		handNotification.put(Constants.EVENT_TYPE, EventType.HAND_NOTIFICATION);
  		handNotification.put(Constants.AUTHOR, Constants.GAME_AUTHOR);
  		
  		// Create array of cards of this player
  		JSONArray handArray = new JSONArray();
  		for (ICard card : player.getCards()) {
  			handArray.put(card.toString());
  		}
  		
  		handNotification.put(Constants.CARDS, handArray);
  		player.sendEvent(handNotification);
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
    move.put(Constants.SUSPECT, suspect.toString());
    move.put(Constants.LOCATION, location.toString());
    return move;
  }
  
  private Player findPlayerWithEvidence(CaseFile casefile, Player suggester) {
	  List<ICard> suspect = new ArrayList<ICard>();
	  suspect.add(casefile.getRoom());
	  Player playerWithEvidence = null;
	  // next Player
	  int nextPlayerIndex = this.currentTurnIndex + 1;
	  for (int i = 0; i < players.size() - 1; i++){
		  Player currentPlayer = players.get((i + nextPlayerIndex) % players.size());
		  if(currentPlayer.hasCard(casefile.getRoom()) || 
			currentPlayer.hasCard(casefile.getSuspect()) ||
			currentPlayer.hasCard(casefile.getWeapon())) {
				playerWithEvidence = currentPlayer;
				break;
		  }
	  }	  
	  return playerWithEvidence;
  }
  
  //This should ultimately be deleted, but I need it for
  //testing
  /*
  private void spoofHand(Player player, ICard card){
	  List<ICard> spoofHand = new ArrayList<ICard>();
	  spoofHand.add(card);
	  player.setCards(spoofHand);
  }
  */
  
  private void provideEvidenceNotification(Player evidenceHolder, CaseFile casefile){
	  JSONObject evidenceNotification = new JSONObject();
	  evidenceNotification.put(Constants.EVENT_TYPE, EventType.PROVIDE_EVIDENCE_NOTIFICATION);
	  evidenceNotification.put(Constants.AUTHOR, Constants.GAME_AUTHOR);
	  evidenceNotification.put(Constants.SUSPECT, casefile.getSuspect().toString());
	  evidenceNotification.put(Constants.WEAPON, casefile.getWeapon().toString());
	  evidenceNotification.put(Constants.ROOM, casefile.getRoom().toString());
	  evidenceHolder.sendEvent(evidenceNotification);
  }
  
  
  private void provideEvidence(CaseFile casefile, Player suggester) {
	  Player playerWithEvidence = findPlayerWithEvidence(casefile, suggester);
	  if (playerWithEvidence == null) {
		  JSONObject chat = makeChatMessage("Nobody could provide evidence against this suggestion!");
		  notifyPlayers(chat);
		  handleSimpleEvent(suggester, EventType.ALLOW_TURN_END);		  
	  } else {
			  provideEvidenceNotification(playerWithEvidence, casefile);
		  
	  }
  }
  
  private void handleSuggestion(JSONObject accusation, Player suggester) {
	  ILocation suggestedRoom = board.getLocationOf(suggester.getSuspect());
	  Suspect theAccused = Suspect.get(accusation.get("suspect").toString());
	  Weapon theWeapon = Weapon.get(accusation.getString("weapon"));
	 
	  if (suggestedRoom instanceof Room) {
		  		// give the second player a spoof hand
		 /*
		  if(players.size() > 2){
		  		currentTurnIndex = 1;
		  		spoofHand(players.get(0), Suspect.get("Mrs. Peacock"));
		  		spoofHand(players.get(1), Suspect.get("Professor Plum"));
		  		spoofHand(players.get(2), Suspect.get("Mrs. White"));
		  	}
		  	if(players.size() > 1){
		  		currentTurnIndex = 1;
		  		spoofHand(players.get(0), Suspect.get("Mrs. Peacock"));
		  		spoofHand(players.get(1), Suspect.get("Professor Plum"));
		  	}
		  	*/
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
			CaseFile casefile = new CaseFile((Room) suggestedRoom, theAccused, theWeapon);
			provideEvidence(casefile, suggester);
			
		} else {
			suggester.sendEvent(makeInvalidRequestMessage("You are not in a room."));
		}
  }
  
  
  private void handleProvideEvidence(JSONObject evidence, Player player) {
	  Player suggester = players.get(this.currentTurnIndex %  players.size());
	  evidence.put(Constants.EVENT_TYPE, EventType.EVIDENCE_PROVIDED_NOTIFICATION);
	  evidence.put(Constants.AUTHOR, player.getTag());
	  suggester.sendEvent(evidence);
	  handleSimpleEvent(suggester, EventType.PREVENT_SUGGEST);
	  handleSimpleEvent(suggester, EventType.ALLOW_TURN_END);
	  notifyPlayers(makeChatMessage(player.getTag() + 
			  " has disproven " +
			  suggester.getTag() +
			  	"'s suggestion."));
  }

  public void handleEvent(JSONObject event, Player player) {
    String eventType = event.getString(Constants.EVENT_TYPE);
    switch (EventType.valueOf(eventType)) {
      case TEST:
        System.out.println("test event");
        break;
      case CHAT_NOTIFICATION:
    	event.put("author", player.getTag());
        notifyPlayers(event);
        break;
      case SUGGESTION_REQUEST:
        Player suggester = player;
        handleSuggestion(event, suggester);
        break;
      case INVALID_REQUEST_NOTIFICATION:
        player.sendEvent(event);
        break;
      case ACCUSATION_REQUEST:
        handleAccusationRequest(event, player);
        break;
      case MOVE_REQUEST:
        handleMoveRequest(event, player);
        break;
	  case PROVIDE_EVIDENCE_REQUEST:
	  	handleProvideEvidence(event, player);
	  	break;
      case END_TURN_REQUEST:
    	handleEndTurnRequest(player);
    	break;
      default:
        System.out.println("invalid event type");
        break;
    }
  }
  
  private void handleMoveRequest(JSONObject request, Player player) {
    // a player may move only if it is their turn
    if (!isPlayersTurn(player)) {
      player.sendEvent(makeInvalidRequestMessage("It is not your turn."));
      return;
    }
    
    // a player cannot move if they have already moved
    if (playerHasMoved) {
      player.sendEvent(makeInvalidRequestMessage("You have already moved."));
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
      player.sendEvent(makeInvalidRequestMessage("Invalid move."));
      return;
    }
    
    // execute the move
    board.movePiece(player.getSuspect(), dest);
    playerHasMoved = true;
    handleSimpleEvent(player, EventType.ALLOW_SUGGEST);
    handleSimpleEvent(player, EventType.ALLOW_TURN_END);
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
      player.sendEvent(makeInvalidRequestMessage("It is not your turn."));
      return;
    }
    
    // get the case file components from the info in the request
    final Room room = Room.get(request.getString(Constants.ROOM));
    final Suspect suspect = Suspect.get(request.getString(Constants.SUSPECT));
    final Weapon weapon = Weapon.get(request.getString(Constants.WEAPON));
    
    if (room == null || suspect == null || weapon == null) {
      player.sendEvent(makeInvalidRequestMessage("Invalid Case File."));
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
    accusationOutcomeNotification.put(Constants.ACCUSER, player.getTag());
    accusationOutcomeNotification.put(Constants.OUTCOME, String.valueOf(outcome));
    notifyPlayers(accusationOutcomeNotification);
    
    if (outcome) {
      notifyPlayers(secretCardNotification);
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
      handleEndTurnRequest(player);
    }
  }

  public void handleEndTurnRequest(Player player) {
	  if (!isPlayersTurn(player)){
		  player.sendEvent(makeInvalidRequestMessage("It's not your turn."));
	  }
	  else{
	// keep going until we get to a player that is allowed additional turns
		  do {
			  currentTurnIndex = (currentTurnIndex + 1) % players.size();
		  } while (players.get(currentTurnIndex).getHasLost());
    
		  playerHasMoved = false;
    
		  sendTurnNotification();
	  }
  }

}
