package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Arrays;

import helper.Packet;
import main.Client;

public class UniListeningThread implements Runnable {

	public volatile boolean wait = true;
	private Client client;
	
	public UniListeningThread(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		while(wait) {
	
			byte[] buffer = new byte[1024];
			DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			try {
				client.uniSocket.receive(recvPacket);
			} catch (IOException e) {e.printStackTrace();}
			Packet pkt = null;
			try {
				pkt = new Packet(buffer);
			} catch (UnknownHostException | ArrayIndexOutOfBoundsException e) { e.printStackTrace();continue; }
			if(pkt != null) {
				handlePacket(pkt);
			}
			
		}
	}
	
	public void handlePacket(Packet packet) {
		if(packet.getAckNr() != 0) {
			// TODO ackpacket
			return;
		}
		if(packet.getDest().equals(client.getLocalAddress())) {
			//TODO what if the packet is for me
			Packet ackpkt = new Packet(client.getLocalAddress(), packet.getSrc(),0,  packet.getSeqNr() + 1, 0, null);
			DatagramPacket pkt = new DatagramPacket(ackpkt.getBytes(), ackpkt.getBytes().length, 
					client.routingTable.get(ackpkt.getDest()).nextHop, client.uniPort);
			try {
				client.uniSocket.send(pkt);
			} catch (IOException e) { e.printStackTrace(); }
			System.out.println(packet.getData());
		} else {
			if(!packet.isExpired()) {
				packet.decreaseTTL();
				DatagramPacket pkt = new DatagramPacket(packet.getBytes(), packet.getBytes().length,
						client.routingTable.get(packet.getDest()).nextHop, client.uniPort);
				try {
					client.uniSocket.send(pkt);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}
}
