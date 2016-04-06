package threads;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import helper.Packet;

public class UniListeningThread implements Runnable {

	public volatile boolean wait = true;
	
	public UniListeningThread() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	public void handlePacket(byte[] raw) throws UnknownHostException {
		Packet packet = new Packet(raw);
		if(packet.getDest() == InetAddress.getLocalHost()) {
			//TODO what if the packet is for me
			Packet ackpkt = new Packet(InetAddress.getLocalHost(), packet.getDest(), 0,0,0, null);
			//TODO add destination address
			DatagramPacket pkt = new DatagramPacket(ackpkt.getBytes(), ackpkt.getBytes().length);
		} else {
			if(!packet.isExpired()) {
				packet.decreaseTTL();
				//TODO what if the packet is not expired.
				//TODO add destination address
				DatagramPacket pkt = new DatagramPacket(packet.getBytes(), packet.getBytes().length);
			}
		}
	}
}
