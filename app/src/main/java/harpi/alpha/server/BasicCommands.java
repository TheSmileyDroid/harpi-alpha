package harpi.alpha.server;

import java.util.List;

import javax.annotation.Nonnull;

import harpi.alpha.commands.AbsCommand;
import harpi.alpha.commands.CommandGroup;
import harpi.alpha.commands.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BasicCommands implements CommandGroup {

  public BasicCommands(CommandHandler handler) {
    registerCommands(handler);
  }

  @Override
  public void registerCommands(@Nonnull CommandHandler handler) {
    handler.registerCommand(new Shutdown());
    handler.registerCommand(new Ping());
    handler.registerCommand(new Help(handler));
  }

  class Shutdown extends AbsCommand {

    @Override
    public void execute(MessageReceivedEvent event, List<String> args) {
      event.getChannel().sendMessage("Desligando...").queue();
      System.exit(0);
    }

    @Override
    public String getName() {
      return "shutdown";
    }
  }

  class Ping extends AbsCommand {

    @Override
    public void execute(MessageReceivedEvent event, List<String> args) {
      event.getChannel().sendMessage("Pong!").queue();
    }

    @Override
    public String getName() {
      return "ping";
    }

    @Override
    public String getDescription() {
      return "Responde com \"Pong!\".";
    }
  }

  class Help extends AbsCommand {
    private final CommandHandler handler;

    public Help(CommandHandler handler) {
      this.handler = handler;
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> args) {
      EmbedBuilder embedBuilder = new EmbedBuilder()
          .setDescription("Lista de comandos:")
          .setColor(0xffe663);
      for (String command : handler.getCommands().keySet()) {
        String[] aliases = handler.getCommands().get(command).getAlias();
        boolean isAlias = false;
        for (String alias : aliases) {
          if (alias.equals(command)) {
            isAlias = true;
            break;
          }
        }
        if (isAlias) {
          continue;
        }
        embedBuilder.addField("" + command,
            "" + handler.getCommands().get(command).getDescription()
                + (aliases.length > 0 ? "\nAliases: " + String.join(", ", aliases) : ""),
            false);
      }
      event.getChannel().sendMessageEmbeds(
          embedBuilder
              .build())
          .queue();
    }

    @Override
    public String getName() {
      return "help";
    }

    @Override
    public String getDescription() {
      return "Mostra a lista de comandos.";
    }
  }

}
