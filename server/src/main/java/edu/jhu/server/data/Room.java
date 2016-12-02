package edu.jhu.server.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Room implements ILocation, ICard {
	
  public static final Room HALL = new Room("Hall");
  public static final Room LOUNGE = new Room("Lounge");
  public static final Room DINING_ROOM = new Room("Dining Room");
  public static final Room KITCHEN = new Room("Kitchen");
  public static final Room BALLROOM = new Room("Ballroom");
  public static final Room CONSERVATORY = new Room("Conservatory");
  public static final Room BILLIARD_ROOM = new Room("Billiard Room");
  public static final Room LIBRARY = new Room("Library");
  public static final Room STUDY = new Room("Study");

  private static final Map<String, Room> rooms = new HashMap<>();

  static {
    final Room[] roomList =
        {HALL, LOUNGE, DINING_ROOM, KITCHEN, BALLROOM, CONSERVATORY, BILLIARD_ROOM, LIBRARY, STUDY};
    
    for (Room room : roomList) {
      rooms.put(room.getName(), room);
    }
  }

  public static Room get(String name) {
  	if (name == null || name.isEmpty())
  		throw new IllegalArgumentException("name was null or empty");
  	
  	return rooms.get(name);
  }

  public static Collection<Room> getAll() {
    return Collections.unmodifiableCollection(rooms.values());
  }

  private String name;
  
  private Room(String name) {
  	this.name = name;
  }
  
  public String getName() {
	  return name;
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
