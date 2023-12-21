package server;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BasicCommands extends ListenerAdapter {

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    String message = event.getMessage().getContentRaw();
    if (message.equals("-shutdown")) {
      event.getChannel().sendMessage("Desligando...").queue();
      System.exit(0);
    }

    if (message.equals("-ping")) {
      event.getChannel().sendMessage("Pong!").queue();
    }

  }

}
