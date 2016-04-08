package helper;

import java.util.ArrayList;
import java.util.List;

import main.Client;

public class PacketFragmenter {

	public static List<Packet> getPackets(Packet packet) {
		List<Packet> packets = new ArrayList<Packet>();
		if(packet.getBytes().length > Client.MAX_PACKET_SIZE) {
			int nrOfPackets = (int)Math.ceil(packet.getDataL() / (double)(Client.MAX_PACKET_SIZE - Packet.BASICL));
			String allData = packet.getData();
			for(int i = 0; i < nrOfPackets; i++) {
				Packet p = packet.copyHeader();
				p.setFragmentNr(i);
				p.setData(null);
			}
		}
		else
			packets.add(packet);
		return packets;
	}

}
