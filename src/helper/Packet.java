package helper;

import java.net.InetAddress;

public class Packet {
	private InetAddress src;
	private InetAddress dest;
	private int seqNr;
	private int ackNr;
	private int timeStamp;
	private int TTL;
	private int dataL;
	private String data;
		
	public Packet(InetAddress src, InetAddress dest, int seqNr, int ackNr, int timeStamp, int TTL, String data) {
		this.src = src;
		this.dest = dest;
		this.seqNr = seqNr;
		this.ackNr = ackNr;
		this.timeStamp = timeStamp;
		this.TTL = TTL;
		this.dataL = data.getBytes().length;
		this.data = data;
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

}
