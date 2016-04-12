package helper;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class AudioPlayer extends Thread {
	
	private Clip clip;
	private boolean enabled;
	
	/**
	 * This class makes it possible to play audio
	 */
	public AudioPlayer() {
		enabled = true;
	}
	
	/**
	 * Plays the sound corresponding to the name
	 * @param effectName the name of the file.
	 */
	public void playSound(String effectName) {
		if(enabled) {
			try {
		        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(effectName));
		        DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
		        clip = (Clip)AudioSystem.getLine(info);
		        clip.open(inputStream);
		        clip.start();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Makes it possible to mute/activate sounds by setting enabled true or false
	 * @param enabled true if you want to play audio, false is you want to mute the audio
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Keeps looping the audio continuously until stopped.
	 */
	public void loopTrack() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	/**
	 * Stops the current audio that is playing.
	 */
	public void stopTrack() {
		clip.stop();
	}
}