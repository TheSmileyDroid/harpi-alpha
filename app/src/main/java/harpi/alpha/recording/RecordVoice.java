package harpi.alpha.recording;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

  private void onRecordCommand(MessageReceivedEvent event, AudioChannel channel) {

    AudioManager audioManager = channel.getGuild().getAudioManager();
    audioManager.openAudioConnection(channel);
    audioManager.setReceivingHandler(new RecordHandler());
    scheduledTasks.put(event.getGuild().getId(),
        scheduler.schedule(new ResetAndSend(event, "Quebra realizada!"), 20, TimeUnit.MINUTES));
  }

  class ResetAndSend implements Runnable {
    private MessageReceivedEvent event;
    private String message;

    public ResetAndSend(MessageReceivedEvent event, String message) {
      this.event = event;
      this.message = message;
    }

    @Override
    public void run() {
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
      event.getChannel().sendMessage(message != null ? message : "Gravação finalizada!").queue();
      onStopCommand(event, channel);
      onRecordCommand(event, channel);
    }
  }

  private void onStopCommand(MessageReceivedEvent event, AudioChannel channel) {
    AudioManager audioManager = channel.getGuild().getAudioManager();
    RecordHandler handler = (RecordHandler) audioManager.getReceivingHandler();
    audioManager.setReceivingHandler(null);
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

      if (scheduledTasks.containsKey(event.getGuild().getId())) {
        event.getChannel().sendMessage("Já estou gravando!").queue();
        return;
      }

      event.getChannel().sendMessage("Gravando...").queue();

      onRecordCommand(event, channel);
    }

    @Override
    public String getName() {
      return "record";
    }

    @Override
    public String getDescription() {
      return "Grava o áudio do canal de voz por 20 minutos. E então envia o arquivo.";
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
      if (!scheduledTasks.containsKey(event.getGuild().getId())) {
        event.getChannel().sendMessage("Não estou gravando!").queue();
        return;
      }

      event.getChannel().sendMessage("Parando...").queue();
      onStopCommand(event, channel);

      scheduledTasks.get(event.getGuild().getId()).cancel(true);
      scheduledTasks.remove(event.getGuild().getId());

    }

    @Override
    public String getName() {
      return "stoprec";
    }

    @Override
    public String getDescription() {
      return "Para a gravação.";
    }
  }

  class CheckRecording extends AbsCommand {

    @Override
    public void execute(MessageReceivedEvent event, List<String> args) {
      if (scheduledTasks.containsKey(event.getGuild().getId())) {
        event.getChannel().sendMessage("Estou gravando! Falta "
            + scheduledTasks.get(event.getGuild().getId()).getDelay(TimeUnit.MINUTES) + " minutos para a quebra.")
            .queue();
      } else {
        event.getChannel().sendMessage("Não estou gravando!").queue();
      }
    }

    @Override
    public String getName() {
      return "checkrec";
    }

    @Override
    public String getDescription() {
      return "Verifica se estou gravando.";
    }
  }

  @Override
  public void registerCommands(@Nonnull CommandHandler handler) {
    handler.registerCommand(new Record());
    handler.registerCommand(new StopRecord());
    handler.registerCommand(new CheckRecording());
  }
}
