package edu.jhu.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hallway implements ILocation {
  private static Map<String, Hallway> hallways;

  static {
    hallways = new HashMap<String, Hallway>();
    String[][] hallways = {{Room.STUDY, Room.LIBRARY}, {Room.STUDY, Room.HALL},
        {Room.HALL, Room.BILLIARD_ROOM}, {Room.HALL, Room.LOUNGE}, {Room.LOUNGE, Room.DINING_ROOM},
        {Room.DINING_ROOM, Room.BILLIARD_ROOM}, {Room.DINING_ROOM, Room.KITCHEN},
        {Room.KITCHEN, Room.BALLROOM}, {Room.BALLROOM, Room.BILLIARD_ROOM},
        {Room.BALLROOM, Room.CONSERVATORY}, {Room.CONSERVATORY, Room.LIBRARY},
        {Room.LIBRARY, Room.BILLIARD_ROOM}};
    for (String[] roomNames : hallways) {
      Hallway hallway = new Hallway(roomNames[0], roomNames[1]);
    }
  }

  public static Hallway get(String end1, String end2) {
    Hallway try1 = hallways.get(end1 + "_" + end2);
    if (try1 != null) {
      return try1;
    } else {
      return hallways.get(end2 + "_" + end1);
    }
  }

  public static List<Hallway> getAll() {
    return new ArrayList<Hallway>(hallways.values());
  }

  private String end1, end2;

  public Hallway(String end1, String end2) {
    this.end1 = end1;
    this.end2 = end2;
    hallways.put(end1 + "_" + end2, this);
  }

  public String getEnd1() {
    return end1;
  }

  public String getEnd2() {
    return end2;
  }

  @Override
  public String toString() {
    return end1 + "_" + end2;
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
