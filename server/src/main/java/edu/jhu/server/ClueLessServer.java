package edu.jhu.server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONObject;

public class ClueLessServer {

  static int id;
  static Map<String, Game> availableGames;
  static Set<Player> noGamePlayers;

  public static void createGame(String name) {
    availableGames.put(name, new Game());
  }

  public static void joinGame(String name, Player player) {
    Game game = availableGames.get(name);
    game.addPlayer(player);
    noGamePlayers.remove(player);

    if (game.isStarted() || game.isFull()) {
      availableGames.remove(name);

      JSONObject notification = new JSONObject();
      notification.put("eventType", "GAME_REMOVED_NOTIFICATION");
      notification.put("name", name);
      notifyWaitingPlayers(notification);
    }
  }

  public static Set<String> getGameNames() {
    return availableGames.keySet();
  }

  public static void notifyWaitingPlayers(JSONObject event) {
    for (Player player : noGamePlayers) {
      player.sendEvent(event);
    }
  }

  public static void main(String[] args) throws Exception {
    id = 0;
    availableGames = new HashMap<>();
    noGamePlayers = new HashSet<>();

    try {
      WebSocketHandler wsHandler = new WebSocketHandler() {
        @Override
        public void configure(WebSocketServletFactory factory) {
          factory.setCreator(new WebSocketCreator() {
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
              Player player = new Player("player" + String.valueOf(id++));
              noGamePlayers.add(player);
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
