package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import main.Client;

import network.MultiConnection;
import network.SingleConnection;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class GUI extends JPanel{
	private static final long serialVersionUID = 1L;
	private GUIController guiController;
	private JFrame frame;
	private JTextPane texta, message;
	private JList<InetAddress> users;
	private JScrollPane scrollUsers;
	private DefaultListModel<InetAddress> userList = new DefaultListModel<InetAddress>();
	private JButton send;
	private HashMap<InetAddress, PrivateGUI> pGUIs = new HashMap<InetAddress, PrivateGUI>();
	private Client client;
	private MultiConnection connections;
	/**
	 * Constructor of the GUI
	 * @param client the client
	 */
	public GUI(Client client) {
		guiController = new GUIController();
		this.client = client;
		this.connections = new MultiConnection(client);
		buildGUI();
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

		JScrollPane scrollPane = new JScrollPane(texta);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		scrollPane.setLocation(0, 0);
		add(scrollPane);

		userList = new DefaultListModel<InetAddress>();
		
		users = new JList<InetAddress>(userList);
		users.addMouseListener(userSelector);
		users.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		users.setLayoutOrientation(JList.VERTICAL);
		users.setVisibleRowCount(-1);

		scrollUsers = new JScrollPane(users);
		scrollUsers.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollUsers.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollUsers.setPreferredSize(new Dimension(170, 300));
		add(scrollUsers);

		message = new JTextPane();
		message.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, false),
				"shiftenter");
		message.getActionMap().put("shiftenter", new shiftEnter());
		message.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "sendText");
		message.getActionMap().put("sendText", new sendText());
		message.getDocument().addDocumentListener(guiController);
		
		JScrollPane scrollMessage = new JScrollPane(message);
		scrollMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollMessage.setPreferredSize(new Dimension(500, 100));
		scrollMessage.setLocation(0, 320);
		add(scrollMessage);

		send = new JButton("send");
		send.setEnabled(false);
		send.addActionListener(guiController);
		send.setPreferredSize(new Dimension(170, 100));
		add(send);
		
		frame = new JFrame("Chatbox");
		frame.addWindowListener(guiController);
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    setBackground(Color.WHITE);
	    frame.setContentPane(this);
	    frame.setSize(700, 455);
	    frame.setResizable(false);
	    frame.setVisible(true);
	    try {
	    	frame.setIconImage(ImageIO.read(new File("msn.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the currecnt time
	 * @return a string of the current time in HH:mm:ss
	 */
	public String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return " (" + sdf.format(cal.getTime()) + ") ";
	}
	
	/**
	 * Gets the HashMap containing all the opened private GUIs
	 * @return a HashMap with InetAddress as keys and privateGUI as values
	 */
	public HashMap<InetAddress, PrivateGUI> getPGUIs() {
		return pGUIs;
	}

	public DefaultListModel<InetAddress> getUserList() {
		return userList;
	}
	
	
	/**
	 * Checks if an action in the GUI has taken place and does an appropriate follow-up
	 */


	public MouseListener userSelector = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			@SuppressWarnings("unchecked")
			JList<InetAddress> users = (JList<InetAddress>) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				privateGUI((InetAddress) users.getSelectedValue(), true);
			}
		}
	};

	public void privateGUI(InetAddress other, boolean initiated) {
		if (pGUIs.get(other) == null) {
			SingleConnection conn = new SingleConnection(client, other);
			if (initiated)
				conn.sendSYN();
			PrivateGUI pGUI = new PrivateGUI(client, client.getLocalAddress(), conn);
			pGUIs.put(other, pGUI);
		} else {
			pGUIs.get(other).requestFocus();
		}
	}

	public void sendMessage(String text) {
		//String oldText = texta.getText();
		//texta.setText(oldText + client.getLocalAddress().getHostName() + " (" + new Date(System.currentTimeMillis())
		//		+ "):" + System.lineSeparator() + " " + text + System.lineSeparator());
		message.setText(null);
		connections.sendMessage(text);
	}

	public void setText(String message) {
		this.texta.setText(texta.getText() + System.lineSeparator() + message);
		texta.setCaretPosition(texta.getDocument().getLength());
	}
	
	public void removePGUI(InetAddress other) {
		pGUIs.remove(other);
	}


	
	@SuppressWarnings("serial")
	public class shiftEnter extends AbstractAction{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			message.setText(message.getText() + System.lineSeparator());
		}
	};
	
	@SuppressWarnings("serial")
	public class sendText extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
				send.doClick();	
		}
	}
	
	public class GUIController implements DocumentListener, ActionListener, WindowListener{		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == send && (!message.getText().isEmpty())) {
				sendMessage(message.getText());
			}
		}
		
		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			client.stop();
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub

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