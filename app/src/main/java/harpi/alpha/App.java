/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package harpi.alpha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import harpi.alpha.dice.DiceRoller;
import harpi.alpha.music.MusicPlayer;
import harpi.alpha.recording.EchoVoice;
import harpi.alpha.recording.RecordVoice;
import harpi.alpha.server.BasicCommands;
import harpi.alpha.tts.GoogleTTS;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class App {
    final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        String token = args.length >= 1 ? args[0] : System.getenv("DISCORD_ID");

        if (token == null) {
            System.out.println(
                    "Please provide a token as the first argument or set the DISCORD_ID environment variable.");
            return;
        } else {
            System.out.println("Using token: " + token);
        }

        logger.info("Starting harpi...");

        CommandHandler commandHandler = new CommandHandler();
        MusicPlayer musicPlayer = new MusicPlayer();
        BasicCommands basicCommands = new BasicCommands();
        DiceRoller diceRoller = new DiceRoller();
        EchoVoice echoVoice = new EchoVoice();
        RecordVoice recordVoice = new RecordVoice();
        GoogleTTS googleTTS = new GoogleTTS(musicPlayer);
        musicPlayer.registerCommands(commandHandler);
        basicCommands.registerCommands(commandHandler);
        diceRoller.registerCommands(commandHandler);
        echoVoice.registerCommands(commandHandler);
        recordVoice.registerCommands(commandHandler);
        googleTTS.registerCommands(commandHandler);

        JDABuilder.createDefault(token, GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners(commandHandler)
                .setActivity(Activity.watching("você!"))
                .enableCache(CacheFlag.VOICE_STATE)
                .build();
    }
}
