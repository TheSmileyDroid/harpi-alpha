package harpi.alpha.recording;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

public class RecordHandler implements AudioReceiveHandler {

  private List<byte[]> audioData = new ArrayList<>();
  private Lock audioDataLock = new ReentrantLock();

  @Override
  public boolean canReceiveCombined() {
    return true;
  }

  @Override
  public void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {
    if (combinedAudio.getUsers().isEmpty())
      return;
    byte[] data = combinedAudio.getAudioData(1.0f); // 48KHz 16bit stereo signed BigEndian PCM.
    try {
      audioDataLock.lock();
      audioData.add(data);
    } catch (OutOfMemoryError e) {
      System.out.println("Out of memory!");
      e.printStackTrace();
    } finally {
      audioDataLock.unlock();
    }
  }

  public String stop() {
    try {
      audioDataLock.lock();
      int size = 0;
      for (byte[] bs : audioData) {
        size += bs.length;
      }
      byte[] decodedData = new byte[size];
      int i = 0;
      for (byte[] bs : audioData) {
        for (int j = 0; j < bs.length; j++) {
          decodedData[i++] = bs[j];
        }
      }

      audioData.clear();

      return saveRAW(decodedData);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      audioDataLock.unlock();
    }
    return null;
  }

  private String saveRAW(byte[] data) {
    File file = new File(new Date().getTime() + ".wav");

    AudioFormat format = OUTPUT_FORMAT;
    try {
      AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(
          data), format, data.length), AudioFileFormat.Type.WAVE, file);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return file.getAbsolutePath();
  }

}
