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
		 byte[] privateKey = hb.getPrivateKey();
		 byte[] key = hb.generateEncryptedKey(publicKey);
		 hb.asEn.decrypt(key, privateKey);
		 hb.decryptAndStoreKey(key);	
//		 byte[] m = hb.encryptMessage(Base64.decodeBase64(test));
		 //byte[] message = hb.decryptMessage(m);
		// System.out.println(Base64.encodeBase64String(message));		 
	 }
	
	public Key secretKey;
	SymmetricEncryption symEn;
	public AsymmetricEncryption asEn;

	public HybridEncryption() {
		symEn = new SymmetricEncryption();
		asEn = new AsymmetricEncryption();
		System.out.println(asEn.getPrivateKey().getFormat());
		System.out.println(asEn.getPublicKey().getFormat());
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
	
	public byte[] getPrivateKey() {
		return asEn.getPrivateKey().getEncoded();
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
