package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

import network.PacketReceiveThread;

public class Test {
	
	
	public static void main(String[] args) throws IOException {
		String name = "Christiaan";
		InetAddress group = InetAddress.getByName("228.0.0.2");
		MulticastSocket s = new MulticastSocket(6789);
		s.joinGroup(group);
		
		Scanner scanner = new Scanner(System.in);

		PacketReceiveThread thread = new PacketReceiveThread(s, group);
		Thread t = new Thread(thread);
		t.start();
		
		String line;
		do {
			line = name + "> " + scanner.nextLine();
			DatagramPacket hi = new DatagramPacket(line.getBytes(), line.length(),
					group, 6789);
			s.send(hi);
		} while(!line.equals("quit"));
		
		scanner.close();



	}

	private static void print(byte[] bs) {
		for (byte b : bs)
			System.out.print((char) b);
		System.out.println();
	}

}