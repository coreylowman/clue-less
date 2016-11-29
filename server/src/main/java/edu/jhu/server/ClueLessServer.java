package edu.jhu.server;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class ClueLessServer {

  static int id;
  static Game currentGame;

  public static void main(String[] args) throws Exception {
    id = 0;
    currentGame = new Game();

    try {
      WebSocketHandler wsHandler = new WebSocketHandler() {
        @Override
        public void configure(WebSocketServletFactory factory) {
          factory.setCreator(new WebSocketCreator() {
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
              Player player = new Player("player" + String.valueOf(id++));
              if (currentGame.isStarted() || currentGame.isFull()) {
                currentGame = new Game();
              }

              currentGame.addPlayer(player);
              currentGame.start();

              return player;
            }
          });
        }
      };

      Server server = new Server(new InetSocketAddress(3000));
      server.setHandler(wsHandler);
      server.start();
      server.join();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }
  }
}
