package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import helper.Constants;
import helper.DistanceVectorEntry;
import helper.Packet;
import main.Client;
import threads.UniListeningThread;

/**
 * Class for handling the group chat
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class MultiConnection implements Observer {

	private Client client;
	public static final InetAddress GROUP = getGroup();

	public MultiConnection(Client client) {
		this.client = client;
		client.getUlRunnable().addObserver(this);
	}

	/**
	 *  Retrieve the group IPv4 address (228.0.0.8)
	 */
	private static InetAddress getGroup() {
		InetAddress result = null;
		try {
			result = InetAddress.getByName("228.0.0.8");
		} catch (UnknownHostException e) {}
		return result;
	}

	/**
	 * Send a message over UDP to all the nodes in the routing table
	 * @param message
	 */
	public void sendMessage(String message) {
		Packet packet = new Packet(client.getLocalAddress(), GROUP, 0, 0, (byte) 0, System.currentTimeMillis(), 0, 0,
				Packet.dataToByteArray(message));
		for (InetAddress address : client.routingTable.keySet())
			sendPacket(packet, address);
	}

	/**
	 * Send a packet over UDP to the specified node
	 * @param packet
	 * @param other Address of the specified node
	 */
	private void sendPacket(Packet packet, InetAddress other) {
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
	}

	/**
	 * Is called after a packet is received, checks whether the GROUP IPv4 is used in the packet destination
	 * and prints the data to the screen.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof UniListeningThread) {
			Packet packet = (Packet) arg1;
			if (packet.getDest().equals(GROUP)) {
				String msg = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
						+ System.lineSeparator() + " ";
				msg += Packet.dataToString(packet.getData()) + System.lineSeparator();
				client.getGUI().setText(msg);
			}
		}

	}
}
