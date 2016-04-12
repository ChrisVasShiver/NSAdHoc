package helper;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher 
 */
public class DistanceVectorEntry {
	public static final int SIZE = 12;
	public InetAddress destination;
	public Integer hops;
	public InetAddress nextHop;
	
	/**
	 * 
	 * @param destination the destination of the computer
	 * @param hops the amount of hops to reach the destination
	 * @param nextHop the nextHop in the direction of the destination
	 */
	public DistanceVectorEntry(InetAddress destination, Integer hops, InetAddress nextHop) {
		this.destination = destination;
		this.hops = hops;
		this.nextHop = nextHop;
	}

	/**
	 * Turns byte array into a Distance Vector
	 * @param raw byte array that contains a Distance Vector
	 * @throws UnknownHostException throws exception if the source is no valid InetAddress
	 */
	public DistanceVectorEntry(byte[] raw) throws UnknownHostException {
		assert raw.length == 12;
		byte[] source = new byte[4];
		byte[] cost = new byte[4];
		byte[] hop = new byte[4];
		System.arraycopy(raw, 0, source, 0, 4);
		for(byte b : source) {
			if(b != 0)
				break;
			throw new UnknownHostException("Host /0.0.0.0 is invalid");
		}
		System.arraycopy(raw, 4, cost, 0, 4);
		System.arraycopy(raw, 8, hop, 0, 4);
		this.destination = InetAddress.getByAddress(source);
		this.hops = Helper.byteArrayToInteger(cost);
		this.nextHop = InetAddress.getByAddress(hop);
	}
	
	/**
	 * Turns a Distance Vector into a byte array
	 * @return byte array that contains a Distance Vector
	 */
	public byte[] getBytes() {
		byte[] result = new byte[SIZE];
		System.arraycopy(destination.getAddress(),  0, result, 0, 4);
		System.arraycopy(Helper.integerToByteArray(hops),  0, result, 4, 4);
		System.arraycopy(nextHop.getAddress(),  0, result, 8, 4);
		return result;
	}
	
	/**
	 * Returns a Distance Vector as information in a String format
	 */
	@Override
	public String toString() {
		return "[" + destination.toString() + ", " + hops + ", " + nextHop.toString() + "]";
	}
}
