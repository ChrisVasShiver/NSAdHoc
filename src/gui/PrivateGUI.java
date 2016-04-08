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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

import helper.AudioPlayer;
import main.Client;
import network.Connection;

public class PrivateGUI extends JPanel implements ActionListener, WindowListener  {
	
	public JTextPane texta, message;
	private JTextField txtf1;
	private JButton send, attach;
	private Client client;
	private InetAddress other;
	private Connection conn;
	private HashMap<InetAddress, PrivateGUI> pGUIs;
	private JFileChooser fc;
	private JScrollPane scrollPane;
	private JScrollPane scrollMessage;
	JFrame frame;
	private FlickIcon flickicon;
	private boolean getNotification = false;
	//private MouseListener userSelector;
	//private JPane test;
	FileFilter docFilter = new FileTypeFilter(".docx", "Microsoft Word Documents");
    FileFilter pdfFilter = new FileTypeFilter(".pdf", "PDF Documents");
    FileFilter xlsFilter = new FileTypeFilter(".xlsx", "Microsoft Excel Documents");
    FileFilter jpgFilter = new FileTypeFilter(".jpg", "JPG Image");

    public PrivateGUI(Client client, InetAddress me, InetAddress other, Connection conn, HashMap<InetAddress, PrivateGUI> pGUIs) {
    	flickicon = new FlickIcon(this);
    	this.client = client;
    	this.other = other;
    	this.conn = conn;
    	this.pGUIs = pGUIs;
    	frame = new JFrame(other.toString());
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
        setLayout(new FlowLayout()); // Always set the layout before you add components

        // you can use null layout, but you have to use setBounds() method 
        //      for placing the components. For an advanced layout see the 
        //      tutorials for GridBagLayout and mixing layouts with each other.

        texta = new JTextPane(); // Do not mix AWT component with 
                                  //    Swing (J components. See the packages)
        //textf.setSize(40, 40); // Use setPreferredSize instead
        //texta.setLineWrap(true);
        //texta.setWrapStyleWord(true);
        
        texta.setEditable(false); // Text fields are for getting data from user
                                  //    If you need to show something to user
                                  //    use JLabel instead.
        texta.setBackground(Color.WHITE);
        texta.setForeground(Color.BLACK);
        texta.setCaretPosition(texta.getDocument().getLength());
        scrollPane = new JScrollPane(texta);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(675, 300));
        scrollPane.setLocation(0, 0);
        add(scrollPane);
        
        message = new JTextPane();
        //message.setLineWrap(true);
        //message.setWrapStyleWord(true);
        scrollMessage= new JScrollPane(message);
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
    
    public void sendMessage(String text) {
    	String oldText = texta.getText();
    	texta.setText(oldText + client.getLocalAddress().getHostName() + " (" 
    			+ new Date(System.currentTimeMillis()) + "):" 
    			+ System.lineSeparator() + " " + text + System.lineSeparator());
    	message.setText(null);
    	conn.sendMessage(text);
    }
    public void messageReceived(String text) {
    	String oldText = texta.getText();
    	AudioPlayer a = new AudioPlayer();
    	a.start();
    	a.playSound("newmsg.wav");
    	if (getNotification)
    		flickicon.run();
    	texta.setText(oldText + text + System.lineSeparator());
    	try {
			a.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /*public void flickicon(){
    	for(int counter =20; counter> 0; counter--){
    	try {
			frame.setIconImage(ImageIO.read(new File("msn_black.png")));
			flick.wait(500);
			frame.setIconImage(ImageIO.read(new File("msn.png")));
			flick.wait(500);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	}
    	
    }*/

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
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		getNotification = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		getNotification = false;		
		
	}
}