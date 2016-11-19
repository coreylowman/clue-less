package edu.jhu.server.data;

import java.util.HashMap;
import java.util.Map;

public class Suspect implements IBoardPiece, ICard {
  private static Map<String, Suspect> suspects = new HashMap<String, Suspect>();

  public static Suspect get(String name) {
    return suspects.get(name);
  }

  private String name;

  public Suspect(String name) {
    suspects.put(name, this);
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
