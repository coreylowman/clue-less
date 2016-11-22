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
    Suspect other = (Suspect) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
