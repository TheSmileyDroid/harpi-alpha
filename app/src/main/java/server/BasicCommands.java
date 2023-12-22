package server;

import java.util.List;

import javax.annotation.Nonnull;

import harpi.alpha.AbsCommand;
import harpi.alpha.CommandGroup;
import harpi.alpha.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BasicCommands implements CommandGroup {

  @Override
  public void registerCommands(@Nonnull CommandHandler handler) {
    handler.registerCommand(new Shutdown());
    handler.registerCommand(new Ping());
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
  }

}
