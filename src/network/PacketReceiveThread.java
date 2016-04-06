package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class PacketReceiveThread implements Runnable {

	private MulticastSocket socket;
	private InetAddress group;
	public volatile boolean wait = true;
	
	public PacketReceiveThread(MulticastSocket s, InetAddress g) {
		socket = s;
		group = g;
	}
	public void run() {
		
		
		while(wait) {
			byte[] buf = new byte[12];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(recv);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(int i = 0; i < buf.length; i += DistanceVectorEntry.SIZE) {
					byte[] entry = new byte[DistanceVectorEntry.SIZE];
					System.arraycopy(buf, i, entry, 0, DistanceVectorEntry.SIZE);
					DistanceVectorEntry dve = null;
					try {
						dve = new DistanceVectorEntry(entry);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(dve.destination);
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
		for(byte b : bs)
				System.out.print(String.format("%02X", b));
		System.out.println();
	}

}