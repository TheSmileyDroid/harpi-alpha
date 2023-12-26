package harpi.alpha.dice;

public class NumberComponent implements RollComponent {
  private int number;
  private int operator;

  public NumberComponent(int number, int operator) {
    this.number = number;
    this.operator = operator;
  }

  @Override
  public void roll() {
    return;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public int getOperator() {
    return operator;
  }

  public static NumberComponent of(int number) {
    return new NumberComponent(number, 1);
  }

  public static NumberComponent of(int number, int operator) {
    return new NumberComponent(number, operator);
  }

  public String toString() {
    if (operator < 0) {
      return String.format("- %d", number);
    }
    return String.format("%d", number);
  }

  @Override
  public String getResult() {
    StringBuilder result = new StringBuilder();
    result.append(toString());
    return result.toString();
  }

  @Override
  public int getTotal() {
    return number * operator;
  }

}
