package harpi.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import harpi.alpha.dice.DiceCommand;
import harpi.alpha.dice.DiceRoller;

class DiceRollerTest {

  @Test
  void testRollDie() {
    int result = DiceRoller.rollDie(6);
    assertTrue(result >= 1 && result <= 6, "Invalid dice roll result");
  }

  @Test
  void testSplitCommandsLenght() {
    String input = "2d6+3";
    String[] commands = DiceRoller.splitCommands(input);
    assertEquals(2, commands.length, "Invalid number of commands");
  }

  @Test
  void testSplitCommandsValues() {
    String input = "2d6+3+1d4";
    String[] commands = DiceRoller.splitCommands(input);
    assertEquals("+2d6", commands[0], "Invalid command value");
  }

  @Test
  void testParseInput() {
    String input = "2d6+3";
    List<DiceCommand> diceCommands = DiceRoller.parseInput(input);
    assertEquals(2, diceCommands.size(), "Invalid number of dice commands");
  }

  @Test
  void testParseInputMultipleCommands() {
    String input = "2d6+3+1d4";
    List<DiceCommand> diceCommands = DiceRoller.parseInput(input);
    assertEquals(3, diceCommands.size(), "Invalid number of dice commands");
  }

  @Test
  void testParseInputNegativeCommands() {
    String input = "2d6+3-1d4";
    List<DiceCommand> diceCommands = DiceRoller.parseInput(input);
    assertEquals(-1, diceCommands.get(2).getOperator(), "Invalid dice command multiplier");
  }

  @Test
  void testRoll() {
    String input = "2d6+3";
    int result = DiceRoller.onlyResultRoll(input);
    assertTrue(result >= 5 && result <= 15, "Invalid roll result");
  }
}