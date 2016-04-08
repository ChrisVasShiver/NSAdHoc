package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import helper.DistanceVectorEntry;
import main.Client;

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
				printRoutingTable();
			} catch (IOException e) {e.printStackTrace();}
			checkTimeoutElapsed();
			InetAddress sender = null;
			try {
				sender = InetAddress.getByName(recvPacket.getSocketAddress().toString().split(":")[0].replace("/", ""));
			} catch (UnknownHostException e) {e.printStackTrace(); }
			if(sender.equals(client.getLocalAddress()))
				continue;
//			if(sender.toString().equals("/192.168.5.2"))	
//				continue;
			client.neighbourTimeout.put(sender, System.currentTimeMillis());
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
			} catch (UnknownHostException e) { continue; }
			recvDistanceVector.put(dve.destination, dve);
		}
		updateRoutingTable(recvDistanceVector, sender);
		updateEntries(recvDistanceVector, sender);
	}

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
	
	private synchronized void printRoutingTable() {
		System.out.println("Routing Table: ");
		for(InetAddress address : client.routingTable.keySet()) 
			System.out.println(client.routingTable.get(address).toString());
	}
	
	private void checkTimeoutElapsed() {
		for(InetAddress address : client.neighbourTimeout.keySet()) {
			long now = System.currentTimeMillis();
			if(now - client.neighbourTimeout.get(address) > 3 * Client.sendTimeout) {
				removeEntries(address);
			}
		}
	}
	
	private void removeEntries(InetAddress node) {
		for(InetAddress address : client.routingTable.keySet())
			if(client.routingTable.get(address).nextHop.equals(node)) 
				client.routingTable.remove(address);
		client.routingTable.remove(node);
	}
	
	/* TODO:
	 * postpone good news after bad news
	 */
}
