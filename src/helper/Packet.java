package helper;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Packet implements Comparable<Packet> {
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
	
	public boolean isExpired() {
		return TTL <= 0;
	}
	
	public void decreaseTTL() {
		System.out.println("Decrease TLL");
		TTL--;
	}
	
	public InetAddress getSrc() {
		return src;
	}

	public void setSrc(InetAddress src) {
		this.src = src;
	}

	public InetAddress getDest() {
		return dest;
	}

	public void setDest(InetAddress dest) {
		this.dest = dest;
	}

	public int getSeqNr() {
		return seqNr;
	}

	public void setSeqNr(int seqNr) {
		this.seqNr = seqNr;
	}

	public int getAckNr() {
		return ackNr;
	}
	
	public void setAckNr(int ackNr) {
		this.ackNr = ackNr;
	}

	public byte getFlag() {
		return flag;
	}
	
	public void setFlag(byte flag) {
		this.flag = flag;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public byte getTTL() {
		return TTL;
	}

	public void setTTL(byte tTL) {
		TTL = tTL;
	}

	public int getPacketID() {
		return packetNumber;
	}
	
	public void setPacketID(int ID) {
		packetNumber = ID;
	}
	public int getDataL() {
		return dataL;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.dataL = data.length;
		this.data = data;
	}

	public int getFragmentNr() {
		return fragmentNr;
	}

	public void setFragmentNr(int fragmentNr) {
		this.fragmentNr = fragmentNr;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	@Override
	public String toString() {
		return "Packet: " + getData();
	}

	@Override
	public int compareTo(Packet other) {
		if(other.getFragmentNr() < this.fragmentNr)
			return -1;
		else if(other.getFragmentNr() == this.fragmentNr)
			return 0;
		else
			return 1;
	}


}
