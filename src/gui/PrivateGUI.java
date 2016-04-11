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
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import helper.AudioPlayer;
import main.Client;
import network.SingleConnection;

public class PrivateGUI extends JPanel implements ActionListener, WindowListener {

	private static final long serialVersionUID = 6883134327632102722L;
	public JTextPane texta, message;
	// private JTextField txtf1;
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
	FileFilter pdfFilter = new FileTypeFilter(".pdf", "PDF Documents");
	FileFilter xlsFilter = new FileTypeFilter(".xlsx", "Microsoft Excel Documents");
	FileFilter jpgFilter = new FileTypeFilter(".jpg", "JPG Image");

	public PrivateGUI(Client client, InetAddress me, SingleConnection conn,
			HashMap<InetAddress, PrivateGUI> pGUIs) {
		flickicon = new FlickIcon(this);
		this.client = client;
		this.conn = conn;
		this.pGUIs = pGUIs;
		frame = new JFrame(other.toString());
		frame.addWindowListener(this);
		this.setBackground(Color.WHITE);
		frame.setContentPane(this);
		frame.setSize(700, 500);
		frame.setVisible(true);
		try {
			frame.setIconImage(ImageIO.read(new File("msn.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buildGUI();
	}

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
		
		scrollMessage = new JScrollPane(message);
		scrollMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollMessage.setPreferredSize(new Dimension(500, 100));
		scrollMessage.setLocation(0, 320);
		add(scrollMessage);

		send = new JButton("Send");
		send.addActionListener(this);
		send.setPreferredSize(new Dimension(170, 100));
		add(send);

		attach = new JButton("attach");
		attach.addActionListener(this);
		add(attach);
	}
	


	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == send && (!message.getText().isEmpty())) {
			sendMessage(message.getText());
		}
		if (e.getSource() == attach) {			
			JFrame fileChooser = new JFrame();
			fileChooser.setSize(700, 500);
			fc = new JFileChooser();
			fileChooser.add(fc);
			fileChooser.setVisible(true);
			fc.addChoosableFileFilter(docFilter);
			fc.addChoosableFileFilter(pdfFilter);
			fc.addChoosableFileFilter(xlsFilter);
			fc.addChoosableFileFilter(jpgFilter);

			int result = fc.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				System.out.println("File opened");
				System.out.println(fc.getSelectedFile());
				fileChooser.dispatchEvent(new WindowEvent(fileChooser, WindowEvent.WINDOW_CLOSING));
				String typedtext = message.getText();
				message.setText(typedtext + " " + fc.getSelectedFile().toString());
				// System.out.println(fc.getSelectedFile().getParent());
				System.out.println(fc.getSelectedFile().getName() + "is verzonden");
				if (fc.getSelectedFile().toString().substring(fc.getSelectedFile().toString().lastIndexOf("."),
						fc.getSelectedFile().toString().length()) == ".jpg") {
					message.insertIcon(new ImageIcon());
				}
				System.out.println(fc.getSelectedFile().toString().substring(
						fc.getSelectedFile().toString().lastIndexOf("."), fc.getSelectedFile().toString().length()));
			} else if (result == JFileChooser.CANCEL_OPTION) {
				System.out.println("Open file was canceled.");
			}
		}
	}

	public void sendMessage(String text) {
		String oldText = texta.getText();
		texta.setText(oldText + client.getLocalAddress().getHostName() + " (" + new Date(System.currentTimeMillis())
				+ "):" + System.lineSeparator() + " " + text + System.lineSeparator());
		message.setText(null);
		conn.sendMessage(text);
	}

	public void messageReceived(String text) {
		String oldText = texta.getText();
		AudioPlayer a = new AudioPlayer();
		a.start();
		a.playSound("newmsg.wav");
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

	@Override
	public void windowActivated(WindowEvent arg0) {
		getNotification = false;
		// TODO Auto-generated catch block
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		getNotification = false;
		// TODO Auto-generated catch block
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
		getNotification = false;
		// TODO Auto-generated catch block
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		getNotification = true;
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		getNotification = false;
		// TODO Auto-generated catch block
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
}