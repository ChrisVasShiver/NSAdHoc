package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import helper.DistanceVectorEntry;
import helper.Packet;
import main.Client;
import threads.TimerThread;
import threads.UniListeningThread;

public class Connection implements Observer {

	private TimerThread timerRunnable;
	private Thread timerThread;
	public final InetAddress[] other;
	private Client client;
	private int lastSeqnr = 1;
	private int lastAcknr = 1;

	public Connection(Client client, InetAddress other) {
		this.other = new InetAddress[]{other};
		this.client = client;
		timerRunnable = new TimerThread();
		timerThread = new Thread(timerRunnable);
		timerThread.start();
		timerRunnable.addObserver(this);
		client.ulRunnable.addObserver(this);
		sendSYN();
	}
	
	public Connection(Client client, InetAddress[] other) {
		this.other = other;
		this.client = client;
		timerRunnable = new TimerThread();
		timerThread = new Thread(timerRunnable);
		timerThread.start();
		timerRunnable.addObserver(this);
		client.ulRunnable.addObserver(this);
		sendSYN();
	}

	public void stop() {
		timerRunnable.wait = false;
		try {
			timerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.ulRunnable.deleteObserver(this);
		sendFIN();
	}

	public void sendMessage(String message) {
		for(int i = 0; i < other.length; i++) {
		Packet packet = new Packet(client.getLocalAddress(), other[i], lastSeqnr, 0, (byte) 0, System.currentTimeMillis(),
				0,0, message);
		sendPacket(packet);
		}
	}

	public void sendSYN() {
		for (int i = 0; i < other.length; i++) {
			Packet packet = new Packet(client.getLocalAddress(), other[i], lastSeqnr, 0, Packet.SYN,
					System.currentTimeMillis(), 0,0, null);
			// TODO send public key
			sendPacket(packet);
		}

	}

	public void sendFIN() {
		for(int i = 0; i < other.length; i++) {
		Packet packet = new Packet(client.getLocalAddress(), other[i], lastSeqnr, 0, Packet.FIN,
				System.currentTimeMillis(),0,0, null);
		sendPacket(packet);
		}
	}

	public void sendPacket(Packet packet) {
		for(int i = 0; i < other.length; i++) {
		DistanceVectorEntry dve = client.routingTable.get(other[i]);
		if (dve == null) {
			System.out.println("Address " + other[i].getHostName() + " is not in your routing table.");
			return;
		}
		DatagramPacket dpack = new DatagramPacket(packet.getBytes(), packet.getBytes().length, dve.nextHop,
				client.uniPort);
		try {
			client.uniSocket.send(dpack);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		timerRunnable.put(lastSeqnr, packet);
		lastSeqnr++;
		}
	}

	public void receiveMessage(Packet packet) {
		switch (packet.getFlag()) {
		case Packet.ACK:
			timerRunnable.remove(packet.getAckNr());
			lastAcknr = packet.getAckNr();
			break;
		case Packet.SYN:
			client.startPrivateGUI(packet.getSrc());
			// TODO
			break;
		case Packet.FIN:
			client.stopPrivateGUI(packet.getSrc());
			// TODO
			break;
		default:
			String message = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
					+ System.lineSeparator() + " " + packet.getData();
			client.messageReceived(packet.getSrc(), message);
			break;
		}
	}

	@Override
	public void update(Observable arg0, Object packet) {
		if (arg0 instanceof UniListeningThread) {
			if (((Packet) packet).getDest().equals(client.getLocalAddress())
					&& ((Packet) packet).getSrc().equals(other)) {
				receiveMessage((Packet) packet);
			}
		}
		if (arg0 instanceof TimerThread) {
			Packet newPacket = (Packet) packet;
			newPacket.setTimeStamp(System.currentTimeMillis());
			sendPacket(newPacket);
		}

	}
}
