package harpi.alpha.recording;

import javax.annotation.Nonnull;

import harpi.alpha.AbsCommand;
import harpi.alpha.CommandGroup;
import harpi.alpha.CommandHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class RecordVoice implements CommandGroup {

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

  class Record extends AbsCommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
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

    @Override
    public String getName() {
      return "record";
    }
  }

  class StopRecord extends AbsCommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
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

    @Override
    public String getName() {
      return "stoprec";
    }
  }

  @Override
  public void registerCommands(@Nonnull CommandHandler handler) {
    handler.registerCommand(new Record());
    handler.registerCommand(new StopRecord());
  }
}
