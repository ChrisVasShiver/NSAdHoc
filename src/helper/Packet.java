package helper;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Packet {
	private final static int BASICL = 26;
	private final static int BASICTTL = 12;
	private InetAddress src;
	private InetAddress dest;
	private int seqNr;
	private int ackNr;
	private byte flag;
	private int timeStamp;
	private int TTL;
	private int dataL;
	private String data;
		
	public Packet(InetAddress src, InetAddress dest, int seqNr, int ackNr, int timeStamp, String data) {
		this.src = src;
		this.dest = dest;
		this.seqNr = seqNr;
		this.ackNr = ackNr;
		this.timeStamp = timeStamp;
		this.TTL = BASICTTL;
		this.dataL = 0;
		try {
		this.dataL = data.getBytes("UTF-16BE").length;
		} catch (Exception e) {}
		this.data = data;
	}
	
	public Packet(byte[] raw) throws UnknownHostException {
		byte[] src = new byte[4];
		byte[] dest = new byte[4];
		byte[] seqNr = new byte[4];
		byte[] ackNr = new byte[4];
		this.flag = raw[16];
		byte[] timeStamp = new byte[4];
		this.TTL = (int)(raw[21]);
		byte[] dataL = new byte[4];
		System.arraycopy(raw,  0, src, 0, 4);
		this.src = InetAddress.getByAddress(src);
		System.arraycopy(raw, 4, dest, 0, 4);
		this.dest = InetAddress.getByAddress(dest);
		System.arraycopy(raw, 8, seqNr, 0, 4);
		this.seqNr = Helper.byteArrayToInteger(seqNr);
		System.arraycopy(raw, 12, ackNr, 0, 4);
		this.ackNr = Helper.byteArrayToInteger(ackNr);
		System.arraycopy(raw, 17, timeStamp, 0, 4);
		this.timeStamp = Helper.byteArrayToInteger(timeStamp);
		System.arraycopy(raw, 22, dataL, 0, 4);
		this.dataL = Helper.byteArrayToInteger(dataL);
		byte[] data = new byte[this.dataL];
		System.arraycopy(raw, 26, data, 0, this.dataL);
		try {
			this.data = new String(data, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Hier kom ik niet als ik de string goed heb getypt!");
		}
		
		
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

	public int getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(int timeStamp) {
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
		this.data = data;
	}
	
	public boolean isExpired() {
		return TTL==0;
	}
	
	public void decreaseTTL() {
		TTL--;
	}
	
	public byte[] getBytes() {
		byte[] result = new byte[BASICL + dataL];
		System.arraycopy(src.getAddress(),  0, result, 0, 4);
		System.arraycopy(dest.getAddress(), 0, result, 4, 4);
		System.arraycopy(Helper.integerToByteArray(seqNr), 0, result, 8, 4);
		System.arraycopy(Helper.integerToByteArray(ackNr), 0, result, 12, 4);
		result[16] = flag;
		System.arraycopy(Helper.integerToByteArray(timeStamp), 0, result, 17, 4);
		System.arraycopy(Helper.integerToByteArray(TTL), 0, result, 21, 1);
		System.arraycopy(Helper.integerToByteArray(dataL), 0, result, 22, 4);
		if(data != null) {
		try {
			System.arraycopy(data.getBytes("UTF-16BE"), 0, result, 26, dataL);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		return result;
	}

}
