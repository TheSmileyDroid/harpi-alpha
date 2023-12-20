package harpi.alpha;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  private final BlockingQueue<AudioTrack> queue;
  private boolean loop = false;

  /**
   * @param player The audio player this scheduler uses
   */
  public TrackScheduler(AudioPlayer player) {
    this.player = player;
    player.setVolume(30);
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
    player.startTrack(queue.poll(), false);
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