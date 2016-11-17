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
}
