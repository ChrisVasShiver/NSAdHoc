package security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class AsymmetricEncryption {

	private PrivateKey privateKey;
	private PublicKey publicKey;

	public AsymmetricEncryption() {
		createKeys();
	}

	public void createKeys() {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyPairGenerator.initialize(1024);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		privateKey = keyPair.getPrivate();
		publicKey = keyPair.getPublic();
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public String encrypt(String dataToBeEncrypted) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		String encryptedData = Base64.encodeBase64String(cipher
				.doFinal(dataToBeEncrypted.getBytes()));
		return encryptedData;
	}

	public String decrypt(String encryptedData) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		String decryptedData = new String(cipher.doFinal(Base64
				.decodeBase64(encryptedData)));
		return decryptedData;
	}

}
