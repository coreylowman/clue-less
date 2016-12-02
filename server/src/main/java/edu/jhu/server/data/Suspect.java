package edu.jhu.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Suspect implements IBoardPiece, ICard {
  public static final String COLONEL_MUSTARD = "Colonel Mustard";
  public static final String MISS_SCARLET = "Miss Scarlet";
  public static final String PROFESSOR_PLUM = "Professor Plum";
  public static final String MR_GREEN = "Mr. Green";
  public static final String MRS_WHITE = "Mrs. White";
  public static final String MRS_PEACOCK = "Mrs. Peacock";

  private static Map<String, Suspect> suspects;

  static {
    suspects = new HashMap<String, Suspect>();
    String[] names =
        {COLONEL_MUSTARD, MISS_SCARLET, PROFESSOR_PLUM, MR_GREEN, MRS_WHITE, MRS_PEACOCK};
    for (String suspectName : names) {
      Suspect suspect = new Suspect(suspectName);
    }
  }

  public static Suspect get(String name) {
    return suspects.get(name);
  }

  public static List<Suspect> getAll() {
    return new ArrayList<Suspect>(suspects.values());
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
