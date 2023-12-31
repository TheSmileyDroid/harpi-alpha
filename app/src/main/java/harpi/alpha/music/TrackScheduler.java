package harpi.alpha.music;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;

public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  private final BlockingQueue<AudioTrack> queue;
  private boolean loop = false;
  private boolean smooth = false;
  private int volume = 30;
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TrackScheduler.class);
  private Thread smoothThread;

  /**
   * @param player The audio player this scheduler uses
   */
  public TrackScheduler(AudioPlayer player) {
    this.player = player;
    player.setVolume(this.volume);
    this.queue = new LinkedBlockingQueue<>();
  }

  /**
   * Add the next track to queue or play right away if nothing is in the queue.
   *
   * @param track The track to play or add to queue.
   */
  public void queue(AudioTrack track) {
    if (!player.startTrack(track, true)) {
      queue.offer(track);
    }
  }

  public void nextTrack() {
    if (this.smooth && this.player.getPlayingTrack() != null
        && this.player.getPlayingTrack().getState() == AudioTrackState.PLAYING) {
      long position = this.player.getPlayingTrack().getPosition();
      long duration = this.player.getPlayingTrack().getDuration();
      if (position < duration - 5000) {
        logger.info("SmoothScheduler: stop track");
        if (this.smoothThread != null) {
          this.smoothThread.interrupt();
        }
        Thread smooth = new Thread(new SmoothScheduler(), "Smooth");
        this.smoothThread = smooth;
        smooth.start();
        return;
      }
    }
    logger.debug("TrackScheduler: next track");
    player.startTrack(queue.poll(), false);
  }

  class SmoothScheduler implements Runnable {

    @Override
    public void run() {
      int _volume = player.getVolume();
      while (_volume > 0) {
        player.setVolume(_volume);
        _volume -= 5;
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      logger.info("SmoothScheduler: next track");

      player.startTrack(queue.poll(), false);

      player.setVolume(volume);

      smoothThread = null;
    }

  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (this.loop) {
        this.player.startTrack(track.makeClone(), false);
      } else {
        nextTrack();
      }
    }
  }

  /**
   * Aleatoriza a fila de músicas.
   */
  public void random() {
    this.queue.stream().forEach((track) -> {
      int random = (int) (Math.random() * this.queue.size());
      AudioTrack aux = this.queue.poll();
      this.queue.add(this.queue.toArray(new AudioTrack[this.queue.size()])[random]);
      this.queue.add(aux);
    });
  }

  public void setVolume(int volume) {
    this.player.setVolume(volume);
  }

  public BlockingQueue<AudioTrack> getQueue() {
    return this.queue;
  }

  public AudioTrack getPlaying() {
    return this.player.getPlayingTrack();
  }

  public boolean isLoop() {
    return this.loop;
  }

  public void setLoop(boolean loop) {
    this.loop = loop;
  }

  public boolean isSmooth() {
    return this.smooth;
  }

  public void setSmooth(boolean smooth) {
    this.smooth = smooth;
  }

  public void clear() {
    this.queue.clear();
  }

  public void remove(int index) {
    this.queue.remove(this.queue.toArray(new AudioTrack[this.queue.size()])[index]);
  }

  public void remove(AudioTrack track) {
    this.queue.remove(track);
  }
}