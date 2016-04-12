package security;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

// This class is used for encrypting Single Connections. It combines the classes AsymmetricEncryption and SymmetricEncryption.

public class HybridEncryption {

	/**
	 * for testing the code
	 */

	public static void main(String[] args) throws Exception {
		// HybridEncryption hb = new HybridEncryption();
		// byte[] publicKey = hb.getPublicKey();
		// byte[] privateKey = hb.getPrivateKey();
		// byte[] key = hb.generateEncryptedKey(publicKey);
		// hb.asEn.decrypt(key, privateKey);
		// hb.decryptAndStoreKey(key);
		// byte[] m = hb.encryptMessage(Base64.decodeBase64(test));
		// byte[] message = hb.decryptMessage(m);
		// System.out.println(Base64.encodeBase64String(message));
	}

	/**
	 * The secret key that both ends of the connection should have at some point
	 */
	private Key secretKey;
	private SymmetricEncryption symEn;
	private AsymmetricEncryption asEn;

	/**
	 * Default constructor that initiates an instance of SymmetricEncryption and
	 * AsymmetricEncryption
	 */
	public HybridEncryption() {
		symEn = new SymmetricEncryption();
		asEn = new AsymmetricEncryption();
	}

	/**
	 * Generates a secret key and encrypts it with a public key
	 * 
	 * @param publicKey
	 *            the key that is used to encrypt the secret key
	 * @return returns an encrypted secret key
	 */
	public byte[] generateEncryptedKey(byte[] publicKey) {
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

	/**
	 * Asks for the public key of the keypair generated in AsymmetricEncryption
	 * 
	 * @return a public key
	 */
	public byte[] getPublicKey() {
		return asEn.getPublicKey().getEncoded();
	}

	/**
	 * Decrypts an encrypted secret key and stores it in this class.
	 * 
	 * @param encryptedKey
	 *            the encrypted key that needs to be decrypted
	 */
	public void decryptAndStoreKey(byte[] encryptedKey) {
		byte[] decryptedKey = null;
		try {
			decryptedKey = asEn.decrypt(encryptedKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		secretKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");
	}

	/**
	 * Encrypts a byte message using a secret key
	 * 
	 * @param message
	 *            the message to be encrypted
	 * @return returns an encrypted byte message
	 */
	public byte[] encryptMessage(byte[] message) {
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

	/**
	 * Decrypts a byte message using a secret key
	 * 
	 * @param encryptedMessage
	 *            the message to be decrypted
	 * @return returns a decrypted byte message
	 */
	public byte[] decryptMessage(byte[] encryptedMessage) {
		byte[] decryptedMessage = null;
		try {
			decryptedMessage = symEn.decrypt(encryptedMessage, secretKey);
		} catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return decryptedMessage;
	}
}
