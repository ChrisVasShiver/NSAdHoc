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
import helper.Constants;
import helper.DistanceVectorEntry;
import helper.FilePacket;
import helper.Helper;
import helper.Packet;
import helper.PacketFragmenter;
import main.Client;
import security.HybridEncryption;
import threads.TimerThread;
import threads.UniListeningThread;

/**
 * Class for a secured single connection between two nodes
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class SingleConnection implements Observer {

	private TimerThread timerRunnable;
	private Thread timerThread;
	public final InetAddress other;
	private Client client;
	private int lastSeqnr = 1;
	private int lastPacketID = 1;
	private int lastPacketReceived = 0;
	private final static int SWS = 10;
	private final static int RWS = 50;
	private List<Packet> queue = new ArrayList<Packet>();
	private HashMap<Integer, HashMap<Integer, Packet>> buffer = new HashMap<Integer, HashMap<Integer, Packet>>();
	private List<Packet> sentWindow = new ArrayList<Packet>();
	private HashMap<Integer, Packet> receiveBuffer = new HashMap<Integer, Packet>();
	private HybridEncryption hybridEnc;

	public SingleConnection(Client client, InetAddress other) {
		this.other = other;
		this.client = client;
		timerRunnable = new TimerThread();
		timerThread = new Thread(timerRunnable);
		timerThread.start();
		timerRunnable.addObserver(this);
		client.getUlRunnable().addObserver(this);
		hybridEnc = new HybridEncryption();
	}

	/**
	 * Tries to stop the thread
	 */
	public void stop() {
		timerRunnable.wait = false;
		try {
			timerThread.join();
		} catch (InterruptedException e) {	}
		client.getUlRunnable().deleteObserver(this);
		sendFIN();
	}

	/**
	 * Add a list of packets to the queue
	 * will send a packet if the sentWindow is not full
	 * @param packets Packets to be added
	 */
	private void addPackets(List<Packet> packets) {
		for (Packet packet : packets) {
			packet.setPacketID(lastPacketID);
			addPacket(packet);
		}
		lastPacketID++;
	}

	/**
	 * Add a single packet to the queue,
	 * will send a packet if the sentWindow is not full
	 * @param packet The packet to be added
	 */
	private void addPacket(Packet packet) {
		queue.add(packet);
		if (sentWindow.size() <= SWS)
			transmitPacket(queue.remove(0));
	}

	/**
	 * Sends an encrypted message to the other node
	 * @param message
	 */
	public void sendMessage(String message) {
		byte[] encryptedMessage = hybridEnc.encryptMessage(Packet.dataToByteArray(message));
		Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, (byte) 0, System.currentTimeMillis(),
				0, 0, null);
		List<Packet> packets = PacketFragmenter.getPackets(header, encryptedMessage);
		addPackets(packets);
	}

	/**
	 * Sends an encrypted file to the other node
	 * @param file
	 */
	public void sendFile(FilePacket file) {
		byte[] encryptedData = hybridEnc.encryptMessage(file.getBytes());
		Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.FILE,
				System.currentTimeMillis(), 0, 0, null);
		List<Packet> packets = PacketFragmenter.getPackets(header, encryptedData);
		addPackets(packets);
	}

	/**
	 * Send a SYN packet (for starting up the connection)
	 */
	public void sendSYN() {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.SYN,
				System.currentTimeMillis(), 0, 0, null);
		byte[] publicKey = hybridEnc.getPublicKey();
		packet.setData(publicKey);
		addPacket(packet);
	}

	/**
	 * Send a SYN_ACK packet (for acknowledging a SYN and generating the shared secret key)
	 * @param publicKey The received public key in the previous SYN packet
	 */
	public void sendSYNACK(byte[] publicKey) {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.SYN_ACK,
				System.currentTimeMillis(), 0, 0, null);
		byte[] secretKey = hybridEnc.generateEncryptedKey(publicKey);
		packet.setData(secretKey);
		addPacket(packet);
	}

	/**
	 * Sends a FIN packet
	 */
	public void sendFIN() {
		Packet packet = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.Flags.FIN,
				System.currentTimeMillis(), 0, 0, null);
		addPacket(packet);
	}

	/**
	 * Send an ACK packet
	 * @param packet
	 */
	public void sendACK(Packet packet) {
		Packet ackpkt = new Packet(client.getLocalAddress(), packet.getSrc(), 0, packet.getSeqNr(), (byte) 0x01,
				System.currentTimeMillis(), 0, 0, null);
		DatagramPacket pkt = new DatagramPacket(ackpkt.getBytes(), ackpkt.getBytes().length,
				client.routingTable.get(ackpkt.getDest()).nextHop, Constants.UNI_SOCKET_PORT);
		try {
			client.getUniSocket().send(pkt);
		} catch (IOException e) {}
	}
	
	/**
	 * Send a packet over UDP, will only work when the destination in the routing table exists
	 * @param packet
	 */
	private void sendPacket(Packet packet) {
		DistanceVectorEntry dve = client.routingTable.get(other);
		if (dve == null) {
			System.out.println("Address " + other.getHostName() + " is not in your routing table.");
			return;
		}
		DatagramPacket dpack = new DatagramPacket(packet.getBytes(), packet.getBytes().length, dve.nextHop,
				Constants.UNI_SOCKET_PORT);
		try {
			client.getUniSocket().send(dpack);
		} catch (IOException e) { }

		timerRunnable.put(packet.getSeqNr(), packet);
	}

	/**
	 * Retransmit a packet (ignoring the sent window)
	 * @param packet
	 */
	private void retransmitPacket(Packet packet) {
		sendPacket(packet);
	}

	/**
	 * Transmit a packet, the sequence number will be increased
	 * @param packet
	 */
	private void transmitPacket(Packet packet) {
		packet.setSeqNr(lastSeqnr);
		sentWindow.add(packet);
		sendPacket(packet);
		lastSeqnr++;
	}

	/**
	 * Is called when a message for this client is received
	 * @param packet
	 */
	public void receiveMessage(Packet packet) {
		// Handle ACKs directly
		if (packet.getFlag() == Packet.Flags.ACK)
			flushMessage(packet);
		// Only handle packets that have a sequence number lower than lastPacketReceived + RWS
		if (packet.getSeqNr() < (lastPacketReceived + RWS)) {
			// If the packet is handled already, just send an ACK, otherwise put it in the buffer 
			if (packet.getSeqNr() > lastPacketReceived) {
				receiveBuffer.put(packet.getSeqNr(), packet);
				// If this packet is to be handled, flush the buffer
				if (packet.getSeqNr() == (lastPacketReceived + 1))
					flushReceiveWindow();
			} else
				sendACK(packet);
		}
	}

	/**
	 * Handle the packet and flush the data to the application layer
	 * @param packet
	 */
	public void flushMessage(Packet packet) {
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
			break;
		case Packet.Flags.FIN:
			sendACK(packet);
			client.stopPrivateGUI(packet.getSrc());
			break;
		case Packet.Flags.FILE_FRG:
		case Packet.Flags.FILE_LST:
		case Packet.Flags.FRG:
		case Packet.Flags.LST:
			sendACK(packet);
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
			break;
		case Packet.Flags.FILE:
			sendACK(packet);
			byte[] decryptedMessage = hybridEnc.decryptMessage(packet.getData());
			FilePacket file = new FilePacket(decryptedMessage);
			Decoder decoder = new Decoder(file.getData());
			decoder.decode(file.getFilename());
			String message = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + ") sent a file: "
					+ file.getFilename();
			client.messageReceived(packet.getSrc(), message);
			break;
		default:
			sendACK(packet);
			String msg = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
					+ System.lineSeparator() + " ";
			byte[] decryptedMsg = hybridEnc.decryptMessage(packet.getData());
			msg += Packet.dataToString(decryptedMsg);
			client.messageReceived(packet.getSrc(), msg);
			break;
		}
	}

	/**
	 * Try to flush the receive window (buffer)
	 * Will call flush message for all consecutive packets beginning at lastPacketReceived
	 */
	private void flushReceiveWindow() {
		for (Integer seq : receiveBuffer.keySet()) {
			if (seq.equals(lastPacketReceived + 1)) {
				flushMessage(receiveBuffer.remove(seq));
				lastPacketReceived++;
			} else
				break;
		}
	}

	/**
	 * Check the fragment buffer whether all fragments for this packet are received or not
	 * @param ID The packet ID in the buffer
	 * @return
	 */
	private boolean checkBuffer(Integer ID) {
		HashMap<Integer, Packet> packets = buffer.get(ID);
		for (int i = 0; i < packets.size(); i++) {
			if (packets.get(i) == null)
				return false;
		}
		Packet last = packets.get(packets.size() - 1);
		return last != null && (last.getFlag() == Packet.Flags.LST || last.getFlag() == Packet.Flags.FILE_LST);
	}

	/**
	 * Flush all the fragments of the packet to the application layer (may be a text message or a file)
	 * @param ID The packet ID in the buffer
	 */
	private void flushBuffer(Integer ID) {
		List<Byte> rawMessage = new ArrayList<Byte>();
		HashMap<Integer, Packet> packetList = buffer.get(ID);
		for (Packet packet : packetList.values())
			for (byte b : packet.getData())
				rawMessage.add(b);
		Packet header = buffer.get(ID).get(0);
		String message = "";
		// Handle file
		if (header.getFlag() == Packet.Flags.FILE_FRG || header.getFlag() == Packet.Flags.FILE_LST) {
			byte[] decryptedMessage = hybridEnc.decryptMessage(Helper.byteListToArray(rawMessage));
			FilePacket file = new FilePacket(decryptedMessage);
			Decoder decoder = new Decoder(file.getData());
			decoder.decode(file.getFilename());
			message += header.getSrc().getHostName() + " (" + new Date(header.getTimeStamp()) + ") sent a file: "
					+ file.getFilename();
		}
		// Handle text message
		else {
			message += header.getSrc().getHostName() + " (" + new Date(header.getTimeStamp()) + "):"
					+ System.lineSeparator() + " ";
			byte[] decryptedMessage = hybridEnc.decryptMessage(Helper.byteListToArray(rawMessage));
			message += Packet.dataToString(decryptedMessage);
		}
		// Notify message
		client.messageReceived(header.getSrc(), message);
	}

	
	/**
	 * Is called when a packet is received
	 * Will handle packets that are for this connection (source == other and destination == client)
	 * Will handle packet timeouts by retransmitting that packet
	 */
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
