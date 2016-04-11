package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Text;

import main.Client;
import network.MultiConnection;
import network.SingleConnection;

// Use upper Case in the start of you class names:
public class GUI extends JPanel implements ActionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextPane texta, message;
	private JTextField txtf1;
	private JList<InetAddress> users;
	private JScrollPane scrollUsers;
	public DefaultListModel<InetAddress> userList = new DefaultListModel<InetAddress>();
	private JButton send;
	private JButton attach;
	public HashMap<InetAddress, PrivateGUI> pGUIs = new HashMap<InetAddress, PrivateGUI>();
	private JFileChooser fc;
	private Client client;
    private MultiConnection connections;
	// private MouseListener userSelector;
	// private JPane test;
	
	FileFilter docFilter = new FileTypeFilter(".docx", "Microsoft Word Documents");
	FileFilter pdfFilter = new FileTypeFilter(".pdf", "PDF Documents");
	FileFilter xlsFilter = new FileTypeFilter(".xlsx", "Microsoft Excel Documents");
	FileFilter jpgFilter = new FileTypeFilter(".jpg", "JPG Image"); 
	
	public GUI(Client client) {
		this.client = client;
		this.connections = new MultiConnection(client); 
		buildGUI();
	}

	public void buildGUI() {
		 setLayout(new FlowLayout()); // Always set the layout before you add components
		 
	        // you can use null layout, but you have to use setBounds() method
	        //      for placing the components. For an advanced layout see the
	        //      tutorials for GridBagLayout and mixing layouts with each other.
	 
	        texta = new JTextPane();
	       
	       
	        texta.setEditable(false); // Text fields are for getting data from user
	                                  //    If you need to show something to user
	                                  //    use JLabel instead.
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
	        message.addKeyListener(sendText);
	        
	        JScrollPane scrollMessage= new JScrollPane(message);
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

	}

	public String getTime() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	    return " (" + sdf.format(cal.getTime()) + ") ";
	}
	
	KeyListener sendText = new KeyAdapter(){
		   public void keyPressed(KeyEvent e){
		            if(e.getKeyCode() == KeyEvent.VK_ENTER){
		            e.consume();
		            send.doClick();
		            }
		        }
		   };
		   
		   
		   public void actionPerformed(ActionEvent e){
               if(e.getSource() == send && (!message.getText().isEmpty())){
            	   sendMessage(message.getText());
               }
	            if(e.getSource() == attach){
	                System.out.println("attach");
	                JFrame fileChooser = new JFrame();
	                fileChooser.setSize(700,500);
	                fc = new JFileChooser();
	                fileChooser.add(fc);
	                fileChooser.setVisible(true);
	               
	                fc.addChoosableFileFilter(docFilter);
	                fc.addChoosableFileFilter(pdfFilter);
	                fc.addChoosableFileFilter(xlsFilter);
	                fc.addChoosableFileFilter(jpgFilter);
	               
	                int result = fc.showOpenDialog(this);
	                if(result == JFileChooser.APPROVE_OPTION){
	                    System.out.println("File opened");
	                    System.out.println(fc.getSelectedFile());
	                    fileChooser.dispatchEvent(new WindowEvent(fileChooser, WindowEvent.WINDOW_CLOSING));
	                    String typedtext = message.getText();
	                    message.setText(typedtext + " " + fc.getSelectedFile().toString());
	                    //System.out.println(fc.getSelectedFile().getParent());
	                    System.out.println(fc.getSelectedFile().getName() + "is verzonden");
	                    if(fc.getSelectedFile().toString().substring(fc.getSelectedFile().toString().lastIndexOf("."),fc.getSelectedFile().toString().length()) == ".jpg"){
	                        message.insertIcon(new ImageIcon());
	                    }
	                    System.out.println(fc.getSelectedFile().toString().substring(fc.getSelectedFile().toString().lastIndexOf("."),fc.getSelectedFile().toString().length()));
	                }else if(result == JFileChooser.CANCEL_OPTION){
	                    System.out.println("Open file was canceled.");
	                }
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

	public MouseListener userSelector = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			@SuppressWarnings("unchecked")
			JList<InetAddress> users = (JList<InetAddress>) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				privateGUI((InetAddress) users.getSelectedValue());
			}
		}
	};

	public void privateGUI(InetAddress other) {
		System.out.println("Starting new window");
		if (pGUIs.get(other) == null) {
			SingleConnection conn = new SingleConnection(client, other, false);
			PrivateGUI pGUI = new PrivateGUI(client, client.getLocalAddress(), other, conn, pGUIs);
			pGUIs.put(other, pGUI);
		} else {
			pGUIs.get(other).requestFocus();
		}
	}
	
	public void setGroupConnections() {
		connections.setConnections();
	}
	
    public void sendMessage(String text) {
    	String oldText = texta.getText();
    	texta.setText(oldText + client.getLocalAddress().getHostName() + " (" 
    			+ new Date(System.currentTimeMillis()) + "):" 
    			+ System.lineSeparator() + " " + text + System.lineSeparator());
    	message.setText(null);
    	
    	connections.sendMessage(text);  	
    }
    

	public void setText(String message) {
		this.texta.setText(texta.getText() + System.lineSeparator() + message);
	}
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

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

		System.out.println("win1dowClostin");
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		System.out.println("windowClostin");
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


}