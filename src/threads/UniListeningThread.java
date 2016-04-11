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
		if(packet == null)
			return;

		if(packet.getDest().equals(client.getLocalAddress())) {
			if(packet.getFlag() == Packet.Flags.SYN) {
				System.out.println("SYN received");
				client.startPrivateGUI(packet.getSrc());
				
			}
			setChanged();
			notifyObservers(packet);
			clearChanged();
			
		} else {
			if(true) {
				System.out.println("TTL: " + packet.getTTL());
				System.out.println("handlePacket notExpired");
				packet.decreaseTTL();
				DatagramPacket pkt = new DatagramPacket(packet.getBytes(), packet.getBytes().length,
						client.routingTable.get(packet.getDest()).nextHop, client.uniPort);
				try {
					client.uniSocket.send(pkt);
				} catch (IOException e) { e.printStackTrace(); }
				System.out.println("Packet resend");
			}
		}
	}

	
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}
