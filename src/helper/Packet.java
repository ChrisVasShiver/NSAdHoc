package helper;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class Packet {
	public class Flags {
		public final static byte ACK = 0x01; // Acknowledgement
		public final static byte SYN = 0x02; // Synchronize
		public final static byte SYN_ACK = 0x03;
		public final static byte FIN = 0x04; // Finish
		public final static byte FILE = 0x08; // File packet
		public final static byte FRG = 0x10; // Fragmented
		public final static byte LST = 0x20; // Last fragment
		public final static byte FILE_FRG = 0x18;
		public final static byte FILE_LST = 0x28;
	}

	public final static int HEADER_SIZE = 42;
	public final static String ENCODING = "UTF-16BE";
	private final static byte BASICTTL = 12;
	private int packetNumber = 0;
	private InetAddress src;
	private InetAddress dest;
	private int seqNr;
	private int ackNr;
	private byte flag;
	private long timeStamp;
	private byte TTL;
	private int fragmentNr;
	private int offset;
	private int dataL;
	private byte[] data;
		
	/**
	 * 
	 * @param src Source IP-address
	 * @param dest Destination IP-address
	 * @param seqNr Sequence Number
	 * @param ackNr Acknowledgement Number
	 * @param flag Flag (see @Packet.Flags for more information)
	 * @param timeStamp Timestamp of the message
	 * @param fragmentNr Fragment number
	 * @param offset Offset in the packet
	 * @param data The payload of the packet
	 */
	public Packet(InetAddress src, InetAddress dest, int seqNr, int ackNr, byte flag, long timeStamp, 
			int fragmentNr, int offset, byte[] data) {
		this.src = src;
		this.dest = dest;
		this.seqNr = seqNr;
		this.ackNr = ackNr;
		this.flag = flag;
		this.timeStamp = timeStamp;
		this.TTL = BASICTTL;
		this.fragmentNr = fragmentNr;
		this.offset = offset;
		this.dataL = 0;
		if(data != null) {
			this.data = data;
			this.dataL = data.length;
		}
	}
	
	/**
	 * Creates a packet based upon received bytes of another client
	 * @param raw the bytes received from another client
	 * @throws UnknownHostException if an address does not exist it throws an exception.
	 */
	public Packet(byte[] raw) throws UnknownHostException {
		byte[] src = new byte[4];
		byte[] dest = new byte[4];
		byte[] seqNr = new byte[4];
		byte[] ackNr = new byte[4];
		this.flag = raw[16];
		byte[] timeStamp = new byte[8];
		this.TTL = raw[25];
		byte[] fragmentNr = new byte[4];
		byte[] offset = new byte[4];
		byte[] packetID = new byte[4];
		byte[] dataL = new byte[4];
		System.arraycopy(raw,  0, src, 0, 4);
		this.src = InetAddress.getByAddress(src);
		System.arraycopy(raw, 4, dest, 0, 4);
		this.dest = InetAddress.getByAddress(dest);
		System.arraycopy(raw, 8, seqNr, 0, 4);
		this.seqNr = Helper.byteArrayToInteger(seqNr);
		System.arraycopy(raw, 12, ackNr, 0, 4);
		this.ackNr = Helper.byteArrayToInteger(ackNr);
		System.arraycopy(raw, 17, timeStamp, 0, 8);
		this.timeStamp = Helper.byteArrayToLong(timeStamp);
		System.arraycopy(raw, 26, fragmentNr, 0, 4);
		this.fragmentNr = Helper.byteArrayToInteger(fragmentNr);
		System.arraycopy(raw, 30, offset, 0, 4);
		this.offset = Helper.byteArrayToInteger(offset);
		System.arraycopy(raw, 34, packetID, 0, 4);
		this.packetNumber = Helper.byteArrayToInteger(packetID);
		System.arraycopy(raw, 38, dataL, 0, 4);
		this.dataL = Helper.byteArrayToInteger(dataL);
		if(this.dataL > 0) {
			this.data = new byte[this.dataL];
			System.arraycopy(raw, 42, this.data, 0, this.dataL);
		}
	}
	
	/**
	 * Turns the complete(read: all the values of the) package into a array of bytes
	 * @return  an array of bytes that represent this package
	 */
	public byte[] getBytes() {
		byte[] result = new byte[HEADER_SIZE + dataL];
		System.arraycopy(src.getAddress(),  0, result, 0, 4);
		System.arraycopy(dest.getAddress(), 0, result, 4, 4);
		System.arraycopy(Helper.integerToByteArray(seqNr), 0, result, 8, 4);
		System.arraycopy(Helper.integerToByteArray(ackNr), 0, result, 12, 4);
		result[16] = flag;
		System.arraycopy(Helper.longToByteArray(timeStamp), 0, result, 17, 8);
		result[25] = TTL;
		System.arraycopy(Helper.integerToByteArray(fragmentNr), 0, result, 26, 4);
		System.arraycopy(Helper.integerToByteArray(offset), 0, result, 30, 4);
		System.arraycopy(Helper.integerToByteArray(packetNumber), 0, result, 34, 4);
		System.arraycopy(Helper.integerToByteArray(dataL),0, result, 38, 4);
		if(data != null)
			System.arraycopy(data, 0, result, 42, dataL);
		return result;
	}
	
	/**
	 * Makes a copy of the header of a packages
	 * @return the copy of the header of a package
	 */
	public Packet copyHeader() {
		Packet result = new Packet(this.src, this.dest, this.seqNr, this.ackNr, this.flag, this.timeStamp,
				this.fragmentNr, this.offset, null);
		result.setTTL(this.TTL);
		return result;
	}
	
	public static String dataToString(byte[] data) {
		String result = null;
		try { result = new String(data, ENCODING);
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		return result;
	}

	public static String dataToString(List<Byte> data) {
		byte[] result = new byte[data.size()];
		for(int i = 0; i < data.size(); i++)
			result[i] = (byte)data.get(i);
		return dataToString(result);
	}
	
	public static byte[] dataToByteArray(String data) {
		if(data == null) { return null; }
		byte[] result = null;
		try { result = data.getBytes(ENCODING);
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		return result;
	}
	
	/**
	 * checks if the package is expired
	 * @return true if the packages is expired; false if the package is not expired
	 */
	public boolean isExpired() {
		return TTL<=0;
	}
	
	/**
	 * Decrease the TTL by 1;
	 */
	public void decreaseTTL() {
		System.out.println("Decrease TLL");
		TTL--;
	}
	
	/**
	 * get the source address of this package
	 * @return InetAddress that represents the source of the package
	 */
	public InetAddress getSrc() {
		return src;
	}

	/**
	 * set the source address of this package
	 * @param src the InetAddress of the source of this package
	 */
	public void setSrc(InetAddress src) {
		this.src = src;
	}

	/**
	 * get the destination address of the this package
	 * @return InetAddress that represents the destination of this package
	 */
	public InetAddress getDest() {
		return dest;
	}
	
	/**
	 * set the destination address of this package
	 * @param dest the InetAddress of the destination of this package
	 */
	public void setDest(InetAddress dest) {
		this.dest = dest;
	}

	/**
	 * gets the sequence number of this package
	 * @return an integer that represents the sequence number of this package
	 */
	public int getSeqNr() {
		return seqNr;
	}
	
	/**
	 * sets the sequence number of this package
	 * @param seqNr the new seqNr of the package
	 */
	public void setSeqNr(int seqNr) {
		this.seqNr = seqNr;
	}
	
	/**
	 * gets the acknowledgement number of this package
	 * @return an integer that represents the acknowledgement number of this package
	 */
	public int getAckNr() {
		return ackNr;
	}
	
	
	/**
	 * sets the acknowledgement number of this package
	 * @param ackNr the new acknowledgement number of the package
	 */
	public void setAckNr(int ackNr) {
		this.ackNr = ackNr;
	}
	
	/**
	 * get the flag of this package
	 * @return a byte that represents the flag of this package
	 */
	public byte getFlag() {
		return flag;
	}
	
	/**
	 * sets the flag of this package
	 * @param flag the new byte that represents the flag of this package
	 */
	public void setFlag(byte flag) {
		this.flag = flag;
	}
	
	/**
	 * gets the timeStamp of this package
	 * @return a long that represents the timeStamp of this package
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * sets the timeStamp of this package
	 * @param timeStamp a long that represents the timeStamp of this package
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * gets the time-to-live(TTL) of the package
	 * @return an byte that represents the TTL
	 */
	public byte getTTL() {
		return TTL;
	}
	
	/**
	 * sets the time-to-live(TTL) of the package
	 * @param TTL an byte that represents the TTL.
	 */
	public void setTTL(byte TTL) {
		this.TTL = TTL;
	}
	
	/**
	 * gets the packetID number
	 * @return an integer that represents the packetID number
	 */
	public int getPacketID() {
		return packetNumber;
	}
	
	/**
	 * sets the PacketID of this package
	 * @param ID an integer that represents the new packageID
	 */
	public void setPacketID(int ID) {
		packetNumber = ID;
	}
	
	/**
	 * get the data length of this package.
	 * @return an integer that represents the data length in bytes of this package.
	 */
	public int getDataL() {
		return dataL;
	}

	/**
	 * get the data of this package in bytes
	 * @return an byte array that represents the data of this package.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * set the data of this package
	 * @param data an byte array that represents the new data of this package.
	 */
	public void setData(byte[] data) {
		this.dataL = data.length;
		this.data = data;
	}
	
	/**
	 * get the Fragment number of this package
	 * @return an integer that represents the framgent number of this package
	 */
	public int getFragmentNr() {
		return fragmentNr;
	}

	/**
	 * sets the framgent number of this package
	 * @param fragmentNr an integer that represents the new fragment number of this package.
	 */
	public void setFragmentNr(int fragmentNr) {
		this.fragmentNr = fragmentNr;
	}

	/**
	 * gets the Offset of this package
	 * @return an integer that represents the Offset of this package
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * sets the Offset of this package
	 * @param offset an integer that represents the offset of this package
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
