package edu.jhu.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.ICard;
import edu.jhu.server.data.Room;
import edu.jhu.server.data.Suspect;
import edu.jhu.server.data.Weapon;

public class CardShuffler {
  public static Random random = new Random();

  public static CaseFile shuffleAndDealCards(List<Player> players) {
    List<Suspect> suspects = new ArrayList<>(Suspect.getAll());
    List<Weapon> weapons = new ArrayList<>(Weapon.getAll());
    List<Room> rooms = new ArrayList<>(Room.getAll());

    // get secret cards
    Suspect secretSuspect = suspects.remove(random.nextInt(suspects.size()));
    Weapon secretWeapon = weapons.remove(random.nextInt(weapons.size()));
    Room secretRoom = rooms.remove(random.nextInt(rooms.size()));
    CaseFile secretCards = new CaseFile(secretRoom, secretSuspect, secretWeapon);

    // shuffle the rest of the cards
    Stack<ICard> cards = new Stack<ICard>();
    cards.addAll(suspects);
    cards.addAll(weapons);
    cards.addAll(rooms);
    Collections.shuffle(cards, random);

    // initialize hands of each player
    List<List<ICard>> hands = new ArrayList<List<ICard>>(players.size());
    for (int i = 0; i < players.size(); i++) {
      hands.add(new ArrayList<ICard>());
    }

    // deal cards into each hand
    int i = 0;
    while (cards.size() > 0) {
      hands.get(i).add(cards.pop());
      i = (i + 1) % players.size();
    }

    for (i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      player.setCards(hands.get(i));
    }

    return secretCards;
  }
}
