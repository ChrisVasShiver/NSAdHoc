package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import helper.Packet;
import main.Client;

public class UniListeningThread extends Observable implements Runnable, Observer {

	public volatile boolean wait = true;
	private Client client;
	
	public UniListeningThread(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		while(wait) {
	
			byte[] buffer = new byte[Client.MAX_PACKET_SIZE];
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
		if(packet.getDest().equals(client.getLocalAddress())) {
			//TODO what if the packet is for me
			Packet ackpkt = new Packet(client.getLocalAddress(), packet.getSrc(),0,  packet.getSeqNr(), (byte)0x01, System.currentTimeMillis(), null);
			DatagramPacket pkt = new DatagramPacket(ackpkt.getBytes(), ackpkt.getBytes().length, 
					client.routingTable.get(ackpkt.getDest()).nextHop, client.uniPort);
			try {
				client.uniSocket.send(pkt);
			} catch (IOException e) { e.printStackTrace(); }

			setChanged();
			notifyObservers(packet);
			this.clearChanged();
			
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

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}
