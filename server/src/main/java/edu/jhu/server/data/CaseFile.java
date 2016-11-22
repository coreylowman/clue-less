package edu.jhu.server.data;

public class CaseFile {
  private Room room;
  private Suspect suspect;
  private Weapon weapon;

  public CaseFile(Room room, Suspect suspect, Weapon weapon) {
    this.room = room;
    this.suspect = suspect;
    this.weapon = weapon;
  }

  public Room getRoom() {
    return room;
  }

  public Suspect getSuspect() {
    return suspect;
  }

  public Weapon getWeapon() {
    return weapon;
  }

  // auto-genned
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((room == null) ? 0 : room.hashCode());
    result = prime * result + ((suspect == null) ? 0 : suspect.hashCode());
    result = prime * result + ((weapon == null) ? 0 : weapon.hashCode());
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
    CaseFile other = (CaseFile) obj;
    if (room == null) {
      if (other.room != null)
        return false;
    } else if (!room.equals(other.room))
      return false;
    if (suspect == null) {
      if (other.suspect != null)
        return false;
    } else if (!suspect.equals(other.suspect))
      return false;
    if (weapon == null) {
      if (other.weapon != null)
        return false;
    } else if (!weapon.equals(other.weapon))
      return false;
    return true;
  }
}
