package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import helper.Constants;
import helper.DistanceVectorEntry;
import main.Client;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class MultiListeningThread implements Runnable {

	public volatile boolean wait = true;
	private Client client;
	
	public MultiListeningThread(Client client) {
		this.client = client;
	}

	/**
	 * Receive packets on the multicast socket
	 */
	@Override
	public void run() {
		while(wait) {
			byte[] buffer = new byte[DistanceVectorEntry.SIZE * 100];
			DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			try {
				client.getMultiSocket().receive(recvPacket);
			} catch (IOException e) { }
			if(!wait)
				continue;
			checkTimeoutElapsed();
			InetAddress sender = null;
			try {
				sender = InetAddress.getByName(recvPacket.getSocketAddress().toString().split(":")[0].replace("/", ""));
			} catch (UnknownHostException e) {e.printStackTrace();}
			if(sender.equals(client.getLocalAddress()))
				continue;
			client.neighbourTimeout.put(sender, System.currentTimeMillis());
			handleDistanceVectorPacket(buffer, sender);
		}
	}
	
	/**
	 * Handle the received distance vector
	 * @param packet
	 * @param sender
	 */
	public synchronized void handleDistanceVectorPacket(byte[] packet, InetAddress sender) {
		HashMap<InetAddress, DistanceVectorEntry> recvDistanceVector = new HashMap<InetAddress, DistanceVectorEntry>();
		for(int i = 0; i < packet.length; i += DistanceVectorEntry.SIZE) {
			byte[] raw = new byte[DistanceVectorEntry.SIZE];
			System.arraycopy(packet, i, raw, 0, DistanceVectorEntry.SIZE);
			DistanceVectorEntry dve = null;
			try { dve = new DistanceVectorEntry(raw);
			} catch (UnknownHostException e) { continue; }
			recvDistanceVector.put(dve.destination, dve);
		}
		updateRoutingTable(recvDistanceVector, sender);
		updateEntries(recvDistanceVector, sender);
	}

	/**
	 * Update the distance vector according to the received distance vector
	 * @param distanceVector
	 * @param sender
	 */
	private void updateRoutingTable(HashMap<InetAddress, DistanceVectorEntry> distanceVector, InetAddress sender) {
		for(InetAddress address : distanceVector.keySet()) {
			DistanceVectorEntry entry = distanceVector.get(address);
			if(!entry.nextHop.equals(client.getLocalAddress())) {
				DistanceVectorEntry storedEntry = client.routingTable.get(address);
				if(storedEntry == null || storedEntry.hops > entry.hops + 1 || storedEntry.nextHop == sender) {
					entry.hops += 1;
					entry.nextHop = sender;
					client.routingTable.put(address, entry);
				}
			}
		}
	}
	
	/**
	 * Remove all the entries that are not being advertised by the node which previously advertised the route
	 * @param distanceVector
	 * @param sender
	 */
	private void updateEntries(HashMap<InetAddress, DistanceVectorEntry> distanceVector, InetAddress sender) {
		for(InetAddress address : client.routingTable.keySet()) {
			DistanceVectorEntry storedEntry = client.routingTable.get(address);
			if(storedEntry.nextHop == sender) {
				DistanceVectorEntry entry = distanceVector.get(address);
				if(entry == null)
					client.routingTable.remove(address);
			}
		}
	}
	
	/**
	 * Print the routing table (for debugging purposes)
	 */
	public synchronized void printRoutingTable() {
		System.out.println("Routing Table: ");
		for(InetAddress address : client.routingTable.keySet()) 
			System.out.println(client.routingTable.get(address).toString());
	}
	
	/**
	 * Check whether a timeout has elapsed so it is assumed that this node is unreachable
	 */
	private void checkTimeoutElapsed() {
		for(InetAddress address : client.neighbourTimeout.keySet()) {
			long now = System.currentTimeMillis();
			if(now - client.neighbourTimeout.get(address) > 3 * Constants.PACKET_TIMEOUT) {
				removeEntries(address);
			}
		}
	}
	
	/**
	 * Remove all the entries that are being advertised by a node which has become unreachable
	 * @param node
	 */
	private void removeEntries(InetAddress node) {
		for(InetAddress address : client.routingTable.keySet())
			if(client.routingTable.get(address).nextHop.equals(node)) 
				client.routingTable.remove(address);
		client.routingTable.remove(node);
	}

}
