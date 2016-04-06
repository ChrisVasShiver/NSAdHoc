package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import network.DistanceVectorEntry;
import network.DistanceVectorThread;
import network.PacketReceiveThread;

public class Test {
	
	
	public static void main(String[] args) throws IOException {
		byte[] destination = InetAddress.getByName("192.168.5.1").getAddress();
		InetAddress group = InetAddress.getByName("228.0.0.2");
		int port = 6789;
		MulticastSocket s = new MulticastSocket(port);
		s.joinGroup(group);
		
		Scanner scanner = new Scanner(System.in);

		DistanceVectorEntry dv = new DistanceVectorEntry(InetAddress.getLocalHost(), 0, InetAddress.getLocalHost());
		ConcurrentHashMap<InetAddress, DistanceVectorEntry> hashmap = new ConcurrentHashMap<InetAddress, DistanceVectorEntry>();
		hashmap.put(InetAddress.getLocalHost(), dv);
		DistanceVectorThread distanceVectorThread = new DistanceVectorThread(hashmap, group, s, port);
		PacketReceiveThread thread = new PacketReceiveThread(s, group);
		Thread t = new Thread(thread);
		Thread dvt = new Thread(distanceVectorThread);
		t.start();
		dvt.start();
		
		String line;
		do {
			line = scanner.nextLine();
			byte[] rawPacket = new byte[destination.length + line.length()];
			System.arraycopy(destination, 0, rawPacket, 0, destination.length); 
			System.arraycopy(line.getBytes(), 0, rawPacket, destination.length, line.length());
			DatagramPacket packet = new DatagramPacket(rawPacket, rawPacket.length,
					group, port);
			s.send(packet);
		} while(!line.equals("quit"));
		
		scanner.close();
		thread.wait = false;
		distanceVectorThread.wait = false;
		try {
			t.join();
			dvt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}