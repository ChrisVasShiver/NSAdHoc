package security;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HybridEncryption {
	
// for testing the code
	
//	 public static void main(String[] args) throws Exception{
//		 HybridEncryption hb = new HybridEncryption();
//		 String key = hb.generateEncryptedKey();
//		 System.out.println(hb.secretKey.toString());
//		 hb.decryptAndStoreKey(key);
//		 System.out.println(hb.secretKey);	 
//		 byte[] m = hb.encryptMessage("Hello World");
//		 String message = hb.decryptMessage(m);
//		 System.out.println(message);		 
//	 }
	
	public Key secretKey;
	SymmetricEncryption symEn;
	AsymmetricEncryption asEn;

	public HybridEncryption() {
		symEn = new SymmetricEncryption();
		asEn = new AsymmetricEncryption();
	}

	public String generateEncryptedKey(){
		try {
			secretKey = symEn.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String keyToString = Base64.encodeBase64String(secretKey.getEncoded());
		String encryptedKey = null;
		try {
			encryptedKey = asEn.encrypt(keyToString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedKey;
	}

	public void decryptAndStoreKey(String encryptedKey){
		String decryptedKey = null;
		try {
			decryptedKey = asEn.decrypt(encryptedKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte keyBytes[] = Base64.decodeBase64(decryptedKey);
		secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
	}

	public byte[] encryptMessage(String message){
		byte[] encryptedMessage = null;
		try {
			encryptedMessage = symEn.encrypt(message, secretKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return encryptedMessage;
	}

	public String decryptMessage(byte[] encryptedMessage){
		String decryptedMessage = null;
		try {
			decryptedMessage = symEn.decrypt(encryptedMessage, secretKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return decryptedMessage;
	}

}
