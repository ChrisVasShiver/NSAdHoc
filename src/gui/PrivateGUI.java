package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.Client;
import network.Connection;

public class PrivateGUI extends JPanel implements ActionListener  {
	
	private JTextArea texta, message;
	private JTextField txtf1;
	private JButton send;
	private Client client;
	private InetAddress other;
	private Connection conn;
	//private MouseListener userSelector;
	//private JPane test;

    public PrivateGUI(Client client, InetAddress me, InetAddress other, Connection conn) {
    	this.client = client;
    	this.other = other;
    	this.conn = conn;
    	JFrame frame = new JFrame(other.toString());
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setBackground(Color.WHITE);
        frame.setContentPane(this);
        frame.setSize(700, 500);
        frame.setVisible(true);
        buildGUI();
    }

    public void buildGUI() {
        setLayout(new FlowLayout()); // Always set the layout before you add components

        // you can use null layout, but you have to use setBounds() method 
        //      for placing the components. For an advanced layout see the 
        //      tutorials for GridBagLayout and mixing layouts with each other.

        texta = new JTextArea(); // Do not mix AWT component with 
                                  //    Swing (J components. See the packages)
        //textf.setSize(40, 40); // Use setPreferredSize instead
        texta.setLineWrap(true);
        texta.setWrapStyleWord(true);
        
        texta.setEditable(false); // Text fields are for getting data from user
                                  //    If you need to show something to user
                                  //    use JLabel instead.
        texta.setBackground(Color.WHITE);
        texta.setForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(texta);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(675, 300));
        scrollPane.setLocation(0, 0);
        add(scrollPane);
        
        message = new JTextArea();
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        JScrollPane scrollMessage= new JScrollPane(message);
        scrollMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollMessage.setPreferredSize(new Dimension(500, 100));
        scrollMessage.setLocation(0, 320);
        add(scrollMessage);
        
        JButton send = new JButton("Send");
        send.addActionListener(this);
        send.setPreferredSize(new Dimension(170, 100));
        add(send);
    }

    public void actionPerformed(ActionEvent e) {
    	String text = message.getText();
    	sendMessage(text);
	}
    
    public void sendMessage(String text) {
    	texta.append(client.getLocalAddress().getHostName() + " (" 
    			+ new Date(System.currentTimeMillis()) + "):" 
    			+ System.lineSeparator() + " " + text + System.lineSeparator());
    	message.setText(null);
    	conn.sendMessage(text);
    }
    public void messageReceived(String text) {
    	texta.append(text + System.lineSeparator());
    }
}