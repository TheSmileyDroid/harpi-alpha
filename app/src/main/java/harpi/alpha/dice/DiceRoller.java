package harpi.alpha.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import harpi.alpha.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiceRoller implements Command {

  private void onRollCommand(MessageReceivedEvent event, String input) {
    String finalString;
    if (input.contains("#")) {
      String[] parts = input.split("#");
      int count = Integer.parseInt(parts[0]);
      boolean isNegative = count <= 0;
      if (isNegative) {
        count = Math.abs(count - 2);
      }
      int max = 0;
      int min = 0;

      String diceString = parts[1];
      RollResult[] results = new RollResult[count];
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < count; i++) {
        RollResult rollResult = roll(diceString);
        results[i] = rollResult;
        if (i == 0) {
          max = rollResult.getTotal();
          min = rollResult.getTotal();
        } else {
          if (rollResult.getTotal() > max) {
            max = rollResult.getTotal();
          }
          if (rollResult.getTotal() < min) {
            min = rollResult.getTotal();
          }
        }
      }

      for (int i = 0; i < count; i++) {
        RollResult rollResult = results[i];
        if (rollResult.getTotal() == max && !isNegative) {
          result.append("**");
        }
        if (rollResult.getTotal() == min && isNegative) {
          result.append("**");
        }
        result.append(rollResult.getResult());

        if (rollResult.getTotal() == max && !isNegative) {
          result.append("**");
        }
        if (rollResult.getTotal() == min && isNegative) {
          result.append("**");
        }
        result.append("\n");
      }

      finalString = result.toString();
    } else {
      finalString = roll(input).getResult();
    }

    EmbedBuilder embed = new EmbedBuilder();
    embed.setTitle("Resultado");
    embed.setDescription(finalString);
    embed.setColor(0xffe663);
    event.getChannel().sendMessageEmbeds(embed.build()).queue();
  }

  public static int rollDie(int sides) {
    Random random = new Random();
    return random.nextInt(sides) + 1;
  }

  public static String[] splitCommands(String input) {
    int size = input.length();
    List<String> commands = new ArrayList<>();
    StringBuilder command = new StringBuilder();

    if (input.charAt(0) != '+' && input.charAt(0) != '-') {
      command.append('+');
    }

    for (int i = 0; i < size; i++) {
      char c = input.charAt(i);
      if (c == '+' || c == '-') {
        commands.add(command.toString());
        command = new StringBuilder();
      }
      command.append(c);
    }

    commands.add(command.toString());

    return commands.toArray(new String[0]);
  }

  public static List<DiceCommand> parseInput(String input) {
    List<DiceCommand> diceCommands = new ArrayList<>();

    String[] commands = splitCommands(input);

    for (String command : commands) {
      if (command.isEmpty()) {
        continue;
      }

      String signal = command.substring(0, 1);
      if (signal.equals("+") || signal.equals("-")) {
        command = command.substring(1);
      }

      int multiplier = signal.equals("+") ? 1 : -1;

      if (command.contains("d")) {
        String[] parts = command.split("d");
        int count = Integer.parseInt(parts[0]);
        int sides = Integer.parseInt(parts[1]);
        diceCommands.add(new DiceCommandImpl(count, sides, multiplier));
      } else {
        int number = Integer.parseInt(command);
        diceCommands.add(NumberCommand.of(number, multiplier));
      }
    }

    return diceCommands;
  }

  public static RollResult roll(String input) {
    List<DiceCommand> command = parseInput(input);

    StringBuilder result = new StringBuilder();
    int total = 0;

    for (int i = 0; i < command.size(); i++) {
      DiceCommand diceCommand = command.get(i);
      result.append(diceCommand.toString());
      if (diceCommand.isNumberOnly()) {
        total += diceCommand.roll()[0] * diceCommand.getOperator();
        continue;
      }
      result.append("[");
      for (int j = 0; j < diceCommand.getNumberOfDices(); j++) {
        int value = diceCommand.roll()[j];
        total += value * diceCommand.getOperator();
        if (value == 1 || value == diceCommand.getNumberOfSides()) {
          result.append("**");
        }
        result.append(value);
        if (value == 1 || value == diceCommand.getNumberOfSides()) {
          result.append("**");
        }
        if (j < diceCommand.getNumberOfDices() - 1) {
          result.append(", ");
        }
      }
      result.append("] ");
    }
    result.append(" = ");
    result.append(total);

    return new RollResult(result.toString(), total);
  }

  public static int onlyResultRoll(String input) {
    List<DiceCommand> command = parseInput(input);

    int total = 0;

    for (DiceCommand diceCommand : command) {
      for (int value : diceCommand.roll()) {
        total += value * diceCommand.getOperator();
      }
    }

    return total;
  }

  @Override
  public String getName() {
    return "roll";
  }

  @Override
  public void execute(MessageReceivedEvent event, String[] command) {
    if (command.length == 1) {
      event.getChannel().sendMessage("Você precisa especificar o dado a ser rolado.").queue();
    } else {
      onRollCommand(event, String.join(" ", command).substring(command[0].length() + 1));
    }
  }

  @Override
  public boolean isGuildOnly() {
    return false;
  }

  @Override
  public boolean hasAlias() {
    return true;
  }

  @Override
  public String[] getAlias() {
    return new String[] { "r", "rolar", "d", "dados" };
  }

}

class RollResult {
  private String result;
  private int total;

  public RollResult(String result, int total) {
    this.result = result;
    this.total = total;
  }

  public String getResult() {
    return result;
  }

  public int getTotal() {
    return total;
  }
}