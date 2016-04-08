package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import helper.DistanceVectorEntry;
import helper.Packet;
import helper.PacketFragmenter;
import main.Client;
import threads.TimerThread;
import threads.UniListeningThread;

public class SingleConnection implements Observer {

	private TimerThread timerRunnable;
	private Thread timerThread;
	public final InetAddress other;
	private Client client;
	private int lastSeqnr = 1;
	private int lastAcknr = 1;
	private int lastPacketID = 1;
	private final int SWS = 10;
	private final int RWS = 10;
	private List<Packet> queue = new ArrayList<Packet>();
	private HashMap<Integer, List<Packet>> buffer = new HashMap<Integer, List<Packet>>();
	private List<Packet> sentWindow = new ArrayList<Packet>();
	
	public SingleConnection(Client client, InetAddress other) {
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

	private void addPackets(List<Packet> packets) {
		for(Packet packet : packets) {
			packet.setPacketID(lastPacketID);
			addPacket(packet);
		}
		lastPacketID++;
	}

	private void addPacket(Packet packet) {
		queue.add(packet);
		if(sentWindow.size() <= SWS)
			transmitPacket(queue.remove(1));
	}
	
	public void sendMessage(String message) {
			Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, (byte) 0, System.currentTimeMillis(),
					0,0, null);
			List<Packet> packets = PacketFragmenter.getPackets(header, Packet.dataToByteArray(message));
			addPackets(packets);
	}

	public void sendSYN() {
			Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.SYN,
					System.currentTimeMillis(), 0,0, null);
			// TODO send public key
			// TODO sendPacket(packet);
			addPacket(packet);
	}

	public void sendFIN() {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.FIN,
				System.currentTimeMillis(),0,0, null);
		// TODO sendPacket(packet);
		addPacket(packet);
	}

	private void sendPacket(Packet packet) {
		DistanceVectorEntry dve = client.routingTable.get(other);
		if (dve == null) {
			System.out.println("Address " + other.getHostName() + " is not in your routing table.");
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

		timerRunnable.put(packet.getSeqNr(), packet);
	}

	private void retransmitPacket(Packet packet) {
		sendPacket(packet);
	}
	
	private void transmitPacket(Packet packet) {
		packet.setSeqNr(lastSeqnr);
		sentWindow.add(packet);
		sendPacket(packet);
		lastSeqnr++;
	}
	
	public void receiveMessage(Packet packet) {
		switch (packet.getFlag()) {
		case Packet.ACK + Packet.SYN:
			// TODO
			// No break; (INTENTIONAL!)
		case Packet.ACK:
			timerRunnable.remove(packet.getAckNr());
			lastAcknr = packet.getAckNr();
			for(int i = 0; i < sentWindow.size(); i++) {
				Packet acked = sentWindow.get(i);
				if(acked.getSeqNr() == packet.getAckNr()) {
					sentWindow.remove(i);
					if(sentWindow.size() <= SWS)
						transmitPacket(queue.remove(1));
				}
			}
			break;
		case Packet.SYN:
			client.startPrivateGUI(packet.getSrc());
			sendACK(packet);
			// TODO
			break;
		case Packet.FIN:
			client.stopPrivateGUI(packet.getSrc());
			sendACK(packet);
			// TODO
			break;
		case Packet.FRG:
		case Packet.LST:
			List<Packet> packets = buffer.get(packet.getPacketID());
			if(packets == null) {
				packets = new ArrayList<Packet>();
				packets.add(packet);
				buffer.put(packet.getPacketID(), packets);	
			} else {
				packets.add(packet);
			}
			if(checkBuffer(packet.getPacketID()))
				flushBuffer(packet.getPacketID());
			sendACK(packet);
			//TODO
			break;
		default:
			String message = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
					+ System.lineSeparator() + " " + packet.getData();
			client.messageReceived(packet.getSrc(), message);
			sendACK(packet);
			break;
		}
	}

	private boolean checkBuffer(Integer ID) {
		List<Packet> packets = buffer.get(ID);
		Collections.sort(packets);
		for(int i = 0; i < packets.size(); i++) {
			if(packets.get(i).getFragmentNr() != i)
				return false;
		}
		return (packets.get(packets.size() - 1).getFlag() == Packet.LST);
	}
	
	private void flushBuffer(Integer ID) {
		ArrayList<Byte> rawMessage = new ArrayList<Byte>();
		for(Packet packet : buffer.get(ID))
			for(byte b : packet.getBytes())
				rawMessage.add(b);
		Packet header = buffer.get(ID).get(0);
		String message = header.getSrc().getHostName() + " (" + new Date(header.getTimeStamp()) + "):"
				+ System.lineSeparator() + " " + Packet.dataToString(rawMessage);
		client.messageReceived(header.getSrc(), message);
	}
	
	public void sendACK(Packet packet) {
		Packet ackpkt = new Packet(client.getLocalAddress(), packet.getSrc(), 0,  packet.getSeqNr(), (byte)0x01, System.currentTimeMillis(), 0, 0, null);
		DatagramPacket pkt = new DatagramPacket(ackpkt.getBytes(), ackpkt.getBytes().length, 
				client.routingTable.get(ackpkt.getDest()).nextHop, client.uniPort);
		try {
			client.uniSocket.send(pkt);
		} catch (IOException e) { e.printStackTrace(); }
	}
	@Override
	public void update(Observable arg0, Object packet) {
		if (arg0 instanceof UniListeningThread) {
			if (((Packet) packet).getDest().equals(client.getLocalAddress())
					&& ((Packet) packet).getSrc().equals(other)) {
				receiveMessage((Packet) packet);
			}
		}
		// Packet timeout
		if (arg0 instanceof TimerThread) {
			Packet newPacket = (Packet) packet;
			newPacket.setTimeStamp(System.currentTimeMillis());
			retransmitPacket(newPacket);
		}

	}

}