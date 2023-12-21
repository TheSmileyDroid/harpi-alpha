package harpi.alpha.recording;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

public class RecordHandler implements AudioReceiveHandler {

  @Override
  public boolean canReceiveCombined() {
    return true;
  }

  @Override
  public void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {
    byte[] data = combinedAudio.getAudioData(0.5f);

    // Save data to file
    System.out.println("Received audio data: " + data.length);
  }

  public void stop() {
  }

}
