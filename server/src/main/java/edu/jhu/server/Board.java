package edu.jhu.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.jhu.server.data.Hallway;
import edu.jhu.server.data.IBoardPiece;
import edu.jhu.server.data.ILocation;
import edu.jhu.server.data.Room;
import edu.jhu.server.data.Suspect;
import edu.jhu.server.data.Weapon;

public class Board {
  private Map<IBoardPiece, ILocation> pieces;
  private Map<ILocation, List<IBoardPiece>> locations;
  private Map<ILocation, List<ILocation>> connectedLocations;

  public Board() {
    pieces = new HashMap<IBoardPiece, ILocation>();
    locations = new HashMap<ILocation, List<IBoardPiece>>();
    connectedLocations = new HashMap<ILocation, List<ILocation>>();

    for (Room room : Room.getAll()) {
      locations.put(room, new ArrayList<IBoardPiece>());
      connectedLocations.put(room, new ArrayList<ILocation>());
    }

    for (Hallway hallway : Hallway.getAll()) {
      locations.put(hallway, new ArrayList<IBoardPiece>());
      connectedLocations.put(hallway, new ArrayList<ILocation>());
    }

    // connect the hallways & rooms
    for (Hallway hallway : Hallway.getAll()) {
      Room room1 = Room.get(hallway.getEnd1());
      Room room2 = Room.get(hallway.getEnd2());
      connectedLocations.get(hallway).add(room1);
      connectedLocations.get(hallway).add(room2);
      connectedLocations.get(room1).add(hallway);
      connectedLocations.get(room2).add(hallway);
    }

    // connect the two secret passage ways
    String[][] secretPassages = {{Room.LOUNGE, Room.CONSERVATORY}, {Room.STUDY, Room.KITCHEN}};
    for (String[] passage : secretPassages) {
      Room room1 = Room.get(passage[0]);
      Room room2 = Room.get(passage[1]);
      connectedLocations.get(room1).add(room2);
      connectedLocations.get(room2).add(room1);
    }
  }

  public boolean isMoveValid(IBoardPiece piece, ILocation destination) {
    return getValidMoves(piece).contains(destination);
  }

  public List<ILocation> getValidMoves(IBoardPiece piece) {
    List<ILocation> moves = new ArrayList<ILocation>(connectedLocations.get(pieces.get(piece)));

    if (piece instanceof Weapon) {
      // weapons can't be moved into hallways
      moves.removeIf(loc -> loc instanceof Hallway);
    } else {
      // remove any destinations if they are hallways with more than 1 person in them
      moves.removeIf(loc -> loc instanceof Hallway && locations.get(loc).size() != 0);
    }

    return moves;
  }

  public ILocation getLocationOf(IBoardPiece piece) {
    return pieces.get(piece);
  }

  public List<ILocation> getConnectedLocations(ILocation location) {
    return connectedLocations.get(location);
  }

  public void movePiece(IBoardPiece piece, ILocation destination) {
    ILocation oldLocation = pieces.get(piece);
    pieces.put(piece, destination);
    locations.get(oldLocation).remove(piece);
    locations.get(destination).add(piece);
  }

  public void initialize() {
    initializeSuspects();
    initializeWeapons();
  }

  // initial positions are detailed in the project description document at the end
  private void initializeSuspects() {
    String[][] initialPositions = {{Suspect.COLONEL_MUSTARD, Room.LOUNGE, Room.DINING_ROOM},
        {Suspect.MISS_SCARLET, Room.HALL, Room.LOUNGE},
        {Suspect.PROFESSOR_PLUM, Room.STUDY, Room.LIBRARY},
        {Suspect.MR_GREEN, Room.CONSERVATORY, Room.BALLROOM},
        {Suspect.MRS_WHITE, Room.BALLROOM, Room.KITCHEN},
        {Suspect.MRS_PEACOCK, Room.LIBRARY, Room.CONSERVATORY}};
    for (String[] location : initialPositions) {
      Suspect suspect = Suspect.get(location[0]);
      Hallway hallway = Hallway.get(location[1], location[2]);
      pieces.put(suspect, hallway);
      locations.get(hallway).add(suspect);

    }
  }

  // weapons are just put into rooms at the start
  private void initializeWeapons() {
    String[] rooms = {Room.HALL, Room.LOUNGE, Room.DINING_ROOM, Room.KITCHEN, Room.BALLROOM,
        Room.CONSERVATORY, Room.BILLIARD_ROOM, Room.LIBRARY, Room.STUDY};
    String[] weapons = {Weapon.ROPE, Weapon.LEAD_PIPE, Weapon.KNIFE, Weapon.WRENCH,
        Weapon.CANDLESTICK, Weapon.PISTOL};

    for (int i = 0; i < weapons.length; i++) {
      Weapon weapon = Weapon.get(weapons[i]);
      Room room = Room.get(rooms[i]);
      pieces.put(weapon, room);
      locations.get(room).add(weapon);
    }
  }
}
