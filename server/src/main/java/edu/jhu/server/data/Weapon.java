package edu.jhu.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Weapon implements ICard, IBoardPiece {
  private static Map<String, Weapon> weapons = new HashMap<String, Weapon>();

  public static Weapon get(String name) {
    return weapons.get(name);
  }

  public static List<Weapon> getAll() {
    return new ArrayList<Weapon>(weapons.values());
  }

  private String name;

  public Weapon(String name) {
    weapons.put(name, this);
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
