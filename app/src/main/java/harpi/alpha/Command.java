package harpi.alpha;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {

  String getName();

  void execute(MessageReceivedEvent event, String[] command);

  boolean isGuildOnly();

  boolean hasAlias();

  String[] getAlias();

}
