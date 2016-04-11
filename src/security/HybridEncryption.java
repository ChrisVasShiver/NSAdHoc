package security;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class HybridEncryption {
	
// for testing the code
	
	 public static void main(String[] args) throws Exception{
		 HybridEncryption hb = new HybridEncryption();
		 byte[] publicKey = hb.getPublicKey();
		 byte[] key = hb.generateEncryptedKey(publicKey);
		 String test = "noQDopuxMGyR9FNIphrYlYw1WvA5/xpDQfW0jfphFOPhPuFcaEC9OmVU1J6UJHCLJh7cEf/zXSBCrByqhD0ylun0EABdu1V2uggPqRPWIOZy5/fbCET7DAFm0TeLgtZVq2kOhe+gUjvxHPG+2N9K2GTmE1FW97H1jD0gdSQoaaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
		 key = Base64.decodeBase64(test);
		// hb.decryptAndStoreKey(key);	
		 byte[] message = hb.asEn.decrypt(key);
		 byte[] m = hb.encryptMessage(Base64.decodeBase64(test));
		 //byte[] message = hb.decryptMessage(m);
		 System.out.println(Base64.encodeBase64String(message));		 
	 }
	
	public Key secretKey;
	SymmetricEncryption symEn;
	AsymmetricEncryption asEn;

	public HybridEncryption() {
		symEn = new SymmetricEncryption();
		asEn = new AsymmetricEncryption();
	}

	public byte[] generateEncryptedKey(byte[] publicKey){
		try {
			secretKey = symEn.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] keyToString = secretKey.getEncoded();
		byte[] encryptedKey = null;
		try {
			encryptedKey = asEn.encrypt(keyToString, publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedKey;
	}

	public byte[] getPublicKey() {
		return asEn.getPublicKey().getEncoded();
	}
	public void decryptAndStoreKey(byte[] encryptedKey){
		byte[] decryptedKey = null;
		try {
			decryptedKey = asEn.decrypt(encryptedKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		secretKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");
	}

	
	public byte[] encryptMessage(byte[] message){
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

	public byte[] decryptMessage(byte[] encryptedMessage){
		byte[] decryptedMessage = null;
		try {
			decryptedMessage = symEn.decrypt(encryptedMessage, secretKey);
		} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return decryptedMessage;
	}
}
