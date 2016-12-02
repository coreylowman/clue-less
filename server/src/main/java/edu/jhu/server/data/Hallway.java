package edu.jhu.server.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Hallway implements ILocation {
	
  private static final Map<String, Hallway> hallways = new HashMap<>();

  static {
    final Room[][] hallwayList = {{Room.STUDY, Room.LIBRARY}, {Room.STUDY, Room.HALL},
        {Room.HALL, Room.BILLIARD_ROOM}, {Room.HALL, Room.LOUNGE}, {Room.LOUNGE, Room.DINING_ROOM},
        {Room.DINING_ROOM, Room.BILLIARD_ROOM}, {Room.DINING_ROOM, Room.KITCHEN},
        {Room.KITCHEN, Room.BALLROOM}, {Room.BALLROOM, Room.BILLIARD_ROOM},
        {Room.BALLROOM, Room.CONSERVATORY}, {Room.CONSERVATORY, Room.LIBRARY},
        {Room.LIBRARY, Room.BILLIARD_ROOM}};
    
    for (Room[] rooms : hallwayList) {
      final Hallway hallway = new Hallway(rooms[0], rooms[1]);
      
      hallways.put(hallway.toString(), hallway);
    }
  }

  public static Hallway get(Room end1, Room end2) {
  	if (end1 == null)
  		throw new IllegalArgumentException("end1 was null");
  	if (end2 == null)
  		throw new IllegalArgumentException("end2 was null");
  	
    final Hallway try1 = hallways.get(hallwayToString(end1, end2));
    
    if (try1 != null) {
      return try1;
    } else {
      return hallways.get(hallwayToString(end2, end1));
    }
  }

  public static Collection<Hallway> getAll() {
    return Collections.unmodifiableCollection(hallways.values());
  }
  
  private static String hallwayToString(Room end1, Room end2) {
  	return end1 + "_" + end2;
  }

  private Room end1;
  private Room end2;

  private Hallway(Room end1, Room end2) {
  	this.end1 = end1;
    this.end2 = end2;
  }

  public Room getEnd1() {
    return end1;
  }

  public Room getEnd2() {
    return end2;
  }

  @Override
  public String toString() {
    return hallwayToString(end1, end2);
  }

  @Override
  public int hashCode() {
    final String val = this.toString();
    final int prime = 31;
    int result = 1;
    result = prime * result + ((val == null) ? 0 : val.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Hallway other = (Hallway) obj;
    return (this.end1.equals(other.end1) && this.end2.equals(other.end2))
        || (this.end1.equals(other.end2) && this.end2.equals(other.end1));
  }
}
