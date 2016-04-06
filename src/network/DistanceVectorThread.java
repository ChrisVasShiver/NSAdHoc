package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentHashMap;

public class DistanceVectorThread implements Runnable {
	public volatile boolean wait = true;
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private ConcurrentHashMap<InetAddress, DistanceVectorEntry> distanceVector = new ConcurrentHashMap<InetAddress, DistanceVectorEntry>();

	public DistanceVectorThread(ConcurrentHashMap<InetAddress, DistanceVectorEntry> distanceVector, InetAddress group,
			MulticastSocket socket, int port) {
		this.distanceVector = distanceVector;
		this.group = group;
		this.socket = socket;
		this.port = port;
	}

	@Override
	public void run() {
		while (wait) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.send(getDatagramPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private DatagramPacket getDatagramPacket() {
		byte[] rawPacket = new byte[distanceVector.size() * DistanceVectorEntry.SIZE];
		int index = 0;
		for (InetAddress address : distanceVector.keySet()) {
			DistanceVectorEntry dv = distanceVector.get(address);
			System.arraycopy(dv.getBytes(), 0, rawPacket, index, DistanceVectorEntry.SIZE);
			index += DistanceVectorEntry.SIZE;
		}
		DatagramPacket packet = new DatagramPacket(rawPacket, rawPacket.length, group, port);
		return packet;

	}

	public ConcurrentHashMap<InetAddress, DistanceVectorEntry> getDistanceVector() {
		return distanceVector;
	}

	public void setDistanceVector(ConcurrentHashMap<InetAddress, DistanceVectorEntry> distanceVector) {
		this.distanceVector = distanceVector;
	}

}
