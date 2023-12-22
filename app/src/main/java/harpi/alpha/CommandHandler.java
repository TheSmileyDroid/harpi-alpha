package harpi.alpha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {
  Map<String, Command> commands = new HashMap<>();

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    if (!event.getMessage().getContentRaw().startsWith("-")) {
      return;
    }

    String[] commandArgs = event.getMessage().getContentRaw().substring(1).split(" ");

    if (commands.containsKey(commandArgs[0])) {
      Command command = commands.get(commandArgs[0]);
      if (command.isGuildOnly() && !event.isFromGuild()) {
        event.getChannel().sendMessage("Esse comando só pode ser executado em um servidor!").queue();
        return;
      }

      List<String> args = List.of(commandArgs);

      command.execute(event, args);
    } else {
      event.getChannel().sendMessage("Comando não encontrado.").queue();
    }
  }

  public void registerCommand(Command command) {
    commands.put(command.getName(), command);
    if (command.hasAlias()) {
      String[] aliases = command.getAlias();
      for (String alias : aliases) {
        commands.put(alias, command);
      }
    }
  }

}
