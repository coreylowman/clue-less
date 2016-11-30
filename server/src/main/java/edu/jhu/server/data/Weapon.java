package edu.jhu.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Weapon implements ICard, IBoardPiece {
  public static final String ROPE = "Rope";
  public static final String LEAD_PIPE = "Lead Pipe";
  public static final String KNIFE = "Knife";
  public static final String WRENCH = "Wrench";
  public static final String CANDLESTICK = "Candlestick";
  public static final String PISTOL = "Pistol";

  private static Map<String, Weapon> weapons;

  static {
    weapons = new HashMap<String, Weapon>();
    String[] names = {ROPE, LEAD_PIPE, KNIFE, WRENCH, CANDLESTICK, PISTOL};
    for (String name : names) {
      Weapon weapon = new Weapon(name);
    }
  }

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
    Weapon other = (Weapon) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
