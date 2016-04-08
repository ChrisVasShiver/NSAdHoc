package security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

public class SymmetricEncryption {

	final static String algorithm = "AES";
	private static Cipher aescipher;

	public SymmetricEncryption() {
		setUp();
	}
	
	private void setUp(){
		try {
			aescipher = Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public Key generateKey() throws Exception {
		Key key = KeyGenerator.getInstance(algorithm).generateKey();
		return key;
	}

	public byte[] encrypt(String input, Key key) throws InvalidKeyException,
			BadPaddingException, IllegalBlockSizeException {
		aescipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] inputBytes = input.getBytes();
		return aescipher.doFinal(inputBytes);
	}

	public String decrypt(byte[] encryptionBytes, Key key)
			throws InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException {
		aescipher.init(Cipher.DECRYPT_MODE, key);
		byte[] recoveredBytes = aescipher.doFinal(encryptionBytes);
		String recovered = new String(recoveredBytes);
		return recovered;
	}

}
