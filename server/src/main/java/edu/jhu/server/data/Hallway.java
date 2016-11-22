package edu.jhu.server.data;

import java.util.HashMap;
import java.util.Map;

public class Hallway implements ILocation {
  private static Map<String, Hallway> hallways = new HashMap<String, Hallway>();

  public static Hallway get(String end1, String end2) {
    return hallways.get(end1 + "_" + end2);
  }

  private String end1, end2;

  public Hallway(String end1, String end2) {
    this.end1 = end1;
    this.end2 = end2;
    hallways.put(end1 + "_" + end2, this);
    hallways.put(end2 + "_" + end1, this);
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
