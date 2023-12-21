package harpi.alpha.dice;

public class DiceCommandImpl implements DiceCommand {
  private int numberOfDices;
  private int numberOfSides;
  private int operator;

  public DiceCommandImpl(int numberOfDices, int numberOfSides, int operator) {
    this.numberOfDices = numberOfDices;
    this.numberOfSides = numberOfSides;
    this.operator = operator;
  }

  @Override
  public int[] roll() {
    int[] results = new int[numberOfDices];

    for (int i = 0; i < numberOfDices; i++) {
      results[i] = DiceRoller.rollDie(numberOfSides);
    }

    return results;
  }

  public int getNumberOfDices() {
    return numberOfDices;
  }

  public int getNumberOfSides() {
    return numberOfSides;
  }

  @Override
  public int getOperator() {
    return operator;
  }

  @Override
  public int getNumber() {
    return 0;
  }

  public String toString() {
    if (operator < 0) {
      return String.format("- %dd%d", numberOfDices, numberOfSides);
    }
    return String.format("+ %dd%d", numberOfDices, numberOfSides);
  }

  @Override
  public boolean isNumberOnly() {
    return false;
  }
}
