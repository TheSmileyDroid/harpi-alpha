package harpi.alpha.recording;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import harpi.alpha.AbsCommand;
import harpi.alpha.CommandGroup;
import harpi.alpha.CommandHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.FileUpload;

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
      String filename = handler.stop();

      String filenameConverted = filename.replace(".wav", ".mp3");

      // Convert to mp3 using ffmpeg; 48KHz 16bit stereo signed BigEndian PCM.
      String[] cmd = { "ffmpeg", "-y", "-i", filename, "-f", "mp3",
          "-acodec", "libmp3lame", filenameConverted };
      try {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
      } catch (Exception e) {
        e.printStackTrace();
      }

      File file = new File(filenameConverted);
      event.getChannel().sendFiles(FileUpload.fromData(file)).queue();
    }

  }

  class Record extends AbsCommand {

    @Override
    public void execute(MessageReceivedEvent event, List<String> args) {
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
    public void execute(MessageReceivedEvent event, List<String> args) {
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
