package helper;

import java.io.UnsupportedEncodingException;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher 
 */
public class FilePacket {
	private static final String ASCII = "ASCII";
	private String filename;
	private byte[] data;
	
	public FilePacket(String filename, byte[] data) {
		this.filename = filename;
		this.data = data;
	}
	
	/**
	 * Builds up a raw packet that can be send.
	 * @param rawPacket packet yet to be build
	 */
	public FilePacket(byte[] rawPacket) {
		byte[] filenameLength = new byte[4];
		System.arraycopy(rawPacket, 0, filenameLength, 0, 4);
		int fLength = Helper.byteArrayToInteger(filenameLength);
		byte[] filename = new byte[fLength];
		System.arraycopy(rawPacket, 4, filename, 0, fLength);
		this.filename = decodeString(filename);
		byte[] dataLength = new byte[4];
		System.arraycopy(rawPacket, 4 + fLength, dataLength, 0, 4);
		int dLength = Helper.byteArrayToInteger(dataLength);
		this.data = new byte[dLength];
		System.arraycopy(rawPacket, 4 + fLength + 4, this.data, 0, dLength);
	}
	
	/**
	 * 
	 * @return bytes that can be send
	 */
	public byte[] getBytes() {
		byte[] filenameBytes = encodeString(this.filename);
		int fLength = filenameBytes.length;
		byte[] result = new byte[4 + fLength + 4 + data.length];
		System.arraycopy(Helper.integerToByteArray(fLength), 0, result, 0, 4);
		System.arraycopy(filenameBytes, 0, result, 4, fLength);
		System.arraycopy(Helper.integerToByteArray(data.length),0, result, 4 + fLength, 4);
		System.arraycopy(data, 0,result, 4 + fLength + 4,  data.length);
		return result;	
	}
	
	public String getFilename() {
		return filename;
	}
	
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Turns bs into ASCII symbols
	 * @param bs bytes to be decoded
	 * @return String of ASCII symbols
	 */
	private static String decodeString(byte[] bs) {
		String result = null;
		try {
			result = new String(bs, ASCII);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Turns s into a array of bytes
	 * @param s String of ASCII symbols
	 * @return array of bytes
	 */
	private static byte[] encodeString(String s) {
		byte[] result = null;
		try {
			result = s.getBytes(ASCII);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
