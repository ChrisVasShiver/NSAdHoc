
package gui;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FlickIcon extends Thread {
	private PrivateGUI pGUI;

	public FlickIcon(PrivateGUI privateGUI) {
		pGUI = privateGUI;
	}

	public void flickIcon() {
		for (int counter = 20; counter > 0; counter--) {
			try {
				pGUI.getFrame().setIconImage(ImageIO.read(new File("msn_black.png")));
				Thread.sleep(200);
				pGUI.getFrame().setIconImage(ImageIO.read(new File("msn.png")));
				Thread.sleep(200);
			} catch (IOException | InterruptedException e) {}
		}

	}

	public void run() {
		flickIcon();
	}

}
