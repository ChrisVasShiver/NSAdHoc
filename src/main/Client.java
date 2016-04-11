package main;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import gui.GUI;
import gui.PrivateGUI;
import helper.DistanceVectorEntry;
import helper.RoutingTable;
import threads.DistanceVectorThread;
import threads.MultiListeningThread;
import threads.UniListeningThread;

public class Client implements Observer {
	public RoutingTable routingTable = new RoutingTable();
	public ConcurrentHashMap<InetAddress, Long> neighbourTimeout = new ConcurrentHashMap<InetAddress, Long>();
	
	private Thread dvThread;
	private Thread mlThread;
	private Thread ulThread;
	private DistanceVectorThread dvRunnable;
	private MultiListeningThread mlRunnable;
	public UniListeningThread ulRunnable;
	
	private GUI gui;
	
	public MulticastSocket multiSocket;
	public InetAddress group;
	public final int multiPort = 6789;
	public DatagramSocket uniSocket;
	public final int uniPort = 7000;
	public static final int sendTimeout = 3000;
	public static final int MAX_PACKET_SIZE = 1024;
	
	public Client() {
		this.gui = new GUI(this);
		routingTable.addObserver(this);
		try {
			group = InetAddress.getByName("228.0.0.2");
			multiSocket = new MulticastSocket(multiPort);
			multiSocket.joinGroup(group);
			uniSocket = new DatagramSocket(uniPort);
		} catch (IOException e) {
			//TODO
			e.printStackTrace();
		}
		InetAddress localAddress = getLocalAddress();
		DistanceVectorEntry defaultEntry = new DistanceVectorEntry(localAddress, 0, localAddress);
		if(localAddress != null)
			routingTable.put(localAddress, defaultEntry);
	}
	
	public void start() {
		startThreads();
		startGUI();
		
	}
	
	public void stop() {
		stopThreads();
		multiSocket.close();
		uniSocket.close();
		System.out.println("Client closed");
	}
	
	public InetAddress getLocalAddress() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while(networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
				Enumeration<InetAddress> nias = ni.getInetAddresses();
				while(nias.hasMoreElements()) {
					InetAddress ia = (InetAddress) nias.nextElement();
					if(!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
						return ia;
					}
				}
			}
		} catch(SocketException e) {} 
		return null;
	}
	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}
	
	public void startGUI() {
		 JFrame frame = new JFrame("Chatbox");
	     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	     gui.setBackground(Color.WHITE);
	     frame.setContentPane(gui);
	     frame.setSize(700, 500);
	     frame.setVisible(true);
	     try {
			frame.setIconImage(ImageIO.read(new File("msn.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startThreads() {
		dvRunnable = new DistanceVectorThread(this);
		mlRunnable = new MultiListeningThread(this);
		ulRunnable = new UniListeningThread(this);
		dvThread = new Thread(dvRunnable);
		mlThread = new Thread(mlRunnable);
		ulThread = new Thread(ulRunnable);
		dvThread.start();
		mlThread.start();
		ulThread.start();
	}
	
	private void stopThreads() {
		dvRunnable.wait = false;
		mlRunnable.wait = false;
		ulRunnable.wait = false;
		try {
			dvThread.join();
			mlThread.join();
			ulThread.join();
		} catch(InterruptedException e) {
			//TODO
			e.printStackTrace();
		}
		
	}

	public void startPrivateGUI(InetAddress address) {
		System.out.println("Client new window");
		gui.privateGUI(address);
	}
			
	public void stopPrivateGUI(InetAddress address) {
		PrivateGUI pGUI = gui.pGUIs.get(address);
		if (pGUI != null){
			String oldText = pGUI.texta.getText();
			pGUI.texta.setText(oldText + address.getHostName() + " closed the connection" + System.lineSeparator());
		}
	}

	public void messageReceived(InetAddress source, String message) {
		for(InetAddress address : gui.pGUIs.keySet()) {
			if(source.equals(address)) {
				gui.pGUIs.get(address).messageReceived(message);
			}
		}
	}
	
	public void update(Observable arg0, Object arg1) {
		gui.userList.removeAllElements();
		for(InetAddress address : routingTable.keySet()) {
			if(!address.equals(getLocalAddress())) {
				gui.userList.addElement(address);
				gui.setGroupConnetions();
			}
	     }
		
	}
}