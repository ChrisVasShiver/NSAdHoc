package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import gui.Decoder;
import helper.DistanceVectorEntry;
import helper.Helper;
import helper.Packet;
import helper.PacketFragmenter;
import main.Client;
import security.HybridEncryption;
import threads.TimerThread;
import threads.UniListeningThread;

public class SingleConnection implements Observer {

	private TimerThread timerRunnable;
	private Thread timerThread;
	public final InetAddress other;
	private Client client;
	private int lastSeqnr = 1;
	private int lastPacketID = 1;
	private int lastPacketReceived = 0;
	private final int SWS = 10;
	private List<Packet> queue = new ArrayList<Packet>();
	private HashMap<Integer, HashMap<Integer, Packet>> buffer = new HashMap<Integer, HashMap<Integer, Packet>>();
	private List<Packet> sentWindow = new ArrayList<Packet>();
	private HybridEncryption hybridEnc;
	
	public SingleConnection(Client client, InetAddress other) {
		this.other = other;
		this.client = client;
		timerRunnable = new TimerThread();
		timerThread = new Thread(timerRunnable);
		timerThread.start();
		timerRunnable.addObserver(this);
		client.ulRunnable.addObserver(this);
		hybridEnc = new HybridEncryption();
	}

	public void stop() {
		timerRunnable.wait = false;
		try {
			timerThread.join();
		} catch (InterruptedException e) {
			//TODO remove stack trace
			e.printStackTrace();
		}
		client.ulRunnable.deleteObserver(this);
		sendFIN();
	}

	protected void addPackets(List<Packet> packets) {
		for (Packet packet : packets) {
			packet.setPacketID(lastPacketID);
			addPacket(packet);
		}
		lastPacketID++;
	}

	private void addPacket(Packet packet) {
		queue.add(packet);
		if (sentWindow.size() <= SWS)
			transmitPacket(queue.remove(0));
	}

	public void sendMessage(String message) {
		byte[] encryptedMessage = hybridEnc.encryptMessage(Packet.dataToByteArray(message));
		Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, (byte)0, System.currentTimeMillis(),
				0, 0, null);
		List<Packet> packets = PacketFragmenter.getPackets(header, encryptedMessage);
		addPackets(packets);
	}

	public void sendFile(byte[] data) {
		byte[] encryptedData = hybridEnc.encryptMessage(data);
		Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.FILE, System.currentTimeMillis(),
				0, 0, null);
		List<Packet> packets = PacketFragmenter.getPackets(header, encryptedData);
		addPackets(packets);
	}
	
	public void sendSYN() {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.SYN,
				System.currentTimeMillis(), 0, 0, null);
		byte[] publicKey = hybridEnc.getPublicKey();
		packet.setData(publicKey);
		addPacket(packet);
	}

	public void sendSYNACK(byte[] publicKey) {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.SYN_ACK,
				System.currentTimeMillis(), 0, 0, null);
		byte[] secretKey = hybridEnc.generateEncryptedKey(publicKey);
		packet.setData(secretKey);
		addPacket(packet);
	}
	
	public void sendFIN() {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.FIN,
				System.currentTimeMillis(), 0, 0, null);
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
			//TODO remove stack trace
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
		case Packet.Flags.SYN_ACK:
			hybridEnc.decryptAndStoreKey(packet.getData());
			// No break; (INTENTIONAL!)
		case Packet.Flags.ACK:
			timerRunnable.remove(packet.getAckNr());
			for (int i = 0; i < sentWindow.size(); i++) {
				Packet acked = sentWindow.get(i);
				if (acked.getSeqNr() == packet.getAckNr()) {
					sentWindow.remove(i);
					if (sentWindow.size() <= SWS && queue.size() > 0)
						transmitPacket(queue.remove(0));
				}
			}
			break;
		case Packet.Flags.SYN:
			sendSYNACK(packet.getData());
			client.startPrivateGUI(packet.getSrc());
			sendACK(packet);
			break;
		case Packet.Flags.FIN:
			client.stopPrivateGUI(packet.getSrc());
			sendACK(packet);
			break;
		case Packet.Flags.FILE_FRG:
		case Packet.Flags.FILE_LST:
		case Packet.Flags.FRG:
		case Packet.Flags.LST:
			HashMap<Integer, Packet> packets = buffer.get(packet.getPacketID());
			if (packets == null) {
				packets = new HashMap<Integer, Packet>();
				packets.put(packet.getFragmentNr(), packet);
				buffer.put(packet.getPacketID(), packets);
			} else {
				packets.put(packet.getFragmentNr(), packet);
			}
			if (checkBuffer(packet.getPacketID()))
				flushBuffer(packet.getPacketID());
			sendACK(packet);
			break;
		case Packet.Flags.FILE:
			if (lastPacketReceived != packet.getSeqNr()) {
				String message = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + ") sent a file: ";
				byte[] decryptedMessage = hybridEnc.decryptMessage(packet.getData());
				Decoder decoder = new Decoder(decryptedMessage);
				decoder.decode("file"+packet.getTimeStamp());
				client.messageReceived(packet.getSrc(), message);
				lastPacketReceived = packet.getSeqNr();
			} 
			sendACK(packet);
			break;
		default:
			if (lastPacketReceived != packet.getSeqNr()) {
				
				String message = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
						+ System.lineSeparator() + " ";
				byte[] decryptedMessage = hybridEnc.decryptMessage(packet.getData());
				message += Packet.dataToString(decryptedMessage);
				client.messageReceived(packet.getSrc(), message);
				lastPacketReceived = packet.getSeqNr();
			} 
			sendACK(packet);
			break;
		}
	}

	private boolean checkBuffer(Integer ID) {
		HashMap<Integer, Packet> packets = buffer.get(ID);
		for (int i = 0; i < packets.size(); i++) {
			if (packets.get(i) == null)
				return false;
		}
		Packet last = packets.get(packets.size() - 1);
		return last != null && last.getFlag() == Packet.Flags.LST;
	}

	private void flushBuffer(Integer ID) {
		List<Byte> rawMessage = new ArrayList<Byte>();
		HashMap<Integer, Packet> packetList = buffer.get(ID);
		for (Packet packet : packetList.values())
			for(byte b : packet.getData())
			rawMessage.add(b);
		Packet header = buffer.get(ID).get(0);
		String message = "";
		if(header.getFlag() == Packet.Flags.FILE_FRG || header.getFlag() == Packet.Flags.FILE_LST) {
			message += header.getSrc().getHostName() + " (" + new Date(header.getTimeStamp()) + ") sent a file: ";
			byte[] decryptedMessage = hybridEnc.decryptMessage(Helper.byteListToArray(rawMessage));
			Decoder decoder = new Decoder(decryptedMessage);
			decoder.decode("file"+header.getTimeStamp());
		} else {
			message += header.getSrc().getHostName() + " (" + new Date(header.getTimeStamp()) + "):"
					+ System.lineSeparator() + " ";
			byte[] decryptedMessage = hybridEnc.decryptMessage(Helper.byteListToArray(rawMessage));
			message += Packet.dataToString(decryptedMessage);
		}
		client.messageReceived(header.getSrc(), message);
	}
	
	public void sendACK(Packet packet) {
		Packet ackpkt = new Packet(client.getLocalAddress(), packet.getSrc(), 0, packet.getSeqNr(), (byte) 0x01,
				System.currentTimeMillis(), 0, 0, null);
		DatagramPacket pkt = new DatagramPacket(ackpkt.getBytes(), ackpkt.getBytes().length,
				client.routingTable.get(ackpkt.getDest()).nextHop, client.uniPort);
		try {
			client.uniSocket.send(pkt);
		} catch (IOException e) {
			e.printStackTrace();
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
		// Packet timeout
		if (arg0 instanceof TimerThread) {
			Packet newPacket = (Packet) packet;
			newPacket.setTimeStamp(System.currentTimeMillis());
			retransmitPacket(newPacket);
		}

	}

}
