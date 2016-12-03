package edu.jhu.server;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.Hallway;
import edu.jhu.server.data.Room;
import edu.jhu.server.data.Suspect;
import edu.jhu.server.data.Weapon;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DataTest extends TestCase {
  public DataTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(DataTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  public void testApp() {
    assertTrue(true);
  }

  public void testRoom() {
    Room room1 = Room.get("Billiard Room");
    Room room2 = Room.get("Library");

    assertFalse(room1.equals(room2));
    assertFalse(room1.equals(null));
    assertFalse(room2.equals(null));

    Room room3 = Room.get("Billiard Room");
    assertTrue(room3.equals(room1));

    assertTrue(Room.get("laskdjf;aosi") == null);
  }

  public void testSuspect() {
    Suspect suspect1 = Suspect.get("Colonel Mustard");
    Suspect suspect2 = Suspect.get("Miss Scarlet");

    assertFalse(suspect1.equals(suspect2));
    assertFalse(suspect1.equals(null));
    assertFalse(suspect2.equals(null));

    Suspect suspect3 = Suspect.get("Colonel Mustard");
    assertTrue(suspect3.equals(suspect1));

    assertTrue(Suspect.get("laksjd;f") == null);
  }

  public void testWeapon() {
    Weapon weapon1 = Weapon.get("Rope");
    Weapon weapon2 = Weapon.get("Knife");

    assertFalse(weapon1.equals(weapon2));
    assertFalse(weapon1.equals(null));
    assertFalse(weapon2.equals(null));

    Weapon weapon3 = Weapon.get("Rope");
    assertTrue(weapon3.equals(weapon1));

    assertTrue(Weapon.get("laksjd;f") == null);
  }

  public void testHallway() {
    Hallway hallway1 = Hallway.get(Room.BALLROOM, Room.KITCHEN);
    Hallway hallway2 = Hallway.get(Room.BILLIARD_ROOM, Room.DINING_ROOM);

    assertFalse(hallway1.equals(hallway2));
    assertFalse(hallway1.equals(null));
    assertFalse(hallway2.equals(null));

    Hallway hallway3 = Hallway.get(Room.BALLROOM, Room.KITCHEN);
    assertTrue(hallway3.equals(hallway1));

    assertTrue(Hallway.get(Room.BILLIARD_ROOM, Room.KITCHEN) == null);
  }

  public void testCaseFile() {
    Suspect suspect1 = Suspect.get("Colonel Mustard");
    Room room1 = Room.get("Billiard Room");
    Weapon weapon1 = Weapon.get("Rope");
    CaseFile casefile1 = new CaseFile(room1, suspect1, weapon1);
    CaseFile casefile2 = new CaseFile(room1, suspect1, weapon1);
    assertFalse(casefile1.equals(null));
    assertTrue(casefile1.equals(casefile2));

    CaseFile casefile3 = new CaseFile(room1, Suspect.get("Miss Scarlet"), weapon1);
    assertFalse(casefile3.equals(null));
    assertFalse(casefile1.equals(casefile3));
  }
}
