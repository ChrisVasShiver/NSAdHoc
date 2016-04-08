package main;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

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
	private Scanner in = new Scanner(System.in);
	
	private Thread dvThread;
	private Thread mlThread;
	private Thread ulThread;
	private DistanceVectorThread dvRunnable;
	private MultiListeningThread mlRunnable;
	public UniListeningThread ulRunnable;
	
	private GUI gui = new GUI(this);
	
	public MulticastSocket multiSocket;
	public InetAddress group;
	public final int multiPort = 6789;
	public DatagramSocket uniSocket;
	public final int uniPort = 7000;
	public static final int sendTimeout = 3000;
	public static final int MAX_PACKET_SIZE = 2048;
	
	public Client() {
		InetAddress localAddress = getLocalAddress();
		routingTable.addObserver(this);
		DistanceVectorEntry defaultEntry = new DistanceVectorEntry(localAddress, 0, localAddress);
		routingTable.put(localAddress, defaultEntry);
		try {
			group = InetAddress.getByName("228.0.0.2");
			multiSocket = new MulticastSocket(multiPort);
			multiSocket.joinGroup(group);
			uniSocket = new DatagramSocket(uniPort);
		} catch (IOException e) {
			//TODO
			e.printStackTrace();
		}
	}
	
	public void start() {
		startThreads();
		startGUI();
		handleUserInput();
		stopThreads();
		multiSocket.close();
		uniSocket.close();
	}
	
	public InetAddress getLocalAddress() {
		InetAddress result = null;
		try {
			result = InetAddress.getLocalHost();
		} catch (UnknownHostException e) { e.printStackTrace();}
		return (result.isLoopbackAddress() ? null : result);
	}
	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}
	
	private void handleUserInput() {
		String line = "";
		do {
			line = in.nextLine();
			InetAddress dest;
			try {
				dest = InetAddress.getByName("192.168.5." + line.toCharArray()[0]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				continue;
			}
			sendMessage(dest, line);
			
		} while(!line.equals("quit"));
	}
	
	public void startGUI() {
		 JFrame frame = new JFrame("Chatbox");
	     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	     gui.setBackground(Color.WHITE);
	     frame.setContentPane(gui);
	     frame.setSize(700, 500);
	     frame.setVisible(true);
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

	public void sendMessage(InetAddress address, String message) {
		throw new UnsupportedOperationException();
	}

	public void messageReceived(InetAddress source, String message) {
		for(InetAddress address : gui.pGUIs.keySet()) {
			if(source.equals(address)) {
				gui.pGUIs.get(address).messageReceived(message);
			}
		}
	}
	
	public void startPrivateGUI(InetAddress address) {
		gui.privateGUI(address);
	}
	
	public void stopPrivateGUI(InetAddress address) {
		PrivateGUI pGUI = gui.pGUIs.get(address);
		if(pGUI != null)
			pGUI.texta.append(address.getHostName() + " closed the connection");
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		gui.userList.removeAllElements();
		for(InetAddress address : routingTable.keySet()) {
	    	 gui.userList.addElement(address);
	     }
	}
}