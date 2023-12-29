package saleson.service.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CryptoUtilsTest {

	@Test
	void encryptAndDecryptTest() throws Exception {
		String data = "at=TDATA";

		CryptoUtils cryptoUtils = new CryptoUtils();
		String q =  cryptoUtils.encrypt(data);

		log.debug("q={}", q);

		String q2 =  cryptoUtils.encrypt(data, CryptoUtils.KEY1, CryptoUtils.IV1);

		log.debug("q2={}", q2);
		assertThat(q).isEqualTo(q2);

		String q3 =  cryptoUtils.encrypt(data, CryptoUtils.KEY2, CryptoUtils.IV2);
		log.debug("q3={}", q3);
		assertThat(q).isNotEqualTo(q3);

		DecryptInfo d1 = cryptoUtils.decrypt(q);
		DecryptInfo d2 = cryptoUtils.decrypt(q2);
		DecryptInfo d3 = cryptoUtils.decrypt(q3);

		log.debug("d1={}", d1);
		log.debug("d2={}", d2);
		log.debug("d3={}", d3);

		assertThat(d1).isNotEqualTo(d2);
		assertThat(d1).isNotEqualTo(d3);
		assertThat(d2).isNotEqualTo(d3);

	}

	@Test
	void decrypt() throws Exception {
		// dyson preset 테스트
		// http -f -v POST https://ds0124.emoldino.com/mms/transfer/data q=Q/ERoXO654sxuZImDOWwahMO0CLjMMQv4WudMDjn5FDP8EQyFkrqhMG88k0uqGWcIs0RHbbiphoQ47eQDSyd/Q==

		String data = "B9%2F4P%2FHNaEruUaasLjmkig%3D%3D";
		DecryptInfo info = resultDecoder(data, CryptoUtils.KEY1, CryptoUtils.IV1);

		log.debug("DESCRYPT : {}", info);
	}

	private DecryptInfo resultDecoder(String enc, String key, String iv) throws Exception {
		CryptoUtils cryptoUtils = new CryptoUtils();
		enc = enc.replaceAll("%2A", "\\*");
		return cryptoUtils.decrypt(URLDecoder.decode(enc, "UTF-8"), key, iv);

	}
}