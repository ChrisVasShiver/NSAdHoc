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

import helper.DistanceVectorEntry;
import helper.Packet;
import helper.PacketFragmenter;
import main.Client;
import security.AsymmetricEncryption;
import security.HybridEncryption;
import security.SymmetricEncryption;
import threads.TimerThread;
import threads.UniListeningThread;

public class SingleConnection implements Observer {

	private TimerThread timerRunnable;
	private Thread timerThread;
	public final InetAddress other;
	private boolean isGroup;
	private Client client;
	private int lastSeqnr = 1;
	private int lastAcknr = 1;
	private int lastPacketID = 1;
	private int lastPacketReceived = 0;
	private final int SWS = 10;
	private final int RWS = 10;
	private List<Packet> queue = new ArrayList<Packet>();
	private HashMap<Integer, HashMap<Integer, Packet>> buffer = new HashMap<Integer, HashMap<Integer, Packet>>();
	private List<Packet> sentWindow = new ArrayList<Packet>();
	private HybridEncryption hybridEnc;
	
	public SingleConnection(Client client, InetAddress other, boolean isGroup) {
		this.other = other;
		this.client = client;
		this.isGroup = isGroup;
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
			// TODO Auto-generated catch block
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
		byte flag = isGroup ? Packet.GRP : (byte)0;
		Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, flag, System.currentTimeMillis(),
				0, 0, null);
		List<Packet> packets = PacketFragmenter.getPackets(header, encryptedMessage);
		addPackets(packets);
	}

	public void sendSYN() {
		System.out.println("Sending SYN");
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.SYN,
				System.currentTimeMillis(), 0, 0, null);
		byte[] publicKey = hybridEnc.getPublicKey();

		System.out.println("Public Key generated: " + Packet.dataToString(publicKey));
		packet.setData(Packet.dataToString(publicKey));
		addPacket(packet);
		System.out.println("Sent SYN");
	}

	public void sendSYNACK(byte[] publicKey) {
		System.out.println("SYN ACK received");
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.SYN,
				System.currentTimeMillis(), 0, 0, null);
		System.out.println("Public Key received: " + Packet.dataToString(publicKey));
		byte[] secretKey = hybridEnc.generateEncryptedKey(publicKey);
		System.out.println("SecretKey generated");
		packet.setData(Packet.dataToString(secretKey));
		addPacket(packet);
	}
	
	public void sendFIN() {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.FIN,
				System.currentTimeMillis(), 0, 0, null);
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
			System.out.println("SYN ACK received");
			hybridEnc.decryptAndStoreKey(Packet.dataToByteArray(packet.getData()));
			// No break; (INTENTIONAL!)
		case Packet.ACK:
			timerRunnable.remove(packet.getAckNr());
			lastAcknr = packet.getAckNr();
			for (int i = 0; i < sentWindow.size(); i++) {
				Packet acked = sentWindow.get(i);
				if (acked.getSeqNr() == packet.getAckNr()) {
					sentWindow.remove(i);
					if (sentWindow.size() <= SWS && queue.size() > 0)
						transmitPacket(queue.remove(0));
				}
			}
			break;
		case Packet.SYN:
			if(!isGroup) {
				sendSYNACK(Packet.dataToByteArray(packet.getData()));
				client.startPrivateGUI(packet.getSrc());
			}
			sendACK(packet);
			// TODO
			break;
		case Packet.FIN:
			if(!isGroup)
				client.stopPrivateGUI(packet.getSrc());
			sendACK(packet);
			// TODO
			break;
		case Packet.FRG:
		case Packet.LST:
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
			// TODO
			break;
		default:
			if (lastPacketReceived != packet.getSeqNr()) {
				String message = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
						+ System.lineSeparator() + " ";
				if(isGroup)  {
					message += packet.getData();
					client.groupMessageReceived(message);
				}
				else {
					byte[] decryptedMessage = hybridEnc.decryptMessage(Packet.dataToByteArray(packet.getData()));
					message += Packet.dataToString(decryptedMessage);
					client.messageReceived(packet.getSrc(), message);
				}
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
		return last != null && last.getFlag() == Packet.LST;
	}

	private void flushBuffer(Integer ID) {
		System.out.println("Flushing buffer");
		String rawMessage = "";
		HashMap<Integer, Packet> packetList = buffer.get(ID);
		for (Packet packet : packetList.values())
			rawMessage += packet.getData();
		Packet header = buffer.get(ID).get(0);
		String message = header.getSrc().getHostName() + " (" + new Date(header.getTimeStamp()) + "):"
				+ System.lineSeparator() + " ";
		if(isGroup) {
			message += rawMessage;
			client.groupMessageReceived(message);
		}
		else {
			byte[] decryptedMessage = hybridEnc.decryptMessage(Packet.dataToByteArray(rawMessage));
			message += Packet.dataToString(decryptedMessage);
			client.messageReceived(header.getSrc(), message);
		}
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
