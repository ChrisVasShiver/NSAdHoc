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
	private ConcurrentHashMap<InetAddress, DistanceVector> distanceVector = new ConcurrentHashMap<InetAddress, DistanceVector>();

	public DistanceVectorThread(ConcurrentHashMap<InetAddress, DistanceVector> distanceVector, InetAddress group,
			MulticastSocket socket) {
		this.distanceVector = distanceVector;
		this.group = group;
		this.socket = socket;
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
		byte[] rawPacket = new byte[distanceVector.size() * DistanceVector.SIZE];
		int index = 0;
		for (InetAddress address : distanceVector.keySet()) {
			DistanceVector dv = distanceVector.get(address);
			System.arraycopy(dv.getBytes(), 0, rawPacket, index, DistanceVector.SIZE);
			index += DistanceVector.SIZE;
		}
		DatagramPacket packet = new DatagramPacket(rawPacket, rawPacket.length, group, socket.getPort());
		return packet;

	}

	public ConcurrentHashMap<InetAddress, DistanceVector> getDistanceVector() {
		return distanceVector;
	}

	public void setDistanceVector(ConcurrentHashMap<InetAddress, DistanceVector> distanceVector) {
		this.distanceVector = distanceVector;
	}

}
