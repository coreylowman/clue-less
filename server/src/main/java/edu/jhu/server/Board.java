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
      final Room room1 = hallway.getEnd1();
      final Room room2 = hallway.getEnd2();
      connectedLocations.get(hallway).add(room1);
      connectedLocations.get(hallway).add(room2);
      connectedLocations.get(room1).add(hallway);
      connectedLocations.get(room2).add(hallway);
    }

    // connect the two secret passage ways
    Room[][] secretPassages = {{Room.LOUNGE, Room.CONSERVATORY}, {Room.STUDY, Room.KITCHEN}};
    for (Room[] rooms : secretPassages) {
      connectedLocations.get(rooms[0]).add(rooms[1]);
      connectedLocations.get(rooms[1]).add(rooms[0]);
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
    final Suspect[] suspects = {Suspect.COLONEL_MUSTARD, Suspect.MISS_SCARLET, Suspect.PROFESSOR_PLUM,
    		Suspect.MR_GREEN, Suspect.MRS_WHITE, Suspect.MRS_PEACOCK};
    final Hallway[] hallways = {Hallway.get(Room.LOUNGE, Room.DINING_ROOM),
        Hallway.get(Room.HALL, Room.LOUNGE), Hallway.get(Room.STUDY, Room.LIBRARY),
        Hallway.get(Room.CONSERVATORY, Room.BALLROOM), Hallway.get(Room.BALLROOM, Room.KITCHEN),
        Hallway.get(Room.LIBRARY, Room.CONSERVATORY)};
    
    for (int i = 0; i < suspects.length; i++) {
      pieces.put(suspects[i], hallways[i]);
      locations.get(hallways[i]).add(suspects[i]);
    }
  }

  // weapons are just put into rooms at the start
  private void initializeWeapons() {
    final Room[] rooms = {Room.HALL, Room.LOUNGE, Room.DINING_ROOM, Room.KITCHEN, Room.BALLROOM,
        Room.CONSERVATORY, Room.BILLIARD_ROOM, Room.LIBRARY, Room.STUDY};
    final Weapon[] weapons = {Weapon.ROPE, Weapon.LEAD_PIPE, Weapon.KNIFE, Weapon.WRENCH,
        Weapon.CANDLESTICK, Weapon.PISTOL};

    for (int i = 0; i < weapons.length; i++) {
      pieces.put(weapons[i], rooms[i]);
      locations.get(rooms[i]).add(weapons[i]);
    }
  }
}
