package harpi.alpha.recording;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class EchoVoice extends ListenerAdapter {

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    String message = event.getMessage().getContentRaw();
    if (message.equals("-echo")) {
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

      onEchoCommand(event, channel);
    }

    if (message.equals("-leave")) {
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

      onLeaveCommand(event, channel);
    }

  }

  private void onEchoCommand(MessageReceivedEvent event, @Nonnull AudioChannel channel) {
    connectTo(channel);
    onConnecting(channel, event.getChannel());
  }

  private void onLeaveCommand(MessageReceivedEvent event, @Nonnull AudioChannel channel) {
    Guild guild = channel.getGuild();
    AudioManager audioManager = guild.getAudioManager();
    audioManager.closeAudioConnection();
    event.getChannel().sendMessage("Saindo do canal de voz: " + channel.getName()).queue();
  }

  private void onConnecting(AudioChannel channel, MessageChannel messageChannel) {
    messageChannel.sendMessage("Conectando ao canal de voz: " + channel.getName()).queue();
  }

  private void connectTo(AudioChannel channel) {
    Guild guild = channel.getGuild();
    AudioManager audioManager = guild.getAudioManager();
    EchoHandler handler = new EchoHandler();

    audioManager.setSendingHandler(handler);
    audioManager.setReceivingHandler(handler);
    audioManager.openAudioConnection(channel);
  }

  public static class EchoHandler implements AudioSendHandler, AudioReceiveHandler {
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean canReceiveCombined() {
      return queue.size() < 10;
    }

    @Override
    public void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {
      if (combinedAudio.getUsers().isEmpty())
        return;

      byte[] data = combinedAudio.getAudioData(0.5f);
      queue.add(data);
    }

    @Override
    public boolean canProvide() {
      return !queue.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
      byte[] data = queue.poll();
      return data == null ? null : ByteBuffer.wrap(data);
    }

    @Override
    public boolean isOpus() {
      return false;
    }
  }
}
