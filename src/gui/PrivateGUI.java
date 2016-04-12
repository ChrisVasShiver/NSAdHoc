package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import helper.AudioPlayer;
import helper.Constants;
import helper.FilePacket;
import main.Client;
import network.SingleConnection;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class PrivateGUI extends JPanel {

	private static final long serialVersionUID = 6883134327632102722L;
	private PrivateGUIController privateGUIController;
	private JTextPane texta, message;
	private JButton send, attach;
	private Client client;
	private SingleConnection conn;
	private JFileChooser fc;
	private JScrollPane scrollPane;
	private JScrollPane scrollMessage;
	private JFrame frame;
	private FlickIcon flickicon;
	private boolean getNotification = false;
	private boolean hasConnection = false;
	
	/**
	 * Constructor of the Private GUI
	 * @param client the client
	 * @param me the IP4 address of the user of the program
	 * @param conn The connection between two clients
	 * @param pGUIs 
	 */
	public PrivateGUI(Client client, InetAddress me, SingleConnection conn) {
		privateGUIController = new PrivateGUIController(this);
		flickicon = new FlickIcon(this);
		this.client = client;
		this.conn = conn;
		buildGUI();
	}
	
	public JTextPane getTextField() {
		return texta;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public boolean getHasConnection() {
		return hasConnection;
	}
	
	public void setHasConnection(boolean bool) {
		hasConnection = bool;
	}
	/**
	 * Initiates all the elements in the GUI
	 */
	public void buildGUI() {
		setLayout(new FlowLayout()); 

		texta = new JTextPane(); 
		texta.setEditable(false);
		texta.setBackground(Color.WHITE);
		texta.setForeground(Color.BLACK);
		texta.setCaretPosition(texta.getDocument().getLength());

		scrollPane = new JScrollPane(texta);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(675, 300));
		scrollPane.setLocation(0, 0);
		add(scrollPane);

		message = new JTextPane();
		message.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, false), "shiftenter");
		message.getActionMap().put("shiftenter", new shiftEnter());
		message.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0, false), "sendText");
		message.getActionMap().put("sendText", new sendText());
		message.getDocument().addDocumentListener(privateGUIController);
		
		scrollMessage = new JScrollPane(message);
		scrollMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollMessage.setPreferredSize(new Dimension(500, 100));
		scrollMessage.setLocation(0, 320);
		add(scrollMessage);

		send = new JButton("Send");
		send.setEnabled(false);
		send.addActionListener(privateGUIController);
		send.setPreferredSize(new Dimension(170, 100));
		add(send);

		attach = new JButton("attach");
		attach.addActionListener(privateGUIController);
		add(attach);
		
		frame = new JFrame(conn.other.toString());
		frame.addWindowListener(privateGUIController);
		this.setBackground(Color.WHITE);
		frame.setContentPane(this);
		frame.setResizable(false);
		frame.setSize(700, 500);
		frame.setVisible(true);
		try {
			frame.setIconImage(ImageIO.read(new File("msn.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void appendText(String text) {
		String oldText = texta.getText();
		texta.setText(oldText + text);
	}
	
	/**
	 * Sends the message to another user.
	 * @param text the text typed in by the users in the JTextPane
	 */
	public void sendMessage(String text) {
		String oldText = texta.getText();
		texta.setText(oldText + client.getLocalAddress().getHostName() + " (" + new Date(System.currentTimeMillis())
				+ "):" + System.lineSeparator() + " " + text + System.lineSeparator());
		message.setText(null);
		conn.sendMessage(text);
	}

	/**
	 * Shows a received message in the GUI
	 * @param text the received message
	 */
	public void messageReceived(String text) {
		String oldText = texta.getText();
		AudioPlayer a = new AudioPlayer();
		a.start();
		a.playSound(Constants.SONGS[0]);
		this.requestFocus();
		message.requestFocusInWindow();
		if (getNotification)
			flickicon.start();
		texta.setText(oldText + text + System.lineSeparator());
		try {
			a.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		texta.setCaretPosition(texta.getDocument().getLength());
	}
	
	@SuppressWarnings("serial")
	public class shiftEnter extends AbstractAction{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String tempTXT = message.getText();
			tempTXT = tempTXT + System.lineSeparator();
			message.setText(tempTXT);
		}
	};
	
	@SuppressWarnings("serial")
	public class sendText extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
				send.doClick();	
		}
	};
	
	public class PrivateGUIController implements WindowListener, ActionListener, DocumentListener{
		private PrivateGUI privateGUI;
		
		public PrivateGUIController(PrivateGUI privateGUI) {
			this.privateGUI = privateGUI;
		}
		
		@Override
		public void windowActivated(WindowEvent arg0) {
			getNotification = false;
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			getNotification = false;
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			client.getGUI().removePGUI(conn.other);
			conn.stop();
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			getNotification = true;
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			getNotification = false;
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			getNotification = true;
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			getNotification = false;
		}
		/**
		 * Checks if an action in the GUI has taken place and does an appropriate follow-up
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == send && (!message.getText().isEmpty())) {
				sendMessage(message.getText());
			}
			if (e.getSource() == attach) {			
				JFrame fileChooser = new JFrame();
				fileChooser.setSize(700, 500);
				fc = new JFileChooser();
				fileChooser.add(fc);
				fileChooser.setVisible(false);
				fc.addChoosableFileFilter(Constants.docFilter);
				fc.addChoosableFileFilter(Constants.pdfFilter);
				fc.addChoosableFileFilter(Constants.xlsFilter);
				fc.addChoosableFileFilter(Constants.jpgFilter);

				int result = fc.showOpenDialog(privateGUI);
				if (result == JFileChooser.APPROVE_OPTION) {
					fileChooser.dispatchEvent(new WindowEvent(fileChooser, WindowEvent.WINDOW_CLOSING));
					File file = fc.getSelectedFile();
					Encoder encoder = new Encoder(file.getPath());
					conn.sendFile(new FilePacket(file.getName(), encoder.encode()));
					appendText(client.getLocalAddress() + " sent the file: " + file.getName() + System.lineSeparator());
//					String typedtext = message.getText();
//					message.setText(typedtext + " " + fc.getSelectedFile().toString());
//					if (fc.getSelectedFile().toString().substring(fc.getSelectedFile().toString().lastIndexOf("."),
//							fc.getSelectedFile().toString().length()) == ".jpg") {
//						message.insertIcon(new ImageIcon());
//					}
//					System.out.println(fc.getSelectedFile().toString().substring(
//							fc.getSelectedFile().toString().lastIndexOf("."), fc.getSelectedFile().toString().length()));
				}
			}
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			disableButtonIfEmpty(e);
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			disableButtonIfEmpty(e);
			
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			disableButtonIfEmpty(e);	
		}
		
		public void disableButtonIfEmpty(DocumentEvent e) {
			send.setEnabled(e.getDocument().getLength() > 0);
		}
	};
}