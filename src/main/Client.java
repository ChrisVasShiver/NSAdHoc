package main;

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

import gui.GUI;
import gui.PrivateGUI;
import helper.DistanceVectorEntry;
import helper.RoutingTable;
import threads.DistanceVectorThread;
import threads.MultiListeningThread;
import threads.UniListeningThread;
/**
 * Class for the client
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
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
	public static final int multiPort = 6789;
	public DatagramSocket uniSocket;
	public static final int uniPort = 7000;
	public static final int sendTimeout = 3000;
	public static final int MAX_PACKET_SIZE = 1024;
	
	public Client() {
		routingTable.addObserver(this);
		try {
			group = InetAddress.getByName("228.0.0.2");
			multiSocket = new MulticastSocket(multiPort);
			multiSocket.joinGroup(group);
			uniSocket = new DatagramSocket(uniPort);
		} catch (IOException e) { System.out.println("Could not start the client, try restarting");}
		InetAddress localAddress = getLocalAddress();
		DistanceVectorEntry defaultEntry = new DistanceVectorEntry(localAddress, 0, localAddress);
		if(localAddress != null)
			routingTable.put(localAddress, defaultEntry);
	}
	
	/** 
	 * Starting point of the program
	 * @param args
	 */
	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}
	
	/**
	 * Start the threads and the GUI
	 */
	public void start() {
		startThreads();	
		this.gui = new GUI(this);
	}
	
	/**
	 * Stop the client and the threads
	 */
	public void stop() {
		stopThreads();
	}
	
	/**
	 * Get the local address, will loop over all the interfaces and ignores LinkLocalAddresses and LoopbackAddresses
	 * Will only return a IPv4 address
	 * @return The client local address or null if it cannot be found
	 */
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
	
	/**
	 * Getter for the GUI
	 * @return the GUI object associated with this client
	 */
	public GUI getGUI() {
		return gui;
	}
	
	/**
	 * Start the threads for this GUI
	 */
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
	
	/** 
	 * Stop the threads for this GUI
	 */
	private void stopThreads() {
		dvRunnable.wait = false;
		mlRunnable.wait = false;
		ulRunnable.wait = false;
		multiSocket.close();
		uniSocket.close();
		try {
			dvThread.join();
			mlThread.join();
			ulThread.join();
		} catch(InterruptedException e) { }
		
	}

	/**
	 * Start a private GUI for the specified address
	 * @param address
	 */
	public void startPrivateGUI(InetAddress address) {
		gui.privateGUI(address, false);
	}
			
	/**
	 * Stop a private GUI for the specified address
	 * @param address
	 */
	public void stopPrivateGUI(InetAddress address) {
		PrivateGUI pGUI = gui.getPGUIs().get(address);
		if (pGUI != null){
			String oldText = pGUI.getTextField().getText();
			pGUI.getTextField().setText(oldText + address.getHostName() + " closed the connection" + System.lineSeparator());
		}
	}

	/**
	 * Sets the message in the correct private GUI
	 * @param source The address of the other node in a private GUI
	 * @param message The message
	 */
	public void messageReceived(InetAddress source, String message) {
		for(InetAddress address : gui.getPGUIs().keySet()) {
			if(source.equals(address)) {
				gui.getPGUIs().get(address).messageReceived(message);
			}
		}
	}

	/**
	 * Sets the group message in the GUI
	 * @param message
	 */
	public void groupMessageReceived(String message) {
		gui.setText(message);
		
	}
	
	/**
	 * Is called when the routingTable is updated, will update the user list in GUI
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		if(gui != null) {
			gui.getUserList().removeAllElements();
			for(InetAddress address : routingTable.keySet()) {
				if(!address.equals(getLocalAddress()))
					gui.getUserList().addElement(address);
			}
		}
	}

}