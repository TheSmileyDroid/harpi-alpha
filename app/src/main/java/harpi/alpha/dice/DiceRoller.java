package harpi.alpha.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import harpi.alpha.commands.AbsCommand;
import harpi.alpha.commands.CommandGroup;
import harpi.alpha.commands.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiceRoller implements CommandGroup {

  public DiceRoller(CommandHandler handler) {
    registerCommands(handler);
  }

  private void onRollCommand(MessageReceivedEvent event, String input) throws Exception {
    String finalString;
    if (input.contains("#")) {
      String[] parts = input.split("#");
      int count = Integer.parseInt(parts[0]);
      if (count > 70) {
        throw new IllegalArgumentException("Não é possível rolar mais de 70 dados.");
      }
      boolean hightlightBest = true;
      if (count < 1) {
        hightlightBest = false;
        count = Math.abs(count) + 2;
      }
      RollResult[] results = multiRoll(parts[1], count);
      finalString = "";
      for (int i = 0; i < results.length; i++) {
        if (i > 0) {
          finalString += "\n";
        }
        if (hightlightBest && RollResult.isBestRoll(results, i)) {
          finalString += "**";
        } else if (!hightlightBest && RollResult.isWorstRoll(results, i)) {
          finalString += "**";
        }
        finalString += results[i].getResult();
        finalString += " = ";
        finalString += results[i].getTotal();
        if (hightlightBest && RollResult.isBestRoll(results, i)) {
          finalString += "**";
        } else if (!hightlightBest && RollResult.isWorstRoll(results, i)) {
          finalString += "**";
        }
      }

    } else {
      RollResult rollResult = roll(input);
      finalString = rollResult.getResult();
      finalString += " = ";
      finalString += rollResult.getTotal();
    }

    EmbedBuilder embed = new EmbedBuilder();
    embed.setDescription(finalString);
    embed.setColor(0xffe663);
    event.getMessage().replyEmbeds(embed.build()).queue();
  }

  public static int rollDie(int sides) {
    Random random = new Random();
    return random.nextInt(sides) + 1;
  }

  public static List<String> splitInput(String input) {
    Pattern pattern = Pattern.compile("([^+-]+)|(\\+|-)");
    Matcher matcher = pattern.matcher(input);

    List<String> result = new ArrayList<>();
    while (matcher.find()) {
      if (!matcher.group().isEmpty()) {
        result.add(matcher.group().trim());
      }
    }

    return result;
  }

  public static List<RollComponent> parseInput(String input) {
    List<RollComponent> diceCommands = new ArrayList<>();

    List<String> commands = splitInput(input);

    for (int i = 0; i < commands.size(); i++) {
      String command = commands.get(i);
      if (command.isEmpty() || command.equals("+") || command.equals("-")) {
        continue;
      }

      String signal = "+";
      if (i != 0) {
        signal = commands.get(i - 1);
        if (signal == null || !(signal.equals("+") || signal.equals("-"))) {
          signal = "+";
        }
      }

      int multiplier = signal.equals("-") ? -1 : 1;

      if (command.contains("d")) {
        String[] parts = command.split("d");
        if (parts[0].isEmpty()) {
          parts[0] = "1";
        }
        if (parts[1].isEmpty()) {
          parts[1] = "6";
        }
        int count = Integer.parseInt(parts[0]);
        int sides = Integer.parseInt(parts[1]);
        diceCommands.add(new DiceCommandImpl(count, sides, multiplier));
      } else {
        int number = Integer.parseInt(command);
        diceCommands.add(NumberComponent.of(number, multiplier));
      }
    }

    return diceCommands;
  }

  public static RollResult roll(String input) {
    List<RollComponent> command = parseInput(input);
    RollResult result = new RollResult(command);

    return result;
  }

  class DiceRoll extends AbsCommand {
    @Override
    public String getName() {
      return "roll";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      if (command.size() == 1) {
        event.getMessage().reply("Você precisa especificar o dado a ser rolado.").queue();
      } else {
        try {
          onRollCommand(event, String.join(" ", command.subList(1, command.size())));
        } catch (Exception e) {
          event.getMessage().reply("Ocorreu um erro ao rolar os dados: " + e.getMessage()).queue();
        }

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

  @Override
  public void registerCommands(@Nonnull CommandHandler handler) {
    handler.registerCommand(new DiceRoll());
  }

  public static RollResult[] multiRoll(String string, int i) {
    RollResult[] results = new RollResult[i];
    for (int j = 0; j < i; j++) {
      results[j] = roll(string);
    }
    return results;
  }

}