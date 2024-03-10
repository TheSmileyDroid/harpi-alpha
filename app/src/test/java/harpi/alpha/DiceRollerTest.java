package harpi.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import harpi.alpha.dice.DiceRoller;
import harpi.alpha.dice.RollResult;

class DiceRollerTest {

  @Test
  void testComponentCount() {
    RollResult result = DiceRoller.roll("d6");
    assert result.getComponents().size() == 1;
  }

  @Test
  void testComponentCount2() {
    RollResult result = DiceRoller.roll("1d6+1d6");
    assert result.getComponents().size() == 2;
  }

  @Test
  void testRoll() {
    RollResult result = DiceRoller.roll("d6");
    assert result.getTotal() >= 1 && result.getTotal() <= 6;
  }

  @Test
  void testRoll2() {
    RollResult result = DiceRoller.roll("1d6+1d6");
    assert result.getTotal() >= 2 && result.getTotal() <= 12;
  }

  @Test
  void testNegativeRoll() {
    RollResult result = DiceRoller.roll("-1d6");
    assert result.getTotal() >= -6 && result.getTotal() <= -1;
  }

  @Test
  void testNegativeRoll2() {
    RollResult result = DiceRoller.roll("-1d6+1d6");
    assert result.getTotal() >= -5 && result.getTotal() <= 5;
  }

  @Test
  void testRollWithModifier() {
    RollResult result = DiceRoller.roll("d6+1");
    assert result.getTotal() >= 2 && result.getTotal() <= 7;
  }

  @Test
  void testRollWithModifier2() {
    RollResult result = DiceRoller.roll("1d6+1d6+1");
    assert result.getTotal() >= 3 && result.getTotal() <= 13;
  }

  @Test
  void testNegativeRollWithModifier() {
    RollResult result = DiceRoller.roll("d6-1");
    assert result.getTotal() >= 0 && result.getTotal() <= 5;
  }

  @Test
  void testWithSpaces() {
    RollResult result = DiceRoller.roll("1d6 + 1d6");
    assert result.getTotal() >= 2 && result.getTotal() <= 12;
  }

  @Test
  void testWithSpaces2() {
    RollResult result = DiceRoller.roll("1d6 + 1d6 + 1");
    assert result.getTotal() >= 3 && result.getTotal() <= 13;
  }

  @Test
  void testMultipleSameRoll() {
    RollResult[] result = DiceRoller.multiRoll("1d6", 2);
    assert result.length == 2;
  }

  @Test
  void testMultipleSameRoll2() {
    RollResult[] result = DiceRoller.multiRoll("1d6", 2);
    assert result[0].getTotal() >= 1 && result[0].getTotal() <= 6;
    assert result[1].getTotal() >= 1 && result[1].getTotal() <= 6;
  }

  @Test
  void testStringOutput() {
    RollResult result = DiceRoller.roll("1d1");
    assertEquals("1d1[1]", result.getResult());
  }

  @Test
  void testStringOutput2() {
    RollResult result = DiceRoller.roll("1d1+1d1");
    assertEquals("1d1[1] + 1d1[1]", result.getResult());
  }

  @Test
  void testStringOutput3() {
    RollResult result = DiceRoller.roll("1d1+1d1+1");
    assertEquals("1d1[1] + 1d1[1] + 1", result.getResult());
  }

  @Test
  void testStringOutput4() {
    RollResult result = DiceRoller.roll("1d1-1d1");
    assertEquals("1d1[1] - 1d1[1]", result.getResult());
  }
}