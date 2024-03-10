package harpi.alpha.dice;

import java.util.List;

public class RollResult {
  private List<RollComponent> components;

  public RollResult(List<RollComponent> components) {
    this.components = components;
    for (RollComponent rollComponent : components) {
      rollComponent.roll();
    }
  }

  public static boolean isBestRoll(RollResult[] results, int index) {
    int best = results[0].getTotal();
    for (int i = 1; i < results.length; i++) {
      if (results[i].getTotal() > best) {
        best = results[i].getTotal();
      }
    }
    return results[index].getTotal() == best;
  }

  public static boolean isWorstRoll(RollResult[] results, int index) {
    int worst = results[0].getTotal();
    for (int i = 1; i < results.length; i++) {
      if (results[i].getTotal() < worst) {
        worst = results[i].getTotal();
      }
    }
    return results[index].getTotal() == worst;
  }

  public String getResult() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < components.size(); i++) {
      RollComponent rollComponent = components.get(i);
      if (i > 0 && rollComponent.getOperator() > 0) {
        result.append(" + ");
      } else if (i > 0 && rollComponent.getOperator() < 0) {
        result.append(" ");
      }
      result.append(rollComponent.getResult());
    }
    return result.toString();
  }

  public int getTotal() {
    int total = 0;
    for (RollComponent rollComponent : components) {
      total += rollComponent.getTotal();
    }
    return total;
  }

  public List<RollComponent> getComponents() {
    return components;
  }
}