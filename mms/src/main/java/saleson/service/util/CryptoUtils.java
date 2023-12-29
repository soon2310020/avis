package saleson.service.util;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {
	public static final String KEY1 = "VIETNAM!IMMS1234";
	public static final String IV1 = "IMMS1234!VIETNAM";

	// New Terminal / Counter
	public static final String KEY2 = "COUNTER!@#GLOBAL";
	public static final String IV2 = "GLOBAL!@#COUNTER";

	//private final String
	private final String ALGORITHM = "AES";

	private String KEY = KEY1;
	private String IV = IV1;


	public String encrypt(String plaintext) throws Exception{
		return encrypt(plaintext, KEY, IV);
	}

	public String encrypt(String plaintext, String key, String iv) throws Exception{
		String encrypted_b64=null;
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		SecretKeySpec k = new SecretKeySpec(key.getBytes(), ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, k,new IvParameterSpec(iv.getBytes()));
		byte[] encryptedData = c.doFinal(plaintext.getBytes());
		encrypted_b64 =  Base64.encodeBase64String(encryptedData);
		return encrypted_b64;
	}

	public DecryptInfo decrypt(String encryptext) throws Exception {
		try {
			DecryptInfo decryptInfo = decrypt(encryptext, KEY, IV);
			if (decryptInfo.getDecryptText().startsWith("at")) {
				return decryptInfo;
			}

			return decryptAfterKeyChange(encryptext);
		} catch (Exception e) {
			return decryptAfterKeyChange(encryptext);
		}
	}

	private DecryptInfo decryptAfterKeyChange(String encryptext) throws Exception {
		String key = KEY.equals(KEY1) ? KEY2 : KEY1;
		String iv = IV.equals(IV1) ? IV2 : IV1;
		return decrypt(encryptext, key, iv);
	}

	public String decryptString(String encryptext) throws Exception {
		DecryptInfo decryptInfo = decrypt(encryptext);
		return decryptInfo.getDecryptText();
	}

	public DecryptInfo decrypt(String encryptext, String key, String iv) throws Exception {
		String plaintext = null;
		byte[] encrypted_b64 = Base64.decodeBase64(encryptext);
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		SecretKeySpec k = new SecretKeySpec(key.getBytes(), ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, k,new IvParameterSpec(iv.getBytes()));
		byte[] plainData = c.doFinal(encrypted_b64);
		plaintext = new String(plainData, "UTF-8");
		return new DecryptInfo(plaintext, key, iv);
	}
}
