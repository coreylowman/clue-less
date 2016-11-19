package edu.jhu.server.data;

import java.util.HashMap;
import java.util.Map;

public class Hallway implements ILocation {
  private static Map<String, Hallway> hallways = new HashMap<String, Hallway>();

  public static Hallway get(String name) {
    return hallways.get(name);
  }

  private String name1, name2;

  public Hallway(String name1, String name2) {
    this.name1 = name1;
    this.name2 = name2;
    hallways.put(name1, this);
    hallways.put(name2, this);
  }

  @Override
  public String toString() {
    return name1;
  }
}
