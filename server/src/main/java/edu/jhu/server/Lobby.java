package edu.jhu.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public class Lobby implements PlayerHolder {
  private static class Constants {
    private static final String EVENT_TYPE = "eventType";
    private static final String NAME = "name";
    private static final String GAME = "game";
    private static final String PLAYER_TAG = "playerTag";
  }

  private static enum EventType {
    GAMES_REQUEST, JOIN_REQUEST, CREATE_GAME_REQUEST
  }

  Map<String, Game> availableGames;
  Set<Player> noGamePlayers;

  public Lobby() {
    availableGames = new HashMap<>();
    noGamePlayers = new HashSet<>();
  }

  public void addPlayer(Player player) {
    noGamePlayers.add(player);
    player.setPlayerHolder(this);
  }

  public void removePlayer(Player player) {
    noGamePlayers.remove(player);
  }

  public void createGame(String name) {
    availableGames.put(name, new Game());
  }

  public void joinGame(String name, Player player) {
    Game game = availableGames.get(name);
    game.addPlayer(player);
    noGamePlayers.remove(player);

    if (game.isStarted() || game.isFull()) {
      availableGames.remove(name);

      JSONObject notification = new JSONObject();
      notification.put(Constants.EVENT_TYPE, "GAME_REMOVED_NOTIFICATION");
      notification.put(Constants.NAME, name);
      notifyPlayers(notification);
    }
  }

  public void notifyPlayers(JSONObject event) {
    for (Player player : noGamePlayers) {
      player.sendEvent(event);
    }
  }

  public void handleEvent(JSONObject event, Player player) {
    switch (EventType.valueOf(event.getString(Constants.EVENT_TYPE))) {
      case GAMES_REQUEST: {
        for (String name : availableGames.keySet()) {
          JSONObject notification = new JSONObject();
          notification.put(Constants.EVENT_TYPE, "GAME_NOTIFICATION");
          notification.put(Constants.NAME, name);
          notifyPlayers(notification);
        }
        break;
      }
      case JOIN_REQUEST:
        player.setTag(event.getString(Constants.PLAYER_TAG));
        joinGame(event.getString(Constants.GAME), player);
        break;
      case CREATE_GAME_REQUEST: {
        createGame(event.getString(Constants.NAME));

        JSONObject notification = new JSONObject();
        notification.put(Constants.EVENT_TYPE, "GAME_NOTIFICATION");
        notification.put(Constants.NAME, event.getString(Constants.NAME));
        notifyPlayers(notification);
        break;
      }
      default:
        break;
    }
  }

}
