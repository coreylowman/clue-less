package edu.jhu.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room implements ILocation, ICard {
  public static final String HALL = "Hall";
  public static final String LOUNGE = "Lounge";
  public static final String DINING_ROOM = "Dining Room";
  public static final String KITCHEN = "Kitchen";
  public static final String BALLROOM = "Ballroom";
  public static final String CONSERVATORY = "Conservatory";
  public static final String BILLIARD_ROOM = "Billiard Room";
  public static final String LIBRARY = "Library";
  public static final String STUDY = "Study";

  public static final String[] NAMES =
      {HALL, LOUNGE, DINING_ROOM, KITCHEN, BALLROOM, CONSERVATORY, BILLIARD_ROOM, LIBRARY, STUDY};

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

  // auto-genned
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  // auto-genned
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Room other = (Room) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
