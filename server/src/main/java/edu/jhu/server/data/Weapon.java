package edu.jhu.server.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Weapon implements ICard, IBoardPiece {
	
  public static final Weapon ROPE = new Weapon("Rope");
  public static final Weapon LEAD_PIPE = new Weapon("Lead Pipe");
  public static final Weapon KNIFE = new Weapon("Knife");
  public static final Weapon WRENCH = new Weapon("Wrench");
  public static final Weapon CANDLESTICK = new Weapon("Candlestick");
  public static final Weapon REVOLVER = new Weapon("Revolver");

  private static final Map<String, Weapon> weapons = new HashMap<>();

  static {
    final Weapon[] weaponList = {ROPE, LEAD_PIPE, KNIFE, WRENCH, CANDLESTICK, REVOLVER};
    
    for (Weapon weapon : weaponList) {
      weapons.put(weapon.getName(), weapon);
    }
  }

  public static Weapon get(String name) {
  	if (name == null || name.isEmpty())
  		throw new IllegalArgumentException("name was null or empty");
  	
    return weapons.get(name);
  }

  public static Collection<Weapon> getAll() {
    return Collections.unmodifiableCollection(weapons.values());
  }

  private String name;

  private Weapon(String name) {
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
    Weapon other = (Weapon) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
