package security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * This class is used to create a public/private keypair. The public key can be
 * sent to someone else who can encrypt a message with it, preferably a secret
 * key, and the private key can be used to decrypt that encrypted message.
 * 
 * @author Bas
 *
 */
public class AsymmetricEncryption {

	// The algorithm for generating the keys and the keys themselves are stored
	// here.
	private static final String algorithm = "RSA";
	private PrivateKey privateKey;
	private PublicKey publicKey;

	/**
	 * Default constructor that create the public and private key
	 */
	public AsymmetricEncryption() {
		createKeys();
	}

	/**
	 * Creates a new random public and private keyset
	 */
	public void createKeys() {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyPairGenerator.initialize(1024);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		privateKey = keyPair.getPrivate();
		publicKey = keyPair.getPublic();
	}

	/**
	 * Asks for the public key (so that it can be sent)
	 * 
	 * @return the public key of the public/private keypair
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Encrypts data using a public key
	 * 
	 * @param dataToBeEncrypted
	 *            the data that needs to be encrypted in bytes
	 * @param publicKeyBytes
	 *            the public key that has to be used to encrypt the bytes
	 * @return the encrypted data in an array of bytes
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] dataToBeEncrypted, byte[] publicKeyBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		PublicKey publicKey = KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(dataToBeEncrypted);
	}

	/**
	 * Decrypts data using a private key
	 * 
	 * @param encryptedData
	 *            the data that needs to be decrypted
	 * @return the decrypted data in an array of bytes
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] encryptedData) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(encryptedData);
	}

}
