package edu.jhu.server.data;

public class Room implements ILocation, ICard {
  public static final Room HALL =
      new Room("hall", Hallway.STUDY_HALL, Hallway.HALL_BILLIARDROOM, Hallway.HALL_LOUNGE);
  public static final Room LOUNGE = new Room("lounge", Hallway.LOUNGE_CONSERVATORY,
      Hallway.LOUNGE_DININGROOM, Hallway.HALL_LOUNGE);
  public static final Room DINING_ROOM = new Room("diningroom", Hallway.DININGROOM_BILLIARDROOM,
      Hallway.DININGROOM_KITCHEN, Hallway.LOUNGE_DININGROOM);
  public static final Room KITCHEN = new Room("kitchen", Hallway.KITCHEN_BALLROOM,
      Hallway.DININGROOM_KITCHEN, Hallway.STUDY_KITCHEN);
  public static final Room BALLROOM = new Room("ballroom", Hallway.BALLROOM_BILLIARDROOM,
      Hallway.BALLROOM_CONSERVATORY, Hallway.KITCHEN_BALLROOM);
  public static final Room CONSERVATORY = new Room("conservatory", Hallway.CONSERVATORY_LIBRARY,
      Hallway.BALLROOM_CONSERVATORY, Hallway.LOUNGE_CONSERVATORY);
  public static final Room BILLIARD_ROOM = new Room("billiardroom", Hallway.BALLROOM_BILLIARDROOM,
      Hallway.DININGROOM_BILLIARDROOM, Hallway.HALL_BILLIARDROOM, Hallway.LIBRARY_BILLIARDROOM);
  public static final Room LIBRARY = new Room("library", Hallway.LIBRARY_BILLIARDROOM,
      Hallway.CONSERVATORY_LIBRARY, Hallway.STUDY_LIBRARY);
  public static final Room STUDY =
      new Room("study", Hallway.STUDY_HALL, Hallway.STUDY_KITCHEN, Hallway.STUDY_LIBRARY);

  private String name;
  private Hallway[] hallways;

  public Room(String name, Hallway... hallways) {
    this.name = name;
    this.hallways = hallways;
  }

  public ILocation[] getConnectedLocations() {
    return hallways;
  }

  @Override
  public String toString() {
    return name;
  }
}
