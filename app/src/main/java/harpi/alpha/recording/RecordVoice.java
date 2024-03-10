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

import harpi.alpha.commands.AbsCommand;
import harpi.alpha.commands.CommandGroup;
import harpi.alpha.commands.CommandHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.FileUpload;

public class RecordVoice implements CommandGroup {
  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

  public RecordVoice(CommandHandler handler) {
    registerCommands(handler);
  }

  private void onRecordCommand(MessageReceivedEvent event, AudioChannel channel, String name, int part) {

    AudioManager audioManager = channel.getGuild().getAudioManager();
    audioManager.openAudioConnection(channel);
    RecordHandler handler = new RecordHandler();
    handler.name = name;
    handler.part = part;
    handler.recording = true;
    audioManager.setReceivingHandler(handler);

    scheduledTasks.put(event.getGuild().getId(),
        scheduler.schedule(new ResetAndSend(event, name, part), 20, TimeUnit.MINUTES));
  }

  class ResetAndSend implements Runnable {
    private MessageReceivedEvent event;
    private String name;
    private int part;

    public ResetAndSend(MessageReceivedEvent event, String name, int part) {
      this.event = event;
      this.name = name;
      this.part = part;
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
      event.getChannel().sendMessage("Gravação de " + name + " (" + part + ")").queue();
      onStopCommand(event, channel);
      onRecordCommand(event, channel, name, part++);
    }
  }

  private void onStopCommand(MessageReceivedEvent event, AudioChannel channel) {
    AudioManager audioManager = channel.getGuild().getAudioManager();
    RecordHandler handler = (RecordHandler) audioManager.getReceivingHandler();
    audioManager.setReceivingHandler(null);
    if (handler != null) {
      String filename = handler.stop();

      event.getChannel().sendMessage("Enviando gravação: " + handler.name + " Parte " + handler.part).queue();

      String filenameConverted = filename.replace(".wav", "_" + handler.name + "_" + handler.part + ".mp3");

      // Convert to mp3 using ffmpeg; 48KHz 16bit stereo signed BigEndian PCM.
      String[] cmd = { "ffmpeg", "-y", "-i", filename, "-f", "mp3",
          "-metadata", "title=" + handler.name + " parte " + handler.part,
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
        sendErrorMessage(event.getChannel(), "You are not in a voice channel");
        return;
      }
      GuildVoiceState voiceState = member.getVoiceState();
      if (voiceState == null) {
        sendErrorMessage(event.getChannel(), "You are not in a voice channel");
        return;
      }
      AudioChannel voiceChannel = voiceState.getChannel();
      if (voiceChannel == null) {
        sendErrorMessage(event.getChannel(), "You are not in a voice channel");
        return;
      }

      if (scheduledTasks.containsKey(event.getGuild().getId())) {
        sendErrorMessage(event.getChannel(), "Already recording");
        return;
      }

      String name = String.join(" ", args.subList(1, args.size()));
      if (name.isEmpty()) {
        name = "recording";
      }

      event.getChannel().sendMessage("Gravando... " + name).queue();

      onRecordCommand(event, voiceChannel, name, 1);
    }

    private void sendErrorMessage(MessageChannelUnion messageChannelUnion, String message) {
      messageChannelUnion.sendMessage(message).queue();
    }

    @Override
    public String getName() {
      return "record";
    }

    @Override
    public String getDescription() {
      return "Grava o áudio do canal de voz por até 20 minutos. E então envia o arquivo.";
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
