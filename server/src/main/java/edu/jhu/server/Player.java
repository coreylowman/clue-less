package edu.jhu.server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class Player extends WebSocketAdapter {
  private Session session;

  public Player() {

  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    System.out.println("Closed");
  }

  @Override
  public void onWebSocketConnect(Session session) {
    System.out.println("Connected:" + session.getRemoteAddress().toString());
    this.session = session;
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    System.out.println("Error: " + cause.getMessage());
  }

  @Override
  public void onWebSocketText(final String message) {
    System.out.println("Message: " + message);
  }
}
