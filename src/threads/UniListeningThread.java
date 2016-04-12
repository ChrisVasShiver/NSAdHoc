package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import helper.Packet;
import main.Client;
import network.MultiConnection;

public class UniListeningThread extends Observable implements Runnable, Observer {

	public volatile boolean wait = true;
	private Client client;

	public UniListeningThread(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		while (wait) {

			byte[] buffer = new byte[Client.MAX_PACKET_SIZE];
			DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
			try {
				client.getUniSocket().receive(recvPacket);
			} catch (IOException e) {}
			if(!wait)
				continue;
			Packet pkt = null;
			try {
				pkt = new Packet(buffer);
			} catch (UnknownHostException | ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				continue;
			}
			if (pkt != null) {
				handlePacket(pkt);
			}

		}
	}

	public void handlePacket(Packet packet) {
		if(packet == null)
			return;
		if(packet.getDest().equals(client.getLocalAddress())) {
			if(packet.getFlag() == Packet.Flags.SYN) 
				client.startPrivateGUI(packet.getSrc());
			setChanged();
			notifyObservers(packet);
			clearChanged();
			
		} else if(packet.getDest().equals(MultiConnection.GROUP)) {
			setChanged();
			notifyObservers(packet);
			clearChanged();
		}
			else {
			if(!packet.isExpired()) {
				packet.decreaseTTL();
				DatagramPacket pkt = new DatagramPacket(packet.getBytes(), packet.getBytes().length,
						client.routingTable.get(packet.getDest()).nextHop, client.uniPort);
				try {
					client.getUniSocket().send(pkt);
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}
}
