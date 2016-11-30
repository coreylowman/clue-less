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

    // initialize rooms
    String[] rooms = {"hall", "lounge", "diningroom", "kitchen", "ballroom", "conservatory",
        "billiardroom", "library", "study"};
    for (String roomName : rooms) {
      Room room = new Room(roomName);
      locations.put(room, new ArrayList<IBoardPiece>());
      connectedLocations.put(room, new ArrayList<ILocation>());
    }

    // initialize hallways
    String[][] hallways = {{"study", "library"}, {"study", "hall"}, {"hall", "billiardroom"},
        {"hall", "lounge"}, {"lounge", "diningroom"}, {"diningroom", "billiardroom"},
        {"diningroom", "kitchen"}, {"kitchen", "ballroom"}, {"ballroom", "billiardroom"},
        {"ballroom", "conservatory"}, {"conservatory", "library"}, {"library", "billiardroom"}};
    for (String[] roomNames : hallways) {
      Hallway hallway = new Hallway(roomNames[0], roomNames[1]);
      locations.put(hallway, new ArrayList<IBoardPiece>());
      connectedLocations.put(hallway, new ArrayList<ILocation>());
    }

    // connect the hallways & rooms
    for (String[] roomNames : hallways) {
      Hallway hallway = Hallway.get(roomNames[0], roomNames[1]);
      Room room1 = Room.get(roomNames[0]);
      Room room2 = Room.get(roomNames[1]);
      connectedLocations.get(hallway).add(room1);
      connectedLocations.get(hallway).add(room2);
      connectedLocations.get(room1).add(hallway);
      connectedLocations.get(room2).add(hallway);
    }

    // connect the two secret passage ways
    String[][] secretPassages = {{"lounge", "conservatory"}, {"study", "kitchen"}};
    for (String[] passage : secretPassages) {
      Room room1 = Room.get(passage[0]);
      Room room2 = Room.get(passage[1]);
      connectedLocations.get(room1).add(room2);
      connectedLocations.get(room2).add(room1);
    }

    String[] suspects = {"colonel_mustard", "miss_scarlet", "professor_plum", "mr_green",
        "mrs_white", "Mrs. Peacock"};
    for (String name : suspects) {
      Suspect suspect = new Suspect(name);
    }

    String[] weapons = {"rope", "lead_pipe", "knife", "wrench", "candlestick", "pistol"};
    for (String name : weapons) {
      Weapon weapon = new Weapon(name);
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
    String[][] initialPositions =
        {{"colonel_mustard", "lounge", "diningroom"}, {"miss_scarlet", "hall", "lounge"},
            {"professor_plum", "study", "library"}, {"mr_green", "conservatory", "ballroom"},
            {"mrs_white", "ballroom", "kitchen"}, {"Mrs. Peacock", "library", "conservatory"}};
    for (String[] location : initialPositions) {
      Suspect suspect = Suspect.get(location[0]);
      Hallway hallway = Hallway.get(location[1], location[2]);
      pieces.put(suspect, hallway);
      locations.get(hallway).add(suspect);

    }
  }

  // weapons are just put into rooms at the start
  private void initializeWeapons() {
    String[] rooms = {"hall", "lounge", "diningroom", "kitchen", "ballroom", "conservatory",
        "billiardroom", "library", "study"};
    String[] weapons = {"rope", "lead_pipe", "knife", "wrench", "candlestick", "pistol"};

    for (int i = 0; i < weapons.length; i++) {
      Weapon weapon = Weapon.get(weapons[i]);
      Room room = Room.get(rooms[i]);
      pieces.put(weapon, room);
      locations.get(room).add(weapon);
    }
  }
}
