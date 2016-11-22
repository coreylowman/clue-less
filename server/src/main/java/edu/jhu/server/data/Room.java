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
