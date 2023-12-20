/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package harpi.alpha;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception {
        String token = args.length >= 1 ? args[0] : System.getenv("DISCORD_ID");

        if (token == null) {
            System.out.println(
                    "Please provide a token as the first argument or set the DISCORD_ID environment variable.");
            return;
        } else {
            System.out.println("Using token: " + token);
        }

        JDABuilder.createDefault(token, GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners(new EchoVoice())
                .addEventListeners(new MusicPlayer())
                .setActivity(Activity.playing("Hello, World!"))
                .enableCache(CacheFlag.VOICE_STATE)
                .build();
    }
}
