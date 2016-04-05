package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

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
				InetAddress address = null;
				try {
					address = InetAddress.getByAddress(new byte[] { buf[0], buf[1], buf[2], buf[3] });
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.print("[ " + recv.getSocketAddress() + " -> ");
				System.out.print(address + "] ");
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
		for (int i = 4; i < bs.length; i++)
			if(bs[i] != 0) {
				System.out.print((char) bs[i]);
			}
		System.out.println();
	}

}