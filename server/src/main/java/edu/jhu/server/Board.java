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
    for (String roomName : Room.NAMES) {
      Room room = new Room(roomName);
      locations.put(room, new ArrayList<IBoardPiece>());
      connectedLocations.put(room, new ArrayList<ILocation>());
    }

    // initialize hallways
    String[][] hallways = {{Room.STUDY, Room.LIBRARY}, {Room.STUDY, Room.HALL},
        {Room.HALL, Room.BILLIARD_ROOM}, {Room.HALL, Room.LOUNGE}, {Room.LOUNGE, Room.DINING_ROOM},
        {Room.DINING_ROOM, Room.BILLIARD_ROOM}, {Room.DINING_ROOM, Room.KITCHEN},
        {Room.KITCHEN, Room.BALLROOM}, {Room.BALLROOM, Room.BILLIARD_ROOM},
        {Room.BALLROOM, Room.CONSERVATORY}, {Room.CONSERVATORY, Room.LIBRARY},
        {Room.LIBRARY, Room.BILLIARD_ROOM}};
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
    String[][] secretPassages = {{Room.LOUNGE, Room.CONSERVATORY}, {Room.STUDY, Room.KITCHEN}};
    for (String[] passage : secretPassages) {
      Room room1 = Room.get(passage[0]);
      Room room2 = Room.get(passage[1]);
      connectedLocations.get(room1).add(room2);
      connectedLocations.get(room2).add(room1);
    }

    String[] suspects = {"colonel_mustard", "miss_scarlet", "professor_plum", "mr_green",
        "mrs_white", "mrs_peacock"};
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
    String[][] initialPositions = {{"colonel_mustard", Room.LOUNGE, Room.DINING_ROOM},
        {"miss_scarlet", Room.HALL, Room.LOUNGE}, {"professor_plum", Room.STUDY, Room.LIBRARY},
        {"mr_green", Room.CONSERVATORY, Room.BALLROOM}, {"mrs_white", Room.BALLROOM, Room.KITCHEN},
        {"mrs_peacock", Room.LIBRARY, Room.CONSERVATORY}};
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
    String[] weapons = {"rope", "lead_pipe", "knife", "wrench", "candlestick", "pistol"};

    for (int i = 0; i < weapons.length; i++) {
      Weapon weapon = Weapon.get(weapons[i]);
      Room room = Room.get(rooms[i]);
      pieces.put(weapon, room);
      locations.get(room).add(weapon);
    }
  }
}
