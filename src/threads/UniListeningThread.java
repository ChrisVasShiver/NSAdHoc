package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Observable;

import helper.Constants;
import helper.Packet;
import main.Client;
import network.MultiConnection;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class UniListeningThread extends Observable implements Runnable {

	public volatile boolean wait = true;
	private Client client;

	public UniListeningThread(Client client) {
		this.client = client;
	}

	/**
	 * Receive packets on the DatagramSocket
	 */
	@Override
	public void run() {
		while (wait) {

			byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
			DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			try {
				client.getUniSocket().receive(recvPacket);
			} catch (IOException e) {}
			if(!wait)
				continue;
			Packet pkt = null;
			try {
				pkt = new Packet(buffer);
			} catch (UnknownHostException | ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				continue;
			}
			if (pkt != null) {
				handlePacket(pkt);
			}

		}
	}

	/**
	 * Handle a packet
	 * @param packet
	 */
	public void handlePacket(Packet packet) {
		// If the packet is null, do nothing
		if(packet == null)
			return;
		// If the packet is for this client, notify the observers
		if(packet.getDest().equals(client.getLocalAddress())) {
			if(packet.getFlag() == Packet.Flags.SYN) 
				client.startPrivateGUI(packet.getSrc());
			setChanged();
			notifyObservers(packet);
			clearChanged();
		// If the packet is for the group chat, notify the observers
		} else if(packet.getDest().equals(MultiConnection.GROUP)) {
			setChanged();
			notifyObservers(packet);
			clearChanged();
		} // The packet has to be routed to another node in the network
			else {
			// The packet may only routed further if it has not been expired yet
			if(!packet.isExpired()) {
				// Decrease the TTL per hop
				packet.decreaseTTL();
				DatagramPacket pkt = new DatagramPacket(packet.getBytes(), packet.getBytes().length,
						client.routingTable.get(packet.getDest()).nextHop, Constants.UNI_SOCKET_PORT);
				try {
					client.getUniSocket().send(pkt);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}
}
