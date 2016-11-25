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
    String[][] hallways = {{"study", "library"}, {"study", "kitchen"}, {"study", "hall"},
        {"hall", "billiardroom"}, {"hall", "lounge"}, {"lounge", "conservatory"},
        {"lounge", "diningroom"}, {"diningroom", "billiardroom"}, {"diningroom", "kitchen"},
        {"kitchen", "ballroom"}, {"ballroom", "billiardroom"}, {"ballroom", "conservatory"},
        {"conservatory", "library"}, {"library", "billiardroom"}};
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
  
  // Function needs to actually check if player's piece is in a room
  public boolean isSuggestionValid(Player suggester) {
	  return false;
  }
  
  // currently a dummy function, actually needs to get a room according to suspect
  public Room getPieceLocation(Suspect suspect){
	  return Room.get("hall");
  }

  public boolean isMoveValid(IBoardPiece piece, ILocation destination) {
    return getValidMoves(piece).contains(destination);
  }

  public List<ILocation> getValidMoves(IBoardPiece piece) {
    return connectedLocations.get(pieces.get(piece));
  }

  public void movePiece(IBoardPiece piece, ILocation destination) {

  }

  public void initialize() {

  }
}
