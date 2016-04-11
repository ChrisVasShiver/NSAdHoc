package network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import helper.Packet;
import helper.PacketFragmenter;
import main.Client;

public class MultiConnection extends SingleConnection {

	public static final InetAddress DUMMY = getGroup();

	public MultiConnection(Client client) {
		super(client, DUMMY);
	}

	private static InetAddress getGroup() {
		InetAddress result = null;
		try {
			result = InetAddress.getByAddress(new byte[] { (byte) 228, 0, 0, 0 });
		} catch (UnknownHostException e) {
		}
		return result;
	}
	
	@Override
	public void sendMessage(String message) {
		for(InetAddress other : client.routingTable.keySet()) {
			Packet header = new Packet(client.getLocalAddress(), other, lastSeqnr, 0, Packet.GRP, System.currentTimeMillis(),
					0,0, null);
			List<Packet> packets = PacketFragmenter.getPackets(header, Packet.dataToByteArray(message));
			addPackets(packets);
		}
		
}

}
