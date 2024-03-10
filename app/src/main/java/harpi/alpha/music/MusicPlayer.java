package harpi.alpha.music;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import harpi.alpha.commands.AbsCommand;
import harpi.alpha.commands.CommandGroup;
import harpi.alpha.commands.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicPlayer implements CommandGroup {
  private final AudioPlayerManager playerManager;
  private final Map<Long, GuildMusicManager> musicManagers;

  public MusicPlayer(CommandHandler handler) {
    this.musicManagers = new HashMap<>();

    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);
    registerCommands(handler);
  }

  public void registerCommands(@Nonnull CommandHandler commandHandler) {
    commandHandler.registerCommand(new PlayMusic());
    commandHandler.registerCommand(new StopMusic());
    commandHandler.registerCommand(new SkipMusic());
    commandHandler.registerCommand(new PauseMusic());
    commandHandler.registerCommand(new ResumeMusic());
    commandHandler.registerCommand(new Random());
    commandHandler.registerCommand(new ListMusics());
    commandHandler.registerCommand(new Volume());
    commandHandler.registerCommand(new Loop());
    commandHandler.registerCommand(new Smooth());
  }

  private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
    long guildId = Long.parseLong(guild.getId());
    GuildMusicManager musicManager = musicManagers.get(guildId);

    if (musicManager == null) {
      musicManager = new GuildMusicManager(playerManager);
      musicManagers.put(guildId, musicManager);
    }

    guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

    return musicManager;
  }

  public void playInternal(MessageReceivedEvent event, GuildMessageChannel channel, String trackUrl) {
    connectToUserVoiceChannel(event.getMember());
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

    playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        play(channel.getGuild(), musicManager, track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack track = playlist.getSelectedTrack();

        if (track == null) {
          List<AudioTrack> tracks = playlist.getTracks();

          for (AudioTrack audioTrack : tracks) {
            play(channel.getGuild(), musicManager, audioTrack);
          }
        } else {
          play(channel.getGuild(), musicManager, track);
        }
      }

      @Override
      public void noMatches() {
        channel.sendMessage("Nada encontrado para " + trackUrl).queue();
      }

      @Override
      public void loadFailed(FriendlyException exception) {
        channel.sendMessage("Não foi possível tocar: " + exception.getMessage()).queue();
      }
    });
  }

  private void loadAndPlay(final GuildMessageChannel channel, final String trackUrl) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

    playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        channel.sendMessage("Adicionando à fila " + track.getInfo().title).queue();

        play(channel.getGuild(), musicManager, track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack track = playlist.getSelectedTrack();

        if (track == null) {
          List<AudioTrack> tracks = playlist.getTracks();
          channel.sendMessage("Adicionando à fila " + tracks.size() + " músicas da playlist "
              + playlist.getName()).queue();

          for (AudioTrack audioTrack : tracks) {
            play(channel.getGuild(), musicManager, audioTrack);
          }
        } else {
          channel.sendMessage(
              "Adicionando à fila " + track.getInfo().title + " (música selecionada da playlist "
                  + playlist.getName()
                  + ")")
              .queue();

          play(channel.getGuild(), musicManager, track);
        }
      }

      @Override
      public void noMatches() {
        channel.sendMessage("Nada encontrado para " + trackUrl).queue();
      }

      @Override
      public void loadFailed(FriendlyException exception) {
        channel.sendMessage("Não foi possível tocar: " + exception.getMessage()).queue();
      }
    });
  }

  private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
    musicManager.scheduler.queue(track);
  }

  private void skipTrack(GuildMessageChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.nextTrack();

    channel.sendMessage("Musica pulada").queue();
  }

  private void stop(GuildMessageChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.player.stopTrack();

    channel.sendMessage("Musica parada").queue();
  }

  private void pause(GuildMessageChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.player.setPaused(true);

    channel.sendMessage("Musica pausada").queue();
  }

  private void resume(GuildMessageChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.player.setPaused(false);

    channel.sendMessage("Musica resumida").queue();
  }

  private void random(GuildMessageChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.random();

    channel.sendMessage("Fila embaralhada").queue();
  }

  private void setVolume(GuildMessageChannel channel, int volume) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.setVolume(volume);

    channel.sendMessage("Volume alterado para " + volume).queue();
  }

  private void setLoop(GuildMessageChannel channel, boolean loop) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.setLoop(loop);

    channel.sendMessage("Loop alterado para " + loop).queue();
  }

  private void getMusicList(GuildMessageChannel channel) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Lista de músicas").setDescription(
        musicManager.scheduler.getQueue().stream().map(AudioTrack::getInfo).map(
            info -> {
              String title = info.title;
              String author = info.author;
              long length = info.length;
              return String.format("%s - %s (%d:%02d)", title, author, length / 60000,
                  (length / 1000) % 60);
            }).reduce("", (acc, cur) -> acc + "\n" + cur))
        .setColor(0xffe663);

    AudioTrack playing = musicManager.scheduler.getPlaying();
    if (playing != null) {
      String current = String.format("%s - %s (%d:%02d/%d:%02d)", playing.getInfo().title,
          playing.getInfo().author, playing.getPosition() / 60000,
          (playing.getPosition() / 1000) % 60, playing.getInfo().length / 60000,
          (playing.getInfo().length / 1000) % 60);
      if (current != null)
        embedBuilder.addField("Playing", current, false);
      embedBuilder.setImage(playing.getInfo().artworkUrl);
    }

    String volume = String.valueOf(musicManager.player.getVolume());
    if (volume != null)
      embedBuilder.addField("Volume", volume, false);

    String loop = "";
    if (musicManager.scheduler.isLoop())
      loop = "✅";
    else
      loop = "❌";

    if (loop != null)
      embedBuilder.addField("Loop", loop, false);

    channel.sendMessageEmbeds(embedBuilder.build()).queue();
  }

  private void connectToUserVoiceChannel(Member member) {
    Guild guild = member.getGuild();
    AudioManager audioManager = guild.getAudioManager();
    if (!audioManager.isConnected()) {

      GuildVoiceState voiceState = member.getVoiceState();
      if (voiceState == null) {
        return;
      }
      AudioChannel channel = voiceState.getChannel();
      if (channel == null) {
        return;
      }
      audioManager.openAudioConnection(channel);
    }
  }

  public class PlayMusic extends AbsCommand {
    @Override
    public String getName() {
      return "play";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      connectToUserVoiceChannel(event.getMember());
      loadAndPlay(event.getChannel().asGuildMessageChannel(),
          String.join(" ", command.subList(1, command.size())).trim());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Coloca uma música ou playlist na fila.";
    }
  }

  public class SkipMusic extends AbsCommand {
    @Override
    public String getName() {
      return "skip";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      skipTrack(event.getChannel().asGuildMessageChannel());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Pula a música atual.";
    }
  }

  public class StopMusic extends AbsCommand {
    @Override
    public String getName() {
      return "stop";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      stop(event.getChannel().asGuildMessageChannel());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Para a música atual.";
    }
  }

  public class PauseMusic extends AbsCommand {
    @Override
    public String getName() {
      return "pause";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      pause(event.getChannel().asGuildMessageChannel());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Pausa a música atual.";
    }
  }

  public class ResumeMusic extends AbsCommand {
    @Override
    public String getName() {
      return "resume";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      resume(event.getChannel().asGuildMessageChannel());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Retoma a música atual.";
    }
  }

  public class Random extends AbsCommand {
    @Override
    public String getName() {
      return "random";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      random(event.getChannel().asGuildMessageChannel());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Embaralha a fila.";
    }
  }

  public class Volume extends AbsCommand {
    @Override
    public String getName() {
      return "volume";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      setVolume(event.getChannel().asGuildMessageChannel(), Integer.parseInt(command.get(1)));
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Altera o volume.";
    }
  }

  public class ListMusics extends AbsCommand {
    @Override
    public String getName() {
      return "list";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      getMusicList(event.getChannel().asGuildMessageChannel());
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Lista as músicas na fila.";
    }
  }

  public class Loop extends AbsCommand {
    @Override
    public String getName() {
      return "loop";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      if (command.size() < 2) {
        event.getChannel().sendMessage("Loop: " + getGuildAudioPlayer(event.getGuild()).scheduler.isLoop()).queue();
        return;
      }
      setLoop(event.getChannel().asGuildMessageChannel(), Boolean.parseBoolean(command.get(1)));
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Altera o estado de loop.";
    }
  }

  public class Smooth extends AbsCommand {
    @Override
    public String getName() {
      return "smooth";
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> command) {
      if (command.size() < 2) {
        event.getChannel().sendMessage("Smooth: " + getGuildAudioPlayer(event.getGuild()).scheduler.isSmooth()).queue();
        return;
      }
      setSmooth(event.getChannel().asGuildMessageChannel(), Boolean.parseBoolean(command.get(1)));
    }

    @Override
    public boolean isGuildOnly() {
      return true;
    }

    @Override
    public String getDescription() {
      return "Altera o estado de smooth. (Smooth é uma técnica de transição entre músicas que deixa a transição mais suave)";
    }
  }

  public void setSmooth(GuildMessageChannel channel, boolean value) {
    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.setSmooth(value);

    channel.sendMessage("Smooth alterado para " + value).queue();
  }
}
