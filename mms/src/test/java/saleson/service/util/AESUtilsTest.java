package saleson.service.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import saleson.common.util.DateUtils;
import unused.util.AESUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AESUtilsTest {

	@Test
	void generateKey() throws NoSuchAlgorithmException {
		String name = "abb";
		String key = name + DateUtils.getToday("yyMMdd") + UUID.randomUUID().toString().replaceAll("-", "");
		key = key.toUpperCase().substring(0, 16);
		String iv = generateIv(key);

		log.debug("KEY: {}, IV: {}", key, iv);
	}

	public String generateIv(String key) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(key.getBytes());

		StringBuilder builder = new StringBuilder();
		for (byte b: md.digest()) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString().substring(0, 16).toUpperCase();
	}


	@Test
	void encryptTest() throws Exception {
		String str = "at=PRESET&ti=TMS1744KRB001&ci=CMS1744I04002&sc=37&tv=2.0.0";
		String encStr = AESUtils.encrypt(str);
		log.debug("ENC: {}", encStr);

		String decStr = AESUtils.decrypt(encStr);
		log.debug("DEC: {}", decStr);

		assertThat(decStr).isEqualTo(str);
	}
}