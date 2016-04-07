package network;

import java.net.InetAddress;

public class TransportLayer {

	enum Flags { SYN, ACK, FYN };
	public TransportLayer() {
		// TODO Auto-generated constructor stub
	}
	
	public void sendMessage(InetAddress source, InetAddress destination, String message) {}

}
