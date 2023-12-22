package tts;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.Nonnull;

import harpi.alpha.AbsCommand;
import harpi.alpha.CommandGroup;
import harpi.alpha.CommandHandler;
import harpi.alpha.music.MusicPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GoogleTTS implements CommandGroup {
  private final MusicPlayer player;

  public GoogleTTS(MusicPlayer player) {
    this.player = player;
  }

  @Override
  public void registerCommands(@Nonnull CommandHandler handler) {
    handler.registerCommand(new TTSCommand());
  }

  class TTSCommand extends AbsCommand {

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

      onTTSCommand(event, channel, String.join(" ", args.subList(1, args.size())));
    }

    @Override
    public String getName() {
      return "f";
    }
  }

  public void onTTSCommand(MessageReceivedEvent event, AudioChannel channel, String content) {
    String url = "https://translate.google.com/translate_tts?ie=UTF-8&tl={language}&client=tw-ob&q={text}";
    String language = "pt-BR";
    String text = "";
    try {
      text = URLEncoder.encode(content, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    String finalUrl = url.replace("{language}", language).replace("{text}", text);

    player.playInternal(event, event.getGuildChannel(), finalUrl);
  }

}
