package harpi.alpha.dice;

public interface DiceCommand {

  int[] roll();

  int getNumberOfDices();

  int getNumberOfSides();

  int getNumber();

  int getOperator();

  boolean isNumberOnly();

}