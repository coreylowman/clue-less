package edu.jhu.server.data;

public class Hallway implements ILocation {
  public static final Hallway STUDY_LIBRARY = new Hallway(Room.STUDY, Room.LIBRARY);
  public static final Hallway STUDY_KITCHEN = new Hallway(Room.STUDY, Room.KITCHEN);
  public static final Hallway STUDY_HALL = new Hallway(Room.STUDY, Room.HALL);
  public static final Hallway HALL_BILLIARDROOM = new Hallway(Room.HALL, Room.BILLIARD_ROOM);
  public static final Hallway HALL_LOUNGE = new Hallway(Room.HALL, Room.LOUNGE);
  public static final Hallway LOUNGE_CONSERVATORY = new Hallway(Room.LOUNGE, Room.CONSERVATORY);
  public static final Hallway LOUNGE_DININGROOM = new Hallway(Room.LOUNGE, Room.DINING_ROOM);
  public static final Hallway DININGROOM_BILLIARDROOM =
      new Hallway(Room.DINING_ROOM, Room.BILLIARD_ROOM);
  public static final Hallway DININGROOM_KITCHEN = new Hallway(Room.DINING_ROOM, Room.KITCHEN);
  public static final Hallway KITCHEN_BALLROOM = new Hallway(Room.KITCHEN, Room.BALLROOM);
  public static final Hallway BALLROOM_BILLIARDROOM =
      new Hallway(Room.BALLROOM, Room.BILLIARD_ROOM);
  public static final Hallway BALLROOM_CONSERVATORY = new Hallway(Room.BALLROOM, Room.CONSERVATORY);
  public static final Hallway CONSERVATORY_LIBRARY = new Hallway(Room.CONSERVATORY, Room.LIBRARY);
  public static final Hallway LIBRARY_BILLIARDROOM = new Hallway(Room.LIBRARY, Room.BILLIARD_ROOM);

  private Room[] rooms;

  public Hallway(Room... rooms) {
    this.rooms = rooms;
  }

  public ILocation[] getConnectedLocations() {
    return rooms;
  }

  @Override
  public String toString() {
    return rooms[0].toString() + "_" + rooms[1].toString();
  }
}
