package network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import main.Client;

public class MultiConnection {


	private List<SingleConnection> connections = new ArrayList<SingleConnection>();
	private Client client;
	
	public MultiConnection(Client client) {
		this.client = client;
		setConnections();
	}


	public void setConnections() {
//		connections.clear();
//		for(InetAddress address : client.routingTable.keySet()) {
//			if(!client.getLocalAddress().equals(address))
//				connections.add(new SingleConnection(this.client, address, true));
//		}
	}
	
	public void sendMessage(String message) {
//		for(SingleConnection conn : connections) {
//			conn.sendMessage(message);
//		}
	}
}
