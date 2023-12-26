package harpi.alpha.dice;

public class DiceCommandImpl implements DiceComponent {
  private int numberOfDices;
  private int numberOfSides;
  private int operator;
  private int[] results;

  public DiceCommandImpl(int numberOfDices, int numberOfSides, int operator) {
    this.numberOfDices = numberOfDices;
    this.numberOfSides = numberOfSides;
    this.operator = operator;
  }

  @Override
  public void roll() {
    int[] results = new int[numberOfDices];

    for (int i = 0; i < numberOfDices; i++) {
      results[i] = DiceRoller.rollDie(numberOfSides);
    }

    this.results = results;
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

  public String toString() {
    if (operator < 0) {
      return String.format("- %dd%d", numberOfDices, numberOfSides);
    }
    return String.format("%dd%d", numberOfDices, numberOfSides);
  }

  @Override
  public String getResult() {
    StringBuilder result = new StringBuilder();
    result.append(toString());
    result.append("[");
    for (int i = 0; i < results.length; i++) {
      if (i > 0) {
        result.append(", ");
      }
      result.append(results[i]);
    }
    result.append("]");
    return result.toString();
  }

  @Override
  public int getTotal() {
    int total = 0;
    for (int i = 0; i < results.length; i++) {
      total += results[i];
    }
    return total * operator;
  }

}
