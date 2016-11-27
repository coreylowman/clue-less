package edu.jhu.server;

import java.util.ArrayList;
import java.util.List;

import edu.jhu.server.data.CaseFile;
import edu.jhu.server.data.ICard;
import edu.jhu.server.data.Room;
import edu.jhu.server.data.Suspect;
import edu.jhu.server.data.Weapon;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class CardShufflerTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public CardShufflerTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(CardShufflerTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  public void testApp() {
    assertTrue(true);
  }

  private void testDealing(int numPlayers) {
    List<Player> players = new ArrayList<Player>();
    for (int i = 0; i < numPlayers; i++) {
      players.add(new Player("player" + i));
    }

    CaseFile caseFile = CardShuffler.shuffleAndDealCards(players);

    // test the player hands are more than numCards / numPlayers
    int numCards = Room.getAll().size() + Weapon.getAll().size() + Suspect.getAll().size() - 3;
    int target = numCards / numPlayers;
    for (Player player : players) {
      assert (player.getCards().size() >= target);
    }

    // test that no player has case file cards
    for (Player player : players) {
      assertFalse(player.hasCard(caseFile.getRoom()));
      assertFalse(player.hasCard(caseFile.getWeapon()));
      assertFalse(player.hasCard(caseFile.getSuspect()));
    }

    // test each player has different cards
    for (int i = 0; i < players.size(); i++) {
      for (int j = i + 1; j < players.size(); j++) {
        Player player1 = players.get(i);
        Player player2 = players.get(j);
        for (ICard card : player1.getCards()) {
          assertFalse(player2.hasCard(card));
        }
      }
    }
  }

  public void testCardShuffler() {
    Board board = new Board();

    for (int i = 2; i < 7; i++) {
      testDealing(i);
    }
  }
}
