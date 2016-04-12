package helper;

import java.util.ArrayList;
import java.util.List;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher 
 */
public class PacketFragmenter {
	
	/*
	 * This class fragments packets that are too big to send in one packet
	 * The packages are splitted at the max packet size minus the headersize
	 * Every package gets flag with the fragment offset, so that the receiver can put packets in the right order
	 */
	public static List<Packet> getPackets(Packet header, byte[] data) {
		List<Packet> packets = new ArrayList<Packet>();
		int maxDataSize = Constants.MAX_PACKET_SIZE - Packet.HEADER_SIZE;
		if(data.length > maxDataSize) {
			int nrOfPackets = (int)Math.ceil(data.length / (double)maxDataSize);
			for(int i = 0; i < nrOfPackets; i++) {
				int packetDataSize = maxDataSize;
				Packet packet = header.copyHeader();
				packet.setFragmentNr(i);
				if(i == nrOfPackets - 1) {
					packetDataSize = data.length % packetDataSize;
					if(packet.getFlag() == Packet.Flags.FILE)
						packet.setFlag(Packet.Flags.FILE_LST);
					else
						packet.setFlag(Packet.Flags.LST);
				} else {
					if(packet.getFlag() == Packet.Flags.FILE)
						packet.setFlag(Packet.Flags.FILE_FRG);
					else
						packet.setFlag(Packet.Flags.FRG);
				}
				packet.setOffset(i * packetDataSize);
				byte[] packetData = new byte[packetDataSize];
				System.arraycopy(data, i * maxDataSize, packetData, 0, packetDataSize);
				packet.setData(packetData);
				packets.add(packet);
			}
		}
		else {
			Packet packet = header.copyHeader();
			packet.setData(data);
			packets.add(packet);
		}
		return packets;
	}
	

}
