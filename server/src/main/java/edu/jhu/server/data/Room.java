package edu.jhu.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room implements ILocation, ICard {
  private static Map<String, Room> rooms = new HashMap<String, Room>();

  public static Room get(String name) {
    return rooms.get(name);
  }

  public static List<Room> getAll() {
    return new ArrayList<Room>(rooms.values());
  }

  private String name;

  public Room(String name) {
    rooms.put(name, this);
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
