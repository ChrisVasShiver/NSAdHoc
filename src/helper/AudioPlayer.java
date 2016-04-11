package helper;

import java.io.File;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioPlayer extends Thread {
	
	public static final String[] SONGS = {"newmsg.wav"};
	
	private Clip clip;
	private boolean enabled;
	
	public AudioPlayer() {
		enabled = true;
	}
	
	public void playSound(String effectName) {
		if(enabled) {
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(effectName).getAbsoluteFile());
				clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			} catch(Exception ex) {
				//ex.printStackTrace();
			}
		}
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void loopTrack() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public void stopTrack() {
		clip.stop();
	}
	
	public static void main(String[] args) {
		AudioPlayer player = new AudioPlayer();
		player.playSound("newmsg.wav");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


