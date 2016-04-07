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
	
	public static byte[] longToByteArray(long l) {
		byte[] result = new byte[8];

		result[0] = (byte) (l >> 56);
		result[1] = (byte) (l >> 48);
		result[2] = (byte) (l >> 40);
		result[3] = (byte) (l >> 32);
		result[4] = (byte) (l >> 24);
		result[5] = (byte) (l >> 16);
		result[6] = (byte) (l >> 8);
		result[7] = (byte) (l);
		
		return result;
	}
	public static int byteArrayToInteger(byte[] bs) {
		 return bs[0] << 24 | (bs[1] & 0xFF) << 16 | (bs[2] & 0xFF) << 8 | (bs[3] & 0xFF);
	}
	
	public static long byteArrayToLong(byte[] bs) {
		long result = 0;
		for(int i = 0; i < bs.length; i++)
			result = (result << 8) + (bs[i] & 0xff);
		return result;
	}
}
