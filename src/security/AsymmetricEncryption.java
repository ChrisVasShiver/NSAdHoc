package security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class AsymmetricEncryption {
	
	private static final String algorithm = "RSA";
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public AsymmetricEncryption() {
		createKeys();
	}

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

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public byte[] encrypt(byte[] dataToBeEncrypted, byte[] publicKeyBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		PublicKey publicKey = 
			    KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(dataToBeEncrypted);
	}

	public byte[] decrypt(byte[] encryptedData) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(encryptedData);
	}
	
}
