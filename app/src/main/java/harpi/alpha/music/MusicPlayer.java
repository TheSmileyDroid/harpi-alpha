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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicPlayer extends ListenerAdapter {
  private final AudioPlayerManager playerManager;
  private final Map<Long, GuildMusicManager> musicManagers;

  public MusicPlayer() {
    this.musicManagers = new HashMap<>();

    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);
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

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    if (!event.isFromGuild()) {
      return;
    }

    if (!event.getMessage().getContentRaw().startsWith("-"))
      return;

    String[] command = event.getMessage().getContentRaw().substring(1).split(" ");
    System.out.println(command[0]);

    if (command[0].equals("play")) {
      if (command.length < 2) {
        event.getChannel().sendMessage("Por favor, forneça um link").queue();
        return;
      }
      Member member = event.getMember();
      if (member == null) {
        event.getChannel().sendMessage("Você não está em um servidor!").queue();
        return;
      }
      connectToUserVoiceChannel(member);

      loadAndPlay(event.getChannel().asGuildMessageChannel(), command[1]);
    }

    if (command[0].equals("skip")) {
      skipTrack(event.getChannel().asGuildMessageChannel());
    }

    if (command[0].equals("stop")) {
      stop(event.getChannel().asGuildMessageChannel());
    }

    if (command[0].equals("pause")) {
      pause(event.getChannel().asGuildMessageChannel());
    }

    if (command[0].equals("resume")) {
      resume(event.getChannel().asGuildMessageChannel());
    }

    if (command[0].equals("random")) {
      random(event.getChannel().asGuildMessageChannel());
    }

    if (command[0].equals("volume")) {
      if (command.length < 2) {
        event.getChannel().sendMessage("Por favor, forneça o novo volume no formato 0-100")
            .queue();
        return;
      }
      if (Integer.parseInt(command[1]) < 0 || Integer.parseInt(command[1]) > 100) {
        event.getChannel().sendMessage("Por favor, forneça o novo volume no formato 0-100")
            .queue();
        return;
      }
      setVolume(event.getChannel().asGuildMessageChannel(), Integer.parseInt(command[1]));
    }

    if (command[0].equals("list") || command[0].equals("queue")) {
      getMusicList(event.getChannel().asGuildMessageChannel());
    }

    if (command[0].equals("loop")) {
      if (command.length < 2) {
        event.getChannel().sendMessage("Por favor, forneça o novo loop no formato true/false")
            .queue();
        return;
      }
      setLoop(event.getChannel().asGuildMessageChannel(), Boolean.parseBoolean(command[1]));
    }

    super.onMessageReceived(event);
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
}
