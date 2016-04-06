package main;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import network.DistanceVectorEntry;
import threads.DistanceVectorThread;
import threads.MultiListeningThread;
import threads.UniListeningThread;

class Client {
	private ConcurrentHashMap<InetAddress, DistanceVectorEntry> routingTable = new ConcurrentHashMap<InetAddress, DistanceVectorEntry>();
	private Scanner in = new Scanner(System.in);
	
	private Thread dvThread;
	private Thread mlThread;
	private Thread ulThread;
	private DistanceVectorThread dvRunnable;
	private MultiListeningThread mlRunnable;
	private UniListeningThread ulRunnable;
	
	private MulticastSocket multiSocket;
	private InetAddress group;
	private int multiPort = 6789;
	private DatagramSocket uniSocket;
	private int uniPort = 7000;
	
	public Client() {
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
		handleUserInput();
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}
	
	private void handleUserInput() {
		String line = "";
		do {
			line = in.nextLine();
		} while(!line.equals("quit"));
	}
	
	private void startThreads() {
		dvRunnable = new DistanceVectorThread();
		mlRunnable = new MultiListeningThread();
		ulRunnable = new UniListeningThread();
		dvThread = new Thread(dvRunnable);
		mlThread = new Thread(mlRunnable);
		ulThread = new Thread(ulRunnable);
		dvThread.start();
		mlThread.start();
		ulThread.start();
	}
	
	private void stopThreads() {
		
	}

}