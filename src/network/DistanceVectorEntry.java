package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import helper.Helper;

public class DistanceVectorEntry {
	public static final int SIZE = 12;
	private InetAddress source;
	private Integer cost;
	private InetAddress hop;
	
	public DistanceVectorEntry(InetAddress source, Integer cost, InetAddress hop) {
		this.source = source;
		this.cost = cost;
		this.hop = hop;
	}

	public DistanceVectorEntry(byte[] raw) throws UnknownHostException {
		assert raw.length == 12;
		byte[] source = new byte[4];
		byte[] cost = new byte[4];
		byte[] hop = new byte[4];
		System.arraycopy(raw, 0, source, 0, 4);
		System.arraycopy(raw, 4, cost, 0, 4);
		System.arraycopy(raw, 8, hop, 0, 4);
		this.source = InetAddress.getByAddress(source);
		this.cost = Helper.byteArrayToInteger(cost);
		this.hop = InetAddress.getByAddress(hop);
	}
	
	public byte[] getBytes() {
		byte[] result = new byte[SIZE];
		System.arraycopy(source.getAddress(),  0, result, 0, 4);
		System.arraycopy(Helper.integerToByteArray(cost),  0, result, 4, 4);
		System.arraycopy(hop.getAddress(),  0, result, 8, 4);
		return result;
	}
	public InetAddress getSource() {
		return source;
	}

	public void setSource(InetAddress source) {
		this.source = source;
	}

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	public InetAddress getHop() {
		return hop;
	}

	public void setHop(InetAddress hop) {
		this.hop = hop;
	}

}
