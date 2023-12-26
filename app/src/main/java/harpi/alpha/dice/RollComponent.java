package harpi.alpha.dice;

public interface RollComponent {

  /**
   * Rolls the component and overrides the previous result.
   */
  void roll();

  /**
   * @return The result of the roll, e.g. "1d6[3]" or "2d6[3, 5]".
   */
  String getResult();

  /**
   * @return The total of the roll, e.g. 3 or 8 for the getResult() examples
   *         above. This is the sum of all the results of the roll multiplied by
   *         the operator.
   */
  int getTotal();

  /**
   * @return The operator of the roll, e.g. 1 for "+1d6[3]" or -1 for "-2d6[3,
   *         5]".
   */
  int getOperator();
}