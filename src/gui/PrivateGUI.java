package gui;

//Github
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

import helper.AudioPlayer;
import main.Client;
import network.SingleConnection;

	private static final long serialVersionUID = 6883134327632102722L;
	public JTextPane texta, message;
	private JButton send, attach;
	private Client client;
	private InetAddress other;
	private SingleConnection conn;
	private HashMap<InetAddress, PrivateGUI> pGUIs;
	private JFileChooser fc;
	private JScrollPane scrollPane;
	private JScrollPane scrollMessage;
	JFrame frame;
	private FlickIcon flickicon;
	private boolean getNotification = false;
	FileFilter docFilter = new FileTypeFilter(".docx", "Microsoft Word Documents");
			frame.setIconImage(ImageIO.read(new File("msn.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			a.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		pGUIs.remove(this.conn.other);
		this.conn.stop();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		getNotification = true;
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		getNotification = true;
		// TODO Auto-generated method stub
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}