package edu.jhu.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.json.JSONObject;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.ICard;
import edu.jhu.server.data.Suspect;

public class Player extends WebSocketAdapter {
  private Session session;
  private String tag;
  private PlayerHolder playerHolder;
  private List<ICard> cards;
  private Suspect suspect;
  private boolean hasLost = false;
  private boolean canSendTo = false;

  public Player(String tag) {
    this.tag = tag;
  }

  public void setCanSendTo(boolean canSendTo) {
	  this.canSendTo = canSendTo;
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

  public void setPlayerHolder(PlayerHolder playerHolder) {
    this.playerHolder = playerHolder;
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
    	if (canSendTo) {
      session.getRemote().sendString(event.toString());
    	}
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    log("WebSocket closed.");
    this.canSendTo = false;
    this.game.disconnectPlayer(this);
    playerHolder.removePlayer(this);
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
    playerHolder.handleEvent(JSONMessage, this);
  }
}
