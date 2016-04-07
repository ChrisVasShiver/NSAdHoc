package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.InetAddress;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Client;
import network.Connection;

// Use upper Case in the start of you class names:
public class GUI extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea texta, message;
	//private JTextField txtf1;
//	private JList<InetAddress> users;
	public DefaultListModel<InetAddress> userList;
	private JButton send;
	private JButton attach;
	public HashMap<InetAddress, PrivateGUI> pGUIs = new HashMap<InetAddress, PrivateGUI>();
	private JFileChooser fc;
	private Client client;
	// private MouseListener userSelector;
	// private JPane test;

	public GUI(Client client) {
		this.client = client;
		buildGUI();
	}

	public void buildGUI() {
		setLayout(new FlowLayout()); // Always set the layout before you add
										// components

		// you can use null layout, but you have to use setBounds() method
		// for placing the components. For an advanced layout see the
		// tutorials for GridBagLayout and mixing layouts with each other.

		texta = new JTextArea(); // Do not mix AWT component with
									// Swing (J components. See the packages)
		// textf.setSize(40, 40); // Use setPreferredSize insteadtest
		texta.setLineWrap(true);
		texta.setWrapStyleWord(true);

		/*
		 * texta.setText("Dit is een test\n"); texta.append("Dit is een test\n"
		 * ); texta.append("Dit is een test\n"); texta.append(
		 * "Dit is een test\n"); texta.append("Dit is een test\n");
		 * texta.append("Dit is een test\n"); texta.append("Dit is een test\n");
		 * texta.append("Dit is een test\n"); texta.append("Dit is een test\n");
		 * texta.append("Dit is een test\n"); texta.append("Dit is een test\n");
		 */

		texta.setEditable(false); // Text fields are for getting data from user
									// If you need to show something to user
									// use JLabel instead.
		texta.setBackground(Color.WHITE);
		texta.setForeground(Color.BLACK);
		JScrollPane scrollPane = new JScrollPane(texta);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		scrollPane.setLocation(0, 0);

		// JScrollBar scroller = new JScrollBar();
		// scroller.setOrientation(JScrollBar.VERTICAL);
		// scroller.setMinimum(1);
		// scroller.setMaximum(100);

		// scrollPane.add(scroller);
		add(scrollPane);
		// add(scroller);

		userList = new DefaultListModel<InetAddress>();
		// userList.addElement("Matthijs");
		// userList.addElement("Bas");
		// userList.addElement("Christiaan");
		// userList.addElement("Thierry");
		JList<InetAddress> users = new JList<InetAddress>(userList);
		users.addMouseListener(userSelector);
		users.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		users.setLayoutOrientation(JList.VERTICAL);
		users.setVisibleRowCount(-1);

		JScrollPane scrollUsers = new JScrollPane(users);
		scrollUsers.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollUsers.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollUsers.setPreferredSize(new Dimension(170, 300));
		add(scrollUsers);

		// txtf1 = new JTextField();
		// txtf1.setSize(40, 40); Use setPreferredSize instead
		// txtf1.setPreferredSize(new Dimension(40, 40));
		// txtf1.getText();
		// txtf1.setEditable(false);
		// txtf1.setBackground(Color.WHITE);
		// txtf1.setForeground(Color.BLACK);
		// add(txtf1);

		// JButton b = new JButton("Click ME!");
		// b.addActionListener(this);
		// add(b);

		message = new JTextArea();
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		JScrollPane scrollMessage = new JScrollPane(message);
		scrollMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollMessage.setPreferredSize(new Dimension(500, 100));
		scrollMessage.setLocation(0, 320);
		add(scrollMessage);

		send = new JButton("send");
		send.addActionListener(this);
		send.setPreferredSize(new Dimension(170, 100));
		add(send);

		attach = new JButton("attach");
		attach.addActionListener(this);
		add(attach);

		// add(fc);

	}

	public void actionPerformed(ActionEvent e) {
		// System.out.println("kom ik hier");
		// System.out.println(e.getSource().toString());
		// System.out.println(send);
		if (e.getSource() == send) {
			// System.out.println("nu kom ik er wel");
			String text = message.getText();
			texta.append(text + System.lineSeparator());
			message.setText(null);
			// TODO Auto-generated method stub
			// Hier kan verzendmethode komen
		}
		if (e.getSource() == attach) {
			System.out.println("attach");
			JFrame fileChooser = new JFrame();
			fileChooser.setSize(700, 500);
			fc = new JFileChooser();
			fileChooser.add(fc);
			fileChooser.setVisible(true);
		}
	}
	/*
	 * public void actionPerformed(ActionEvent e) { String text =
	 * message.getText(); texta.append(text + System.lineSeparator());
	 * message.setText(null); // TODO Auto-generated method stub //Hier kan
	 * verzendmethode komen
	 * 
	 * }
	 */

	MouseListener userSelector = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList users = (JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				privateGUI((InetAddress) users.getSelectedValue());
				// System.out.println("dubbelklik");
				// JOptionPane.showMessageDialog(GUI.this, "Sjaak!");

			}
		}

		private void privateGUI(InetAddress other) {
			if (pGUIs.get(other) == null) {
				Connection conn = new Connection(client, other);
				PrivateGUI pGUI = new PrivateGUI(client, client.getLocalAddress(), other, conn, pGUIs);
				pGUIs.put(other, pGUI);
			}
		}
	};

	/*
	 * public void jListUsernameMouseClicked(java.awt.event.MouseEvent evt){
	 * System.out.println("klik"); JList users = (JList)evt.getSource();
	 * if(evt.getClickCount() == 2){ System.out.println("dubbelklik");
	 * JOptionPane.showMessageDialog(GUI.this, "\"textf\" Sjaak!"); } }
	 */

	public static void main(String[] args) {
		// JFrame frame = new JFrame("Chatbox");
		// frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// GUI gui = new GUI();
		// gui.setBackground(Color.WHITE);
		// frame.setContentPane(gui);
		// frame.setSize(700, 500);
		// frame.setVisible(true);
		// TODO Auto-generated method stub
		// Hier kan verzendmethode komen

	}

}