package harpi.alpha.dice;

public class NumberCommand implements DiceCommand {
  private int number;
  private int operator;

  public NumberCommand(int number, int operator) {
    this.number = number;
    this.operator = operator;
  }

  @Override
  public int[] roll() {
    return new int[] { number };
  }

  @Override
  public int getNumberOfDices() {
    return 1;
  }

  @Override
  public int getNumberOfSides() {
    return 0;
  }

  @Override
  public int getNumber() {
    return number;
  }

  @Override
  public int getOperator() {
    return operator;
  }

  public static DiceCommand of(int number) {
    return new NumberCommand(number, 1);
  }

  public static DiceCommand of(int number, int operator) {
    return new NumberCommand(number, operator);
  }

  public String toString() {
    if (operator < 0) {
      return String.format("- %d", number);
    }
    return String.format("+ %d", number);
  }

  @Override
  public boolean isNumberOnly() {
    return true;
  }
}
