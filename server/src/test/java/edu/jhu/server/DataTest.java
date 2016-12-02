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
    Room room1 = new Room("billiards");
    Room room2 = new Room("asd;f_");

    assertFalse(room1.equals(room2));
    assertFalse(room1.equals(null));

    Room room3 = new Room("billiards");
    assertTrue(room3.equals(room1));
    assertTrue(room3.equals(Room.get("billiards")));
    assertTrue(room1.equals(Room.get("billiards")));

    assertTrue(Room.get("laskdjf;aosi") == null);
  }

  public void testSuspect() {
    Suspect suspsect1 = new Suspect("billiards");
    Suspect suspect2 = new Suspect("asd;f_");

    assertFalse(suspsect1.equals(suspect2));
    assertFalse(suspsect1.equals(null));

    Suspect suspect3 = new Suspect("billiards");
    assertTrue(suspect3.equals(suspsect1));
    assertTrue(suspect3.equals(Suspect.get("billiards")));
    assertTrue(suspsect1.equals(Suspect.get("billiards")));

    assertTrue(Suspect.get("laksjd;f") == null);
  }

  public void testWeapon() {
    Weapon weapon1 = new Weapon("billiards");
    Weapon weapon2 = new Weapon("asd;f_");

    assertFalse(weapon1.equals(weapon2));
    assertFalse(weapon1.equals(null));

    Weapon weapon3 = new Weapon("billiards");
    assertTrue(weapon3.equals(weapon1));
    assertTrue(weapon3.equals(Weapon.get("billiards")));
    assertTrue(weapon1.equals(Weapon.get("billiards")));

    assertTrue(Weapon.get("laksjd;f") == null);
  }

  public void testHallway() {
    Hallway hallway1 = new Hallway("billiards", Room.KITCHEN);
    Hallway hallway2 = new Hallway("asd;f_", " ;joiasdjf;");

    assertFalse(hallway1.equals(hallway2));
    assertFalse(hallway1.equals(null));

    Hallway hallway3 = new Hallway(Room.KITCHEN, "billiards");
    assertTrue(hallway3.equals(hallway1));
    assertTrue(hallway3.equals(Hallway.get("billiards", Room.KITCHEN)));
    assertTrue(hallway1.equals(Hallway.get("billiards", Room.KITCHEN)));

    assertTrue(Hallway.get("laksjd;f", "a;lsdjfoiq") == null);
  }

  public void testCaseFile() {
    Suspect suspect1 = new Suspect("suspect1");
    Room room1 = new Room("room1");
    Weapon weapon1 = new Weapon("weapon1");
    CaseFile casefile1 = new CaseFile(room1, suspect1, weapon1);
    CaseFile casefile2 = new CaseFile(room1, suspect1, weapon1);
    assertTrue(casefile1.equals(casefile2));

    CaseFile casefile3 = new CaseFile(room1, new Suspect("suspect2"), weapon1);
    assertFalse(casefile1.equals(casefile3));
  }
}
