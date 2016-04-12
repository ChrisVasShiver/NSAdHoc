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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import helper.Constants;

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
	private JButton attach;
	private HashMap<InetAddress, PrivateGUI> pGUIs = new HashMap<InetAddress, PrivateGUI>();
	private JFileChooser fc;
	private Client client;
	private MultiConnection connections;
	/**
	 * Constructor of the GUI
	 * @param client the client
	 */
	public GUI(Client client) {
		guiController = new GUIController(this);
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

		attach = new JButton("attach");
		attach.addActionListener(guiController);
		add(attach);
		
		frame = new JFrame("Chatbox");
		frame.addWindowListener(guiController);
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    setBackground(Color.WHITE);
	    frame.setContentPane(this);
	    frame.setSize(700, 500);
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
		String oldText = texta.getText();
		texta.setText(oldText + client.getLocalAddress().getHostName() + " (" + new Date(System.currentTimeMillis())
				+ "):" + System.lineSeparator() + " " + text + System.lineSeparator());
		message.setText(null);
		connections.sendMessage(text);
	}

	public void setText(String message) {
		this.texta.setText(texta.getText() + System.lineSeparator() + message);
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
		private GUI gui;
		
		public GUIController(GUI gui) {
			this.gui = gui;
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
				fileChooser.setVisible(false);
				fc.addChoosableFileFilter(Constants.docFilter);
				fc.addChoosableFileFilter(Constants.pdfFilter);
				fc.addChoosableFileFilter(Constants.xlsFilter);
				fc.addChoosableFileFilter(Constants.jpgFilter);

				int result = fc.showOpenDialog(this.gui);
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
		//	client.stop();
		// TODO Auto-generated method stub
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