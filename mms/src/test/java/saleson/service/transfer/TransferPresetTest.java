package saleson.service.transfer;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import saleson.common.util.StringUtils;
import saleson.model.Preset;
import saleson.service.util.CryptoUtils;
import saleson.service.util.DecryptInfo;
import unused.util.AESUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TransferPresetTest {
	public static final String KEY = "COUNTER!@#GLOBAL";
	public static final String IV = "GLOBAL!@#COUNTER";
	public static final String SERVER_URL = "https://dev.emoldino.com/mms/transfer/data";
	private MockMvc mvc;

	@Test
	public void PRESET()  throws Exception {
		String ci = "NCM2025I01023xxx";
		String data = "at=PRESET&ci=" + ci + "&ti=BTM2025KRE002&tv=1.0.0";	// ti 버전에 따라 응답 형식이 달라진다.

		log.debug("[PRESET] REQUEST-1 =================================================================================== ");
		log.debug("[PRESET] REQUEST-1: " + data);

		String response = sendToServer(data);
		PresetResult result1 = new PresetResult(response);

		log.debug("[PRESET] RESULT-1: " + result1);

		if (result1.isSuccess()) {
			data += "&sc=" + result1.getSc();
			System.out.println();
			log.debug("[PRESET] REQUEST-2 =================================================================================== ");
			log.debug("[PRESET] REQUEST-2: " + data);

			response = sendToServer(data);
			PresetResult result2 = new PresetResult(response);

			log.debug("[PRESET] RESULT2: " + result2);
		} else {
			log.debug("[PRESET] AUTH_FAIL");
		}
	}

	@Test
	public void descryptTest() throws Exception {
		List<String> messages = Arrays.asList(
				"gspZAJJHBv%2FmAtJ3UHj3NuJfZAPRI7F9NklCZLAQ8lg%3D",
				"mn7RC1Kb%2BGcEGZjrtuXHQw%3D%3D",
				"uEXa52gRznaAO1WpkRBvHd39OwUo0RFLKINbmwazVQc%3D",
				"R%2Bz0C4oC5Rl%2BOgeUajfvVg%3D%3D",
				"DFu8B3JDTp41f%2FTuNssXcg%3D%3D"
		);
		messages.stream().forEach(m -> {
			try {
				System.out.println(m + " ==> " + decryptString(m));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


	private String decryptString(String encText) throws Exception {
		CryptoUtils cryptoUtils = new CryptoUtils();
		DecryptInfo decryptInfo = cryptoUtils.decrypt(URLDecoder.decode(encText, "UTF-8"), KEY, IV);
		return decryptInfo.getDecryptText();
	}

	private String sendToServer(String... data) throws Exception {
		String url = SERVER_URL;

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		AESUtils aesUtils =new AESUtils();
		aesUtils.setKey(KEY);
		aesUtils.setIV(IV);
		for (int i = 0; i < data.length; i++) {
			String q = AESUtils.encrypt(data[i]);
			params.add("q", q);
		}


		RestTemplate restTemplate = new RestTemplateBuilder().build();
		return restTemplate.postForObject(url, params, String.class);
	}

	private String resultDecoder(String enc) throws Exception {
		enc = enc.replaceAll("%2A", "\\*");
		return AESUtils.decrypt(URLDecoder.decode(enc, "UTF-8"));

	}

	@Data @ToString
	class PresetResult {
		private String sc;   // sc or AUTH_FAIL
		private String presetYn;
		private String alarmShot;				// 알람 설정값
		private String alarmYn;			// 알람 설정
		private String alarmRepeatYn;		// 카운터 SHOT이 알람 설정 값 만큼 증가시 통신 (Y:연속, N:1회)
		private String shockValue;				// 충격 임계값 (0~255)
		private String shockYn;

		public PresetResult(String response) throws Exception {
			log.debug("[PRESET] Server response: " + System.lineSeparator() + "{}", response);
			String[] results = StringUtils.delimitedListToStringArray(response, System.lineSeparator());

			String responseData = "";
			if (results.length == 4) {
				responseData = resultDecoder(results[2]);

				String[] delimitedData =  StringUtils.delimitedListToStringArray(responseData, ",");

				this.sc = delimitedData[0];

				if (delimitedData.length == 7) {
					this.presetYn = delimitedData[1];
					this.alarmShot = delimitedData[2];
					this.alarmYn = delimitedData[3];
					this.alarmRepeatYn = delimitedData[4];
					this.shockValue = delimitedData[5];
					this.shockYn = delimitedData[6];
				}

				log.debug("[PRESET] Server message: {}", responseData);
				log.debug("[PRESET] SC: {}", sc);
			} else {
				log.debug("[PRESET] ERROR DATA");
			}
		}

		public boolean isSuccess() {
			return sc == null || "AUTH_FAIL".equals(sc) ? false : true;
		}

		private String resultDecoder(String enc) throws Exception {
			enc = enc.replaceAll("%2A", "\\*");
			return AESUtils.decrypt(URLDecoder.decode(enc, "UTF-8"));
		}

	}
}
