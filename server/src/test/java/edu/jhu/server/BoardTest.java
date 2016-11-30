package edu.jhu.server;

import java.util.List;

import edu.jhu.server.data.Hallway;
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
    assert (board.getLocationOf(Suspect.get("colonel_mustard")) == Hallway.get(Room.LOUNGE,
        Room.DINING_ROOM));
    assert (board.getLocationOf(Suspect.get("miss_scarlet")) == Hallway.get(Room.LOUNGE,
        Room.HALL));
    assert (board.getLocationOf(Suspect.get("professor_plum")) == Hallway.get(Room.STUDY,
        Room.LIBRARY));
    assert (board.getLocationOf(Suspect.get("mr_green")) == Hallway.get(Room.CONSERVATORY,
        Room.BALLROOM));
    assert (board.getLocationOf(Suspect.get("mrs_white")) == Hallway.get(Room.BALLROOM,
        Room.KITCHEN));
    assert (board.getLocationOf(Suspect.get("mrs_peacock")) == Hallway.get(Room.LIBRARY,
        Room.CONSERVATORY));

    String[] weapons = {"rope", "lead_pipe", "knife", "wrench", "candlestick", "pistol"};
    for (String weaponName : weapons) {
      ILocation location = board.getLocationOf(Weapon.get(weaponName));
      assert (location instanceof Room);
    }
  }

  public void testSecretPassageway() {
    Board board = new Board();
    board.initialize();

    // lounge-conservatory
    Suspect suspect = Suspect.get("miss_scarlet");
    List<ILocation> moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get(Room.HALL)));
    assert (moves.contains(Room.get(Room.LOUNGE)));
    assert (board.isMoveValid(suspect, Room.get(Room.HALL)));
    assert (board.isMoveValid(suspect, Room.get(Room.LOUNGE)));
    board.movePiece(suspect, Room.get(Room.LOUNGE));
    assert (board.getLocationOf(suspect) == Room.get(Room.LOUNGE));

    // move to conservatory
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get(Room.CONSERVATORY)));
    assert (moves.contains(Hallway.get(Room.HALL, Room.LOUNGE)));
    assertFalse(moves.contains(Hallway.get(Room.DINING_ROOM, Room.LOUNGE)));
    assert (board.isMoveValid(suspect, Room.get(Room.CONSERVATORY)));
    assert (board.isMoveValid(suspect, Hallway.get(Room.HALL, Room.LOUNGE)));
    assertFalse(board.isMoveValid(suspect, Hallway.get(Room.DINING_ROOM, Room.LOUNGE)));
    board.movePiece(suspect, Room.get(Room.CONSERVATORY));
    assert (board.getLocationOf(suspect) == Room.get(Room.CONSERVATORY));

    // move back to lounge
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get(Room.LOUNGE)));
    assert (moves.size() == 1);
    assert (board.isMoveValid(suspect, Room.get(Room.LOUNGE)));
    board.movePiece(suspect, Room.get(Room.LOUNGE));
    assert (board.getLocationOf(suspect) == Room.get(Room.LOUNGE));


    // kitchen-study
    suspect = Suspect.get("mrs_white");
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get(Room.BALLROOM)));
    assert (moves.contains(Room.get(Room.KITCHEN)));
    assert (board.isMoveValid(suspect, Room.get(Room.BALLROOM)));
    assert (board.isMoveValid(suspect, Room.get(Room.KITCHEN)));
    board.movePiece(suspect, Room.get(Room.KITCHEN));
    assert (board.getLocationOf(suspect) == Room.get(Room.KITCHEN));

    // move to study
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get(Room.STUDY)));
    assert (moves.contains(Hallway.get(Room.KITCHEN, Room.DINING_ROOM)));
    assert (moves.contains(Hallway.get(Room.KITCHEN, Room.BALLROOM)));
    assert (board.isMoveValid(suspect, Room.get(Room.STUDY)));
    assert (board.isMoveValid(suspect, Hallway.get(Room.KITCHEN, Room.DINING_ROOM)));
    assert (board.isMoveValid(suspect, Hallway.get(Room.KITCHEN, Room.BALLROOM)));
    board.movePiece(suspect, Room.get(Room.STUDY));
    assert (board.getLocationOf(suspect) == Room.get(Room.STUDY));

    // move back to kitchen
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.get(Room.KITCHEN)));
    assert (moves.contains(Hallway.get(Room.STUDY, Room.HALL)));
    assertFalse(moves.contains(Hallway.get(Room.STUDY, Room.LIBRARY)));
    assert (board.isMoveValid(suspect, Room.get(Room.KITCHEN)));
    assert (board.isMoveValid(suspect, Hallway.get(Room.STUDY, Room.HALL)));
    assertFalse(board.isMoveValid(suspect, Hallway.get(Room.STUDY, Room.LIBRARY)));
    board.movePiece(suspect, Room.get(Room.KITCHEN));
    assert (board.getLocationOf(suspect) == Room.get(Room.KITCHEN));

  }

  private void testLocationConnections(Board board, ILocation location, ILocation... connections) {
    List<ILocation> moves = board.getConnectedLocations(location);
    assert (moves.size() == connections.length);
    for (ILocation connection : connections) {
      assert (moves.contains(connection));
    }
  }

  public void testRoomConnections() {
    Board board = new Board();
    board.initialize();

    // study connected to kitchen, study-hall, study-library
    testLocationConnections(board, Room.get(Room.STUDY), Room.get(Room.KITCHEN),
        Hallway.get(Room.HALL, Room.STUDY), Hallway.get(Room.STUDY, Room.LIBRARY));

    // hall connected to hall-billiardroom, hall-study, hall-lounge
    testLocationConnections(board, Room.get(Room.HALL), Hallway.get(Room.HALL, Room.BILLIARD_ROOM),
        Hallway.get(Room.HALL, Room.STUDY), Hallway.get(Room.HALL, Room.LOUNGE));

    // lounge connected to lounge-hall, lounge-diningroom, conservatory
    testLocationConnections(board, Room.get(Room.LOUNGE), Hallway.get(Room.HALL, Room.LOUNGE),
        Hallway.get(Room.LOUNGE, Room.DINING_ROOM), Room.get(Room.CONSERVATORY));

    // diningroom connected to diningroom-lounge, diningroom-billiardroom, diningroom-kitchen
    testLocationConnections(board, Room.get(Room.DINING_ROOM),
        Hallway.get(Room.LOUNGE, Room.DINING_ROOM),
        Hallway.get(Room.DINING_ROOM, Room.BILLIARD_ROOM),
        Hallway.get(Room.DINING_ROOM, Room.KITCHEN));

    // kitchen connected to kitchen-ballroom, study, kitchen-diningroom
    testLocationConnections(board, Room.get(Room.KITCHEN), Hallway.get(Room.KITCHEN, Room.BALLROOM),
        Room.get(Room.STUDY), Hallway.get(Room.KITCHEN, Room.DINING_ROOM));

    // ballroom connected to ballroom-billiardroom, ballroom-kitchen, ballroom-conservatory
    testLocationConnections(board, Room.get(Room.BALLROOM),
        Hallway.get(Room.BALLROOM, Room.BILLIARD_ROOM), Hallway.get(Room.BALLROOM, Room.KITCHEN),
        Hallway.get(Room.BALLROOM, Room.CONSERVATORY));

    // conservatory connected to conservatory-library, conservatory-ballroom, lounge
    testLocationConnections(board, Room.get(Room.CONSERVATORY),
        Hallway.get(Room.CONSERVATORY, Room.LIBRARY), Room.get(Room.LOUNGE),
        Hallway.get(Room.CONSERVATORY, Room.BALLROOM));

    // library connected to library-study, library-billiardroom, library-conservatory
    testLocationConnections(board, Room.get(Room.LIBRARY), Hallway.get(Room.LIBRARY, Room.STUDY),
        Hallway.get(Room.LIBRARY, Room.BILLIARD_ROOM),
        Hallway.get(Room.LIBRARY, Room.CONSERVATORY));
  }

  public void testHallwayConnections() {
    Board board = new Board();
    board.initialize();

    String[][] connections = {{Room.STUDY, Room.HALL}, {Room.HALL, Room.LOUNGE},
        {Room.STUDY, Room.LIBRARY}, {Room.HALL, Room.BILLIARD_ROOM},
        {Room.LOUNGE, Room.DINING_ROOM}, {Room.LIBRARY, Room.BILLIARD_ROOM},
        {Room.BILLIARD_ROOM, Room.DINING_ROOM}, {Room.LIBRARY, Room.CONSERVATORY},
        {Room.BILLIARD_ROOM, Room.BALLROOM}, {Room.DINING_ROOM, Room.KITCHEN},
        {Room.CONSERVATORY, Room.BALLROOM}, {Room.BALLROOM, Room.KITCHEN}};

    for (String[] roomConnection : connections) {
      testLocationConnections(board, Hallway.get(roomConnection[0], roomConnection[1]),
          Room.get(roomConnection[0]), Room.get(roomConnection[1]));
    }
  }
}
