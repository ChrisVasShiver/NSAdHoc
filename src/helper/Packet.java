package helper;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import main.Client;

public class Packet {
	public final static byte ACK = 0x01; // Acknowledgement
	public final static byte SYN = 0x02; // Synchronize
	public final static byte FIN = 0x04; // Finish
	public final static byte GRP = 0x08; // Group
	public final static byte FRG = 0x10; // Fragmented
	public final static int HEADER_SIZE = 38;
	public final static String ENCODING = "UTF-16BE";
	private final static int BASICTTL = 12;
	private InetAddress src;
	private InetAddress dest;
	private int seqNr;
	private int ackNr;
	private byte flag;
	private long timeStamp;
	private int TTL;
	private int fragmentNr;
	private int offset;
	private int dataL;
	private String data;
		
	/**
	 * 
	 * @param src
	 * @param dest
	 * @param seqNr
	 * @param ackNr
	 * @param flag
	 * @param timeStamp
	 * @param fragmentNr
	 * @param offset
	 * @param data
	 */
	public Packet(InetAddress src, InetAddress dest, int seqNr, int ackNr, byte flag, long timeStamp, 
			int fragmentNr, int offset, String data) {
		this.src = src;
		this.dest = dest;
		this.seqNr = seqNr;
		this.ackNr = ackNr;
		this.flag = flag;
		this.timeStamp = timeStamp;
		this.TTL = BASICTTL;
		this.fragmentNr = fragmentNr;
		this.offset = offset;
		if(data == null)
			this.dataL = 0;
		else
			this.dataL = dataToByteArray(data).length;
		this.data = data;
	}
	
	public Packet(byte[] raw) throws UnknownHostException {
		byte[] src = new byte[4];
		byte[] dest = new byte[4];
		byte[] seqNr = new byte[4];
		byte[] ackNr = new byte[4];
		this.flag = raw[16];
		byte[] timeStamp = new byte[8];
		this.TTL = (int)(raw[25]);
		byte[] fragmentNr = new byte[4];
		byte[] offset = new byte[4];
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
		System.arraycopy(raw, 34, dataL, 0, 4);
		this.dataL = Helper.byteArrayToInteger(dataL);
		byte[] data = new byte[this.dataL];
		System.arraycopy(raw, 38, data, 0, this.dataL);
		this.data = dataToString(data);
	}
	
	
	public byte[] getBytes() {
		byte[] result = new byte[HEADER_SIZE + dataL];
		System.arraycopy(src.getAddress(),  0, result, 0, 4);
		System.arraycopy(dest.getAddress(), 0, result, 4, 4);
		System.arraycopy(Helper.integerToByteArray(seqNr), 0, result, 8, 4);
		System.arraycopy(Helper.integerToByteArray(ackNr), 0, result, 12, 4);
		result[16] = flag;
		System.arraycopy(Helper.longToByteArray(timeStamp), 0, result, 17, 8);
		System.arraycopy(Helper.integerToByteArray(TTL), 0, result, 25, 1);
		System.arraycopy(Helper.integerToByteArray(fragmentNr), 0, result, 26, 4);
		System.arraycopy(Helper.integerToByteArray(offset), 0, result, 30, 4);
		System.arraycopy(Helper.integerToByteArray(dataL), 0, result, 34, 4);
		if(data != null)
			System.arraycopy(dataToByteArray(data), 0, result, 38, dataL);
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
	
	public static byte[] dataToByteArray(String data) {
		byte[] result = null;
		try { result = data.getBytes(ENCODING);
		} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		return result;
	}
	
	public boolean isExpired() {
		return TTL==0;
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

	public int getTTL() {
		return TTL;
	}

	public void setTTL(int tTL) {
		TTL = tTL;
	}

	public int getDataL() {
		return dataL;
	}

	public void setDataL(int dataL) {
		this.dataL = dataL;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.dataL = dataToByteArray(ENCODING).length;
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


}
