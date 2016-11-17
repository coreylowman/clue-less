package edu.jhu.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.jhu.server.data.IBoardPiece;
import edu.jhu.server.data.ILocation;

public class Board {
  private Map<IBoardPiece, ILocation> pieces;
  private Map<ILocation, List<IBoardPiece>> locations;

  public Board() {
    pieces = new HashMap<IBoardPiece, ILocation>();
    locations = new HashMap<ILocation, List<IBoardPiece>>();
  }

  public boolean isMoveValid(IBoardPiece piece, ILocation destination) {
    return false;
  }

  public ILocation[] getValidMoves(IBoardPiece piece) {
    return null;
  }

  public void movePiece(IBoardPiece piece, ILocation destination) {

  }

  public void initialize() {

  }
}
