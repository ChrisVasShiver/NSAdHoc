package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentHashMap;

import helper.Helper;
import helper.Tuple;

public class DistanceVectorThread implements Runnable {

	private static final int rowSize = 12;
	
	private MulticastSocket socket;
	private InetAddress group;
	private ConcurrentHashMap<InetAddress, Tuple<Integer, InetAddress>> distanceVector = new ConcurrentHashMap<InetAddress, Tuple<Integer, InetAddress>>();
	
	public DistanceVectorThread(ConcurrentHashMap<InetAddress, Tuple<Integer, InetAddress>> distanceVector, InetAddress group, MulticastSocket socket) {
		this.distanceVector = distanceVector;
	}

	@Override
	public void run() {
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

	private DatagramPacket getDatagramPacket() {
		byte[] rawPacket = new byte[distanceVector.size() * rowSize];
		int index = 0;
		for(InetAddress address : distanceVector.keySet()) {
			System.arraycopy(address.getAddress(), 0, rawPacket, index, 4);
			System.arraycopy(Helper.integerToByteArray(distanceVector.get(address).getFirst()), 0, rawPacket, index+4, 4);
			System.arraycopy(distanceVector.get(address).getSecond(), 0, rawPacket, index+8, 4);
			index += rowSize;
		}
		DatagramPacket packet = new DatagramPacket(rawPacket, rawPacket.length, group, socket.getPort() );
		return packet;
		
	}
	public ConcurrentHashMap<InetAddress, Tuple<Integer, InetAddress>> getDistanceVector() {
		return distanceVector;
	}

	public void setDistanceVector(ConcurrentHashMap<InetAddress, Tuple<Integer, InetAddress>> distanceVector) {
		this.distanceVector = distanceVector;
	}

}
