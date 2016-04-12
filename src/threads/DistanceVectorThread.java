package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import helper.DistanceVectorEntry;
import main.Client;

public class DistanceVectorThread implements Runnable {

	public volatile boolean wait = true;
	
	private Client client;
	public DistanceVectorThread(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		while(wait) {
			try {
				client.getMultiSocket().send(getDatagramPacket());
				Thread.sleep(Client.sendTimeout);
			} catch (InterruptedException | IOException e) { }
		}
	}
	
	private synchronized DatagramPacket getDatagramPacket() {
		byte[] rawPacket = new byte[client.routingTable.size() * DistanceVectorEntry.SIZE];
		int index = 0;
		for (InetAddress address : client.routingTable.keySet()) {
			DistanceVectorEntry dv = client.routingTable.get(address);
			System.arraycopy(dv.getBytes(), 0, rawPacket, index, DistanceVectorEntry.SIZE);
			index += DistanceVectorEntry.SIZE;
		}
		DatagramPacket packet = new DatagramPacket(rawPacket, rawPacket.length, client.getGroup(), client.multiPort);
		return packet;

	}

}
