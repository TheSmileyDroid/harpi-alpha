package harpi.alpha.recording;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class RecordVoice extends ListenerAdapter {
  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    String message = event.getMessage().getContentRaw();
    if (message.equals("-record")) {
      Member member = event.getMember();
      if (member == null) {
        event.getChannel().sendMessage("Você não está em um servidor!").queue();
        return;
      }
      GuildVoiceState voiceState = member.getVoiceState();
      if (voiceState == null) {
        event.getChannel().sendMessage("Você não está em um canal de voz!").queue();
        return;
      }
      AudioChannel channel = voiceState.getChannel();
      if (channel == null) {
        event.getChannel().sendMessage("Você não está em um canal de voz!").queue();
        return;
      }
      event.getChannel().sendMessage("Gravando...").queue();

      onRecordCommand(event, channel);
    }

    if (message.equals("-stop")) {
      Member member = event.getMember();
      if (member == null) {
        event.getChannel().sendMessage("Você não está em um servidor!").queue();
        return;
      }
      GuildVoiceState voiceState = member.getVoiceState();
      if (voiceState == null) {
        event.getChannel().sendMessage("Você não está em um canal de voz!").queue();
        return;
      }
      AudioChannel channel = voiceState.getChannel();
      if (channel == null) {
        event.getChannel().sendMessage("Você não está em um canal de voz!").queue();
        return;
      }
      event.getChannel().sendMessage("Parando...").queue();

      onStopCommand(event, channel);
    }
  }

  private void onRecordCommand(MessageReceivedEvent event, AudioChannel channel) {
    AudioManager audioManager = channel.getGuild().getAudioManager();
    audioManager.openAudioConnection(channel);
    audioManager.setReceivingHandler(new RecordHandler());
  }

  private void onStopCommand(MessageReceivedEvent event, AudioChannel channel) {
    AudioManager audioManager = channel.getGuild().getAudioManager();
    RecordHandler handler = (RecordHandler) audioManager.getReceivingHandler();
    if (handler != null) {
      handler.stop();
    }
    audioManager.closeAudioConnection();
  }
}
