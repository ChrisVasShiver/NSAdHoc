package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import main.Client;
import network.DistanceVectorEntry;

public class MultiListeningThread implements Runnable {

	public volatile boolean wait = true;
	private Client client;
	
	public MultiListeningThread(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		while(wait) {
			byte[] buffer = new byte[DistanceVectorEntry.SIZE * 100];
			DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			try {
				client.multiSocket.receive(recvPacket);
			} catch (IOException e) {e.printStackTrace();}
			InetAddress sender = null;
			try {
				sender = InetAddress.getByName(recvPacket.getSocketAddress().toString());
			} catch (UnknownHostException e) {e.printStackTrace(); }
			
			handleDistanceVectorPacket(buffer, sender);
		}
	}
	
	public synchronized void handleDistanceVectorPacket(byte[] packet, InetAddress sender) {
		HashMap<InetAddress, DistanceVectorEntry> recvDistanceVector = new HashMap<InetAddress, DistanceVectorEntry>();
		for(int i = 0; i < packet.length; i += DistanceVectorEntry.SIZE) {
			byte[] raw = new byte[DistanceVectorEntry.SIZE];
			System.arraycopy(packet, i, raw, 0, DistanceVectorEntry.SIZE);
			DistanceVectorEntry dve = null;
			try { dve = new DistanceVectorEntry(raw);
			} catch (UnknownHostException e) {e.printStackTrace(); continue;}
			recvDistanceVector.put(dve.destination, dve);
		}
		updateRoutingTable(recvDistanceVector);
		removeEntries(recvDistanceVector, sender);
	}

	public void updateRoutingTable(HashMap<InetAddress, DistanceVectorEntry> distanceVector) {
		for(InetAddress address : distanceVector.keySet()) {
			DistanceVectorEntry entry = distanceVector.get(address);
			if(!entry.nextHop.equals(client.getLocalAddress())) {
				DistanceVectorEntry storedEntry = client.routingTable.get(address);
				if(storedEntry == null || storedEntry.hops > entry.hops + 1) {
					entry.hops += 1;
					client.routingTable.put(address, entry);
				}
			}
		}
	}
	
	public void removeEntries(HashMap<InetAddress, DistanceVectorEntry> distanceVector, InetAddress sender) {
		for(InetAddress address : client.routingTable.keySet()) {
			DistanceVectorEntry storedEntry = client.routingTable.get(address);
			if(storedEntry.nextHop == sender) {
				DistanceVectorEntry entry = distanceVector.get(address);
				if(entry == null)
					client.routingTable.remove(address);
			}
		}
	}
	
	/* TODO:
	 * postpone good news after bad news
	 * remove neighbours if link is broken? After timeout no broadcast received?
	 */
}
