package security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

/**
 * This class is used to encrypt and decrypt data with a secret AES-based key.
 * The secret key can be sent, preferably via an Asymmetric Encryption method.
 * 
 * @author Bas
 *
 */
public class SymmetricEncryption {

//  The encryption algorithm that will be used to create a secret key and the cipher to encrypt the data are stored here
	private final static String algorithm = "AES";
	private static Cipher aescipher;

	/**
	 * Default constructor that initiates the cipher
	 */
	public SymmetricEncryption() {
		setUpCipher();
	}

	/**
	 * Sets up the cipher
	 */
	private void setUpCipher() {
		try {
			aescipher = Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a secret key
	 * @return a random secret key
	 * @throws Exception
	 */
	public Key generateKey() throws Exception {
		Key key = KeyGenerator.getInstance(algorithm).generateKey();
		return key;
	}

	/**
	 * Encrypts a piece of byte data using a secret key
	 * @param input the data that has to be encrypted
	 * @param key the secret key that has to be used to encrypt the data
	 * @return the encrypted data in byte form
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public byte[] encrypt(byte[] input, Key key)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		aescipher.init(Cipher.ENCRYPT_MODE, key);
		return aescipher.doFinal(input);
	}

	/**
	 * Decrypts a piece of data using a secret key
	 * @param encryptionBytes the data that has to be decrypted
	 * @param key the key that has to be used to decrypt the data with
	 * @return the decrypted data in byte form
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public byte[] decrypt(byte[] encryptionBytes, Key key)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		aescipher.init(Cipher.DECRYPT_MODE, key);
		return aescipher.doFinal(encryptionBytes);
	}

}
