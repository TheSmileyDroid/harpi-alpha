package harpi.alpha.commands;

import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class AbsCommand implements Command {
  public abstract void execute(MessageReceivedEvent event, List<String> args);

  public abstract String getName();

  public String[] getAlias() {
    return new String[0];
  }

  public boolean isGuildOnly() {
    return false;
  }

  public boolean hasAlias() {
    return false;
  }

  public String getDescription() {
    return "";
  }

}
