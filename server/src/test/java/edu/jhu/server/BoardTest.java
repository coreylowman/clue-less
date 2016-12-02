package edu.jhu.server;

import java.util.ArrayList;
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

    List<Suspect> suspects = new ArrayList<>(Suspect.getAll());
    for (int i = 0; i < suspects.size(); i++) {
      ILocation location = board.getLocationOf(suspects.get(i));
      assert (location instanceof Hallway);
      for (int j = i + 1; j < suspects.size(); j++) {
        ILocation otherLocation = board.getLocationOf(suspects.get(j));
        assert (!otherLocation.equals(location));
      }
    }

    assert (board.getLocationOf(Suspect.COLONEL_MUSTARD) == Hallway.get(Room.LOUNGE, Room.DINING_ROOM));
    assert (board.getLocationOf(Suspect.MISS_SCARLET) == Hallway.get(Room.LOUNGE, Room.HALL));
    assert (board.getLocationOf(Suspect.PROFESSOR_PLUM) == Hallway.get(Room.STUDY,Room.LIBRARY));
    assert (board.getLocationOf(Suspect.MR_GREEN) == Hallway.get(Room.CONSERVATORY, Room.BALLROOM));
    assert (board.getLocationOf(Suspect.MRS_WHITE) == Hallway.get(Room.BALLROOM, Room.KITCHEN));
    assert (board.getLocationOf(Suspect.MRS_PEACOCK) == Hallway.get(Room.LIBRARY, Room.CONSERVATORY));

    for (Weapon weapon : Weapon.getAll()) {
      ILocation location = board.getLocationOf(weapon);
      assert (location instanceof Room);
    }
  }

  public void testSecretPassageway() {
    Board board = new Board();
    board.initialize();

    // lounge-conservatory
    Suspect suspect = Suspect.MISS_SCARLET;
    List<ILocation> moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.HALL));
    assert (moves.contains(Room.LOUNGE));
    assert (board.isMoveValid(suspect, Room.HALL));
    assert (board.isMoveValid(suspect, Room.LOUNGE));
    board.movePiece(suspect, Room.LOUNGE);
    assert (board.getLocationOf(suspect) == Room.LOUNGE);

    // move to conservatory
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.CONSERVATORY));
    assert (moves.contains(Hallway.get(Room.HALL, Room.LOUNGE)));
    assertFalse(moves.contains(Hallway.get(Room.DINING_ROOM, Room.LOUNGE)));
    assert (board.isMoveValid(suspect, Room.CONSERVATORY));
    assert (board.isMoveValid(suspect, Hallway.get(Room.HALL, Room.LOUNGE)));
    assertFalse(board.isMoveValid(suspect, Hallway.get(Room.DINING_ROOM, Room.LOUNGE)));
    board.movePiece(suspect, Room.CONSERVATORY);
    assert (board.getLocationOf(suspect) == Room.CONSERVATORY);

    // move back to lounge
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.LOUNGE));
    assert (moves.size() == 1);
    assert (board.isMoveValid(suspect, Room.LOUNGE));
    board.movePiece(suspect, Room.LOUNGE);
    assert (board.getLocationOf(suspect) == Room.LOUNGE);


    // kitchen-study
    suspect = Suspect.MRS_WHITE;
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.BALLROOM));
    assert (moves.contains(Room.KITCHEN));
    assert (board.isMoveValid(suspect, Room.BALLROOM));
    assert (board.isMoveValid(suspect, Room.KITCHEN));
    board.movePiece(suspect, Room.KITCHEN);
    assert (board.getLocationOf(suspect) == Room.KITCHEN);

    // move to study
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.STUDY));
    assert (moves.contains(Hallway.get(Room.KITCHEN, Room.DINING_ROOM)));
    assert (moves.contains(Hallway.get(Room.KITCHEN, Room.BALLROOM)));
    assert (board.isMoveValid(suspect, Room.STUDY));
    assert (board.isMoveValid(suspect, Hallway.get(Room.KITCHEN, Room.DINING_ROOM)));
    assert (board.isMoveValid(suspect, Hallway.get(Room.KITCHEN, Room.BALLROOM)));
    board.movePiece(suspect, Room.STUDY);
    assert (board.getLocationOf(suspect) == Room.STUDY);

    // move back to kitchen
    moves = board.getValidMoves(suspect);
    assert (moves.contains(Room.KITCHEN));
    assert (moves.contains(Hallway.get(Room.STUDY, Room.HALL)));
    assertFalse(moves.contains(Hallway.get(Room.STUDY, Room.LIBRARY)));
    assert (board.isMoveValid(suspect, Room.KITCHEN));
    assert (board.isMoveValid(suspect, Hallway.get(Room.STUDY, Room.HALL)));
    assertFalse(board.isMoveValid(suspect, Hallway.get(Room.STUDY, Room.LIBRARY)));
    board.movePiece(suspect, Room.KITCHEN);
    assert (board.getLocationOf(suspect) == Room.KITCHEN);

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
    testLocationConnections(board, Room.STUDY, Room.KITCHEN,
        Hallway.get(Room.HALL, Room.STUDY), Hallway.get(Room.STUDY, Room.LIBRARY));

    // hall connected to hall-billiardroom, hall-study, hall-lounge
    testLocationConnections(board, Room.HALL, Hallway.get(Room.HALL, Room.BILLIARD_ROOM),
        Hallway.get(Room.HALL, Room.STUDY), Hallway.get(Room.HALL, Room.LOUNGE));

    // lounge connected to lounge-hall, lounge-diningroom, conservatory
    testLocationConnections(board, Room.LOUNGE, Hallway.get(Room.HALL, Room.LOUNGE),
        Hallway.get(Room.LOUNGE, Room.DINING_ROOM), Room.CONSERVATORY);

    // diningroom connected to diningroom-lounge, diningroom-billiardroom, diningroom-kitchen
    testLocationConnections(board, Room.DINING_ROOM,
        Hallway.get(Room.LOUNGE, Room.DINING_ROOM),
        Hallway.get(Room.DINING_ROOM, Room.BILLIARD_ROOM),
        Hallway.get(Room.DINING_ROOM, Room.KITCHEN));

    // kitchen connected to kitchen-ballroom, study, kitchen-diningroom
    testLocationConnections(board, Room.KITCHEN, Hallway.get(Room.KITCHEN, Room.BALLROOM),
        Room.STUDY, Hallway.get(Room.KITCHEN, Room.DINING_ROOM));

    // ballroom connected to ballroom-billiardroom, ballroom-kitchen, ballroom-conservatory
    testLocationConnections(board, Room.BALLROOM,
        Hallway.get(Room.BALLROOM, Room.BILLIARD_ROOM), Hallway.get(Room.BALLROOM, Room.KITCHEN),
        Hallway.get(Room.BALLROOM, Room.CONSERVATORY));

    // conservatory connected to conservatory-library, conservatory-ballroom, lounge
    testLocationConnections(board, Room.CONSERVATORY,
        Hallway.get(Room.CONSERVATORY, Room.LIBRARY), Room.LOUNGE,
        Hallway.get(Room.CONSERVATORY, Room.BALLROOM));

    // library connected to library-study, library-billiardroom, library-conservatory
    testLocationConnections(board, Room.LIBRARY, Hallway.get(Room.LIBRARY, Room.STUDY),
        Hallway.get(Room.LIBRARY, Room.BILLIARD_ROOM),
        Hallway.get(Room.LIBRARY, Room.CONSERVATORY));
  }

  public void testHallwayConnections() {
    Board board = new Board();
    board.initialize();

    for (Hallway hallway : Hallway.getAll()) {
      testLocationConnections(board, hallway, hallway.getEnd1(), hallway.getEnd2());
    }
  }
}
