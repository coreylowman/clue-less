package edu.jhu.server;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class ClueLessServer {

  static int id;
  static Lobby lobby;

  public static void main(String[] args) throws Exception {
    id = 0;
    lobby = new Lobby();

    try {
      WebSocketHandler wsHandler = new WebSocketHandler() {
        @Override
        public void configure(WebSocketServletFactory factory) {
          factory.getPolicy().setIdleTimeout(20 * 60 * 1000); // 20 min. timeout on web sockets
          factory.setCreator(new WebSocketCreator() {
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
              Player player = new Player("player" + String.valueOf(id++));
              lobby.addPlayer(player);
              return player;
            }
          });
        }
      };

      ResourceHandler resHandler = new ResourceHandler();
      resHandler.setResourceBase("../frontend");

      HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[] {wsHandler, resHandler});

      Server server = new Server(new InetSocketAddress(3000));
      server.setHandler(handlers);
      server.start();
      server.join();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    }
  }
}
