package edu.jhu.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.ICard;
import edu.jhu.server.data.Suspect;

public class Player extends WebSocketAdapter {
  private static enum LobbyEventType {
    GAMES_REQUEST, JOIN_GAME_REQUEST, CREATE_GAME_REQUEST
  }

  private Session session;
  private String tag;
  private Game game;
  private List<ICard> cards;
  private Suspect suspect;
  private boolean hasLost = false;
  private boolean inLobby;

  public Player(String tag) {
    this.tag = tag;
    this.inLobby = true;
  }

  public boolean getHasLost() {
    return hasLost;
  }

  public void setHasLost(boolean hasLost) {
    this.hasLost = hasLost;
  }

  public String getTag() {
    return this.tag;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public void setSuspect(Suspect suspect) {
    this.suspect = suspect;
  }

  public Suspect getSuspect() {
    return this.suspect;
  }

  public void setCards(List<ICard> cards) {
    this.cards = cards;
  }

  public List<ICard> getCards() {
    return this.cards;
  }

  public boolean hasCard(ICard card) {
    return cards.contains(card);
  }

  public List<ICard> provideEvidence(CaseFile caseFile) {
    return new ArrayList<ICard>();
  }

  private void log(String message) {
    System.out.println("'" + tag + "' (" + session.getRemoteAddress().toString() + ") " + message);
  }

  public void sendEvent(JSONObject event) {
    try {
      session.getRemote().sendString(event.toString());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void handleLobbyEvent(JSONObject event) {
    switch (LobbyEventType.valueOf(event.getString("eventType"))) {
      case GAMES_REQUEST: {
        JSONArray games = new JSONArray();
        for (String name : ClueLessServer.getGameNames()) {
          games.put(name);
        }
        JSONObject notification = new JSONObject();
        notification.put("eventType", "GAMES_NOTIFICATION");
        notification.put("games", games);
        sendEvent(notification);
        break;
      }
      case JOIN_GAME_REQUEST:
        ClueLessServer.joinGame(event.getString("name"), this);
        this.inLobby = false;
        break;
      case CREATE_GAME_REQUEST: {
        ClueLessServer.createGame(event.getString("name"));
        JSONObject notification = new JSONObject();
        notification.put("eventType", "GAME_NOTIFICATION");
        notification.put("name", event.getString("name"));
        ClueLessServer.notifyWaitingPlayers(notification);
        break;
      }
      default:
        break;
    }
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    log("WebSocket closed.");
  }

  @Override
  public void onWebSocketConnect(Session session) {
    this.session = session;
    log("WebSocket connected.");
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    log("Error: " + cause.getMessage());
  }

  @Override
  public void onWebSocketText(final String message) {
    log("Message: " + message);
    JSONObject JSONMessage = new JSONObject(message);

    if (inLobby) {
      handleLobbyEvent(JSONMessage);
    } else {
      game.handleEvent(JSONMessage, this);
    }
  }
}
