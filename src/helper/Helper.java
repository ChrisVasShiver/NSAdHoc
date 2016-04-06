package helper;

public class Helper {

	public static byte[] integerToByteArray(int i) {
		byte[] result = new byte[4];

		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i);
		
		return result;
	}
	
	public static int byteArrayToInteger(byte[] bs) {
		 return bs[0] << 24 | (bs[1] & 0xFF) << 16 | (bs[2] & 0xFF) << 8 | (bs[3] & 0xFF);
	}
}
