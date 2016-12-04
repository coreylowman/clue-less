package edu.jhu.server;

import org.json.JSONObject;

public interface PlayerHolder {
  public void notifyPlayers(JSONObject event);

  public void addPlayer(Player player);

  public void removePlayer(Player player);

  public void handleEvent(JSONObject event, Player player);
}
