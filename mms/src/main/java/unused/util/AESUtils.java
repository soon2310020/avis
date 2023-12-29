package unused.util;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Deprecated
@Component
public class AESUtils {
	//private final static String
	private final static String ALGORITHM = "AES";

	private static String KEY = "VIETNAM!IMMS1234";
	private static String IV = "IMMS1234!VIETNAM";

	@Value("${crypto.aes.secret.key}")
	public void setKey(String value) {
		KEY = value;
	}

	@Value("${crypto.aes.iv.parameter}")
	public void setIV(String value) {
		IV = value;
	}

	public static String encrypt(String plaintext) throws Exception {
		String encrypted_b64 = null;
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		SecretKeySpec k = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(IV.getBytes()));
		byte[] encryptedData = c.doFinal(plaintext.getBytes());
		encrypted_b64 = Base64.encodeBase64String(encryptedData);
		return encrypted_b64;
	}

	public static String decrypt(String encryptext) throws Exception {
		String plaintext = null;
		byte[] encrypted_b64 = Base64.decodeBase64(encryptext);
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		SecretKeySpec k = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(IV.getBytes()));
		byte[] plainData = c.doFinal(encrypted_b64);
		plaintext = new String(plainData, "UTF-8");
		return plaintext;
	}
}
