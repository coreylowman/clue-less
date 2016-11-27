package edu.jhu.server;

import java.util.List;

import edu.jhu.server.data.Hallway;
import edu.jhu.server.data.IBoardPiece;
import edu.jhu.server.data.ILocation;
import edu.jhu.server.data.Room;
import edu.jhu.server.data.Suspect;
import edu.jhu.server.data.Weapon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BoardTest extends TestCase {
  public BoardTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(BoardTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  public void testApp() {
    assertTrue(true);
  }

  public void testInitialPositions() {
    Board board = new Board();
    board.initialize();

    String[] suspects = {"colonel_mustard", "miss_scarlet", "professor_plum", "mr_green",
        "mrs_white", "mrs_peacock"};
    for (String suspectName : suspects) {
      ILocation location = board.getLocationOf(Suspect.get(suspectName));
      assert (location instanceof Hallway);
    }
    assert (board.getLocationOf(Suspect.get("colonel_mustard")) == Hallway.get("lounge",
        "diningroom"));
    assert (board.getLocationOf(Suspect.get("miss_scarlet")) == Hallway.get("lounge", "hall"));
    assert (board.getLocationOf(Suspect.get("professor_plum")) == Hallway.get("study", "library"));
    assert (board.getLocationOf(Suspect.get("mr_green")) == Hallway.get("conservatory",
        "ballroom"));
    assert (board.getLocationOf(Suspect.get("mrs_white")) == Hallway.get("ballroom", "kitchen"));
    assert (board.getLocationOf(Suspect.get("mrs_peacock")) == Hallway.get("library",
        "conservatory"));

    String[] weapons = {"rope", "lead_pipe", "knife", "wrench", "candlestick", "pistol"};
    for (String weaponName : weapons) {
      ILocation location = board.getLocationOf(Weapon.get(weaponName));
      assert (location instanceof Room);
    }
  }

  public void testSecretPassageway() {
    Board board = new Board();
    board.initialize();

    Suspect suspect = Suspect.get("miss_scarlet");
    List<ILocation> moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get("hall")));
    assert (moves.contains(Room.get("lounge")));
    assert (board.isMoveValid(suspect, Room.get("hall")));
    assert (board.isMoveValid(suspect, Room.get("lounge")));
    board.movePiece(suspect, Room.get("lounge"));
    assert (board.getLocationOf(suspect) == Room.get("lounge"));

    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get("conservatory")));
    assert (moves.contains(Hallway.get("hall", "lounge")));
    assert (moves.contains(Hallway.get("diningroom", "lounge")));
    assert (board.isMoveValid(suspect, Room.get("conservatory")));
    assert (board.isMoveValid(suspect, Hallway.get("hall", "lounge")));
    assert (board.isMoveValid(suspect, Hallway.get("diningroom", "lounge")));
    board.movePiece(suspect, Room.get("conservatory"));
    assert (board.getLocationOf(suspect) == Room.get("conservatory"));
  }

  private void testLocationConnections(Board board, ILocation location, ILocation... connections) {
    IBoardPiece piece = Weapon.get("rope");

    board.movePiece(piece, location);
    assert (board.getLocationOf(piece) == location);

    List<ILocation> moves = board.getValidMoves(piece);
    assert (moves.size() == connections.length);
    for (ILocation connection : connections) {
      assert (moves.contains(connection));
    }
  }

  public void testRoomConnections() {
    Board board = new Board();
    board.initialize();

    // study connected to kitchen, study-hall, study-library
    testLocationConnections(board, Room.get("study"), Room.get("kitchen"),
        Hallway.get("hall", "study"), Hallway.get("study", "library"));

    // hall connected to hall-billiardroom, hall-study, hall-lounge
    testLocationConnections(board, Room.get("hall"), Hallway.get("hall", "billiardroom"),
        Hallway.get("hall", "study"), Hallway.get("hall", "lounge"));

    // lounge connected to lounge-hall, lounge-diningroom, conservatory
    testLocationConnections(board, Room.get("lounge"), Hallway.get("hall", "lounge"),
        Hallway.get("lounge", "diningroom"), Room.get("conservatory"));

    // diningroom connected to diningroom-lounge, diningroom-billiardroom, diningroom-kitchen
    testLocationConnections(board, Room.get("diningroom"), Hallway.get("lounge", "diningroom"),
        Hallway.get("diningroom", "billiardroom"), Hallway.get("diningroom", "kitchen"));

    // kitchen connected to kitchen-ballroom, study, kitchen-diningroom
    testLocationConnections(board, Room.get("kitchen"), Hallway.get("kitchen", "ballroom"),
        Room.get("study"), Hallway.get("kitchen", "diningroom"));

    // ballroom connected to ballroom-billiardroom, ballroom-kitchen, ballroom-conservatory
    testLocationConnections(board, Room.get("ballroom"), Hallway.get("ballroom", "billiardroom"),
        Hallway.get("ballroom", "kitchen"), Hallway.get("ballroom", "conservatory"));

    // conservatory connected to conservatory-library, conservatory-ballroom, lounge
    testLocationConnections(board, Room.get("conservatory"), Hallway.get("conservatory", "library"),
        Room.get("lounge"), Hallway.get("conservatory", "ballroom"));

    // library connected to library-study, library-billiardroom, library-conservatory
    testLocationConnections(board, Room.get("library"), Hallway.get("library", "study"),
        Hallway.get("library", "billiardroom"), Hallway.get("library", "conservatory"));
  }

  public void testHallwayConnections() {
    Board board = new Board();
    board.initialize();

    String[][] connections = {{"study", "hall"}, {"hall", "lounge"}, {"study", "library"},
        {"hall", "billiardroom"}, {"lounge", "diningroom"}, {"library", "billiardroom"},
        {"billiardroom", "diningroom"}, {"library", "conservatory"}, {"billiardroom", "ballroom"},
        {"diningroom", "kitchen"}, {"conservatory", "ballroom"}, {"ballroom", "kitchen"}};

    for (String[] roomConnection : connections) {
      testLocationConnections(board, Hallway.get(roomConnection[0], roomConnection[1]),
          Room.get(roomConnection[0]), Room.get(roomConnection[1]));
    }
  }
}
