package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import helper.Helper;

public class DistanceVectorEntry {
	public static final int SIZE = 12;
	public InetAddress destination;
	public Integer hops;
	public InetAddress nextHop;
	
	public DistanceVectorEntry(InetAddress destination, Integer hops, InetAddress nextHop) {
		this.destination = destination;
		this.hops = hops;
		this.nextHop = nextHop;
	}

	public DistanceVectorEntry(byte[] raw) throws UnknownHostException {
		assert raw.length == 12;
		byte[] source = new byte[4];
		byte[] cost = new byte[4];
		byte[] hop = new byte[4];
		System.arraycopy(raw, 0, source, 0, 4);
		System.arraycopy(raw, 4, cost, 0, 4);
		System.arraycopy(raw, 8, hop, 0, 4);
		this.destination = InetAddress.getByAddress(source);
		this.hops = Helper.byteArrayToInteger(cost);
		this.nextHop = InetAddress.getByAddress(hop);
	}
	
	public byte[] getBytes() {
		byte[] result = new byte[SIZE];
		System.arraycopy(destination.getAddress(),  0, result, 0, 4);
		System.arraycopy(Helper.integerToByteArray(hops),  0, result, 4, 4);
		System.arraycopy(nextHop.getAddress(),  0, result, 8, 4);
		return result;
	}
}
