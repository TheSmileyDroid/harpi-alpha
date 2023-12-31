package harpi.alpha;

import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {

  String getName();

  void execute(MessageReceivedEvent event, List<String> command);

  boolean isGuildOnly();

  boolean hasAlias();

  String[] getAlias();

  String getDescription();

}
