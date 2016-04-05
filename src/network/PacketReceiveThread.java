package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class PacketReceiveThread implements Runnable {

	private MulticastSocket socket;
	private InetAddress group;
	
	public PacketReceiveThread(MulticastSocket s, InetAddress g) {
		socket = s;
		group = g;
	}
	public void run() {
		boolean wait = true;
		
		
		while(wait) {
			byte[] buf = new byte[1000];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(recv);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			print(buf);
		}

		try {
			socket.leaveGroup(group);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket.close();
	}

	private void print(byte[] bs) {
		for (byte b : bs)
			if(b != 0) {
				System.out.print((char) b);
			}
		System.out.println();
	}

}