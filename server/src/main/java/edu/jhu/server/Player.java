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
  private Game game;
  private List<ICard> cards;
  private Suspect suspect;

  public Player(String tag) {
	  this.tag = tag;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public void setSuspect(Suspect suspect) {
    this.suspect = suspect;
  }

  public void setCards(List<ICard> cards) {
    this.cards = cards;
  }

  public List<ICard> provideEvidence(CaseFile caseFile) {
    return new ArrayList<ICard>();
  }

  private void log(String message) {
	  System.out.println("log1");
	  System.out.println(this.getSession());
	  System.out.println("log2");
	  System.out.println("'" + tag + "' (" + session.getRemoteAddress().toString() + ") " + message);
	  System.out.println("log3");
  }

  public void sendEvent(JSONObject event) {
    try {
      session.getRemote().sendString(event.toString());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    game.handleEvent(new JSONObject(message));
  }
}
