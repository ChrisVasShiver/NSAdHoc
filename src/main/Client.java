package main;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import network.DistanceVectorEntry;
import threads.DistanceVectorThread;
import threads.MultiListeningThread;
import threads.UniListeningThread;

public class Client {
	public ConcurrentHashMap<InetAddress, DistanceVectorEntry> routingTable = new ConcurrentHashMap<InetAddress, DistanceVectorEntry>();
	private Scanner in = new Scanner(System.in);
	
	private Thread dvThread;
	private Thread mlThread;
	private Thread ulThread;
	private DistanceVectorThread dvRunnable;
	private MultiListeningThread mlRunnable;
	private UniListeningThread ulRunnable;
	
	public MulticastSocket multiSocket;
	public InetAddress group;
	public final int multiPort = 6789;
	public DatagramSocket uniSocket;
	public final int uniPort = 7000;
	
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
		stopThreads();
	}
	
	public InetAddress getLocalAddress() {
		InetAddress result = null;
		try {
			result = InetAddress.getLocalHost();
		} catch (UnknownHostException e) { e.printStackTrace();}
		return result;
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
		dvRunnable = new DistanceVectorThread(this);
		mlRunnable = new MultiListeningThread(this);
		ulRunnable = new UniListeningThread();
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

}