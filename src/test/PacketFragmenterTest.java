package test;

import java.net.InetAddress;
import java.util.List;

import gui.Decoder;
import helper.Packet;
import helper.PacketFragmenter;

public class PacketFragmenterTest {

	private Packet p;
	private byte[] big;
	public void setupTest() {
		InetAddress src = null;
		InetAddress des = null;
		try {
			src = InetAddress.getByName("127.0.0.8");
			des = InetAddress.getByName("255.255.255.255");
		} catch (Exception e) {}
		p = new Packet(src, des, 0, 0, (byte)0, 0L, 0, 0, null);
	}
	
	public void makeMegaPacket() {
		Decoder decode = new Decoder("bigfile.txt");
		big = decode.decode();
	}
	
	public void testFragments() {
		List<Packet> result = PacketFragmenter.getPackets(p, big);
		for(Packet i : result)
			System.out.println("Packet: " + i.getData());
	}
	public static void main(String[] args) {
		PacketFragmenterTest t = new PacketFragmenterTest();
		t.setupTest();
		t.makeMegaPacket();
		t.testFragments();
	}

}
