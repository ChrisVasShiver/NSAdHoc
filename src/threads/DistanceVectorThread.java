package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import helper.Constants;
import helper.DistanceVectorEntry;
import main.Client;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class DistanceVectorThread implements Runnable {

	public volatile boolean wait = true;
	
	private Client client;
	public DistanceVectorThread(Client client) {
		this.client = client;
	}

	/**
	 * Send the distance vector and wait PACKET_TIMEOUT
	 */
	@Override
	public void run() {
		while(wait) {
			try {
				client.getMultiSocket().send(getDatagramPacket());
				Thread.sleep(Constants.PACKET_TIMEOUT);
			} catch (InterruptedException | IOException e) { }
		}
	}
	
	/**
	 * Make the DatagramPacket with the routing table
	 * @return
	 */
	private synchronized DatagramPacket getDatagramPacket() {
		byte[] rawPacket = new byte[client.routingTable.size() * DistanceVectorEntry.SIZE];
		int index = 0;
		for (InetAddress address : client.routingTable.keySet()) {
			DistanceVectorEntry dv = client.routingTable.get(address);
			System.arraycopy(dv.getBytes(), 0, rawPacket, index, DistanceVectorEntry.SIZE);
			index += DistanceVectorEntry.SIZE;
		}
		DatagramPacket packet = new DatagramPacket(rawPacket, rawPacket.length, client.getGroup(), Constants.MULTI_SOCKET_PORT);
		return packet;

	}

}
