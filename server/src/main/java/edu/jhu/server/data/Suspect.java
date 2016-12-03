package edu.jhu.server.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Suspect implements IBoardPiece, ICard {
	
  public static final Suspect COLONEL_MUSTARD = new Suspect("Colonel Mustard");
  public static final Suspect MISS_SCARLET = new Suspect("Miss Scarlet");
  public static final Suspect PROFESSOR_PLUM = new Suspect("Professor Plum");
  public static final Suspect MR_GREEN = new Suspect("Mr. Green");
  public static final Suspect MRS_WHITE = new Suspect("Mrs. White");
  public static final Suspect MRS_PEACOCK = new Suspect("Mrs. Peacock");

  private static final Map<String, Suspect> suspects = new HashMap<>();

  static {
    final Suspect[] suspectList =
        {COLONEL_MUSTARD, MISS_SCARLET, PROFESSOR_PLUM, MR_GREEN, MRS_WHITE, MRS_PEACOCK};
    
    for (Suspect suspect : suspectList) {
      suspects.put(suspect.getName(), suspect);
    }
  }

  public static Suspect get(String name) {
  	if (name == null || name.isEmpty())
  		throw new IllegalArgumentException("name was null or empty");
  	
    return suspects.get(name);
  }

  public static Collection<Suspect> getAll() {
    return Collections.unmodifiableCollection(suspects.values());
  }

  private String name;

  private Suspect(String name) {
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
    Suspect other = (Suspect) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
