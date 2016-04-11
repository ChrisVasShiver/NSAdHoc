package helper;

import java.util.ArrayList;
import java.util.List;

import main.Client;

public class PacketFragmenter {

	public static List<Packet> getPackets(Packet header, byte[] data) {
		List<Packet> packets = new ArrayList<Packet>();
		int maxDataSize = Client.MAX_PACKET_SIZE - Packet.HEADER_SIZE;
		if(data.length > maxDataSize) {
			System.out.println("Big packet");
			int nrOfPackets = (int)Math.ceil(data.length / (double)maxDataSize);
			for(int i = 0; i < nrOfPackets; i++) {
				Packet packet = header.copyHeader();
				packet.setFragmentNr(i);
				if(i == nrOfPackets - 1) {
					maxDataSize = data.length % maxDataSize;
					packet.setFlag(Packet.LST);
				} else
					packet.setFlag(Packet.FRG);
				packet.setOffset(i * maxDataSize);
				byte[] packetData = new byte[maxDataSize];
				System.arraycopy(data, i * maxDataSize, packetData, 0, maxDataSize);
				packet.setData(Packet.dataToString(packetData));
				packets.add(packet);
			}
		}
		else {
			Packet packet = header.copyHeader();
			packet.setData(Packet.dataToString(data));
			packets.add(packet);
		}
		return packets;
	}
	

}
