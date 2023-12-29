package saleson.service.transfer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import saleson.common.util.DateUtils;
import unused.util.AESUtils;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class TransferJunitTest {
	private MockMvc mvc;

	@Test
	public void adata() throws Exception {
		String q = "at=ADATA&id=T0001/NCM2021I01002&time=20200618015000/20200618020000&acc=00570/00018/00600/-0026/04040/00262/04060/-0178/04100/-0024/00570/00018/00600/-0026/04040/00262/04060/-0178/04100/-0024/00570/00018/00600/-0026/04040/00262/04060/-0178/04100/-0024&sn=00002";
		String response = sendToServer(q);

		System.out.println(response);
	}

	private String sendToServer(String... data) throws Exception {
		String url = "http://localhost:8080/mms/transfer/data";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//		AESUtils aesUtils =new AESUtils();
//		aesUtils.setKey("COUNTER!@#GLOBAL");
//		aesUtils.setIV("GLOBAL!@#COUNTER");
		for (int i = 0; i < data.length; i++) {
			String q = AESUtils.encrypt(data[i]);
			params.add("q", q);
		}


		RestTemplate restTemplate = new RestTemplateBuilder().build();
		return restTemplate.postForObject(url, params, String.class);
	}

	//@Test
	public void decryptQ() throws Exception {
		String encText = "YDQ+6++wkdtLjxTuV/CWu0l8YD0AKRlouKw75mWHBGKaj+rGmdMVMaJGR7Aa4MxP66nv1rtiCYdyoRRVxWFjbWGx/jDebhNri0B629FuYtHZMewmkPQSSyovRBNgWcID/gTEvd1tR7il7D4z6iSr13qwPRNqFmhVaEcspZqmmO4=";

		String plainText = AESUtils.decrypt(encText);
		String params = plainText;
		if (!plainText.startsWith("?")) {
			params = "?" + plainText;
		}

		// object mapping
		MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(params).build().getQueryParams();

		Map<String, Object> map = new HashMap<>();
		parameters.forEach((k, v)->{
			map.put(k, v.get(0)); // 멀티 밸류값 중 첫번째만.
			System.out.println(k + " : " + v.get(0));
		});
	}


	//@Test
	public void decryptQ2() throws Exception {
		String encText = "M5nlUJ73GXuntilAKDoq+hAT98FlFWxwHqCDqTfwv82NbOxAG3fCLORocCrZs8lwx00UGDxnnYWBRw2flmK2pmiVvxzyTJZAUH6EUiKK4oNl5g8wjJyrfbhtvrs0iMEqL9ayNBUjo5SxaklbuhBbqn21mz0t53rSNvCHhy+9P6VeVEDmUII4lgnryRqJ3J7eBeGQHpzRxm//uP0Wlzvsdac0Hb02IT//ZcAVzjmrkyQvhOIUKZXEtt/ZcBzkpjYRg6i20iQT96dA0AUs13krzG3SHOiAPjQJ7mYK9xwJQfBppLJqh4lZxMbX+PSNxMkG&q=M5nlUJ73GXuntilAKDoq+hAT98FlFWxwHqCDqTfwv81oIBlvZA+VWMrPq4eDYWeERdJq1+vQfTxvuSg6oAuPiDFfIwEXqCHKBMqcd+rPdJVcKf3AqPBGbAXRh0qhM3eMOmyF7xaohk/7pOAEntS+Zgw3Lq+SWD/UC+UMjNY1bx5fhjiNLxrx+UqC9BkZYK/vvllFJfL3I1+ks7+9+CCS7+MI0HBtVjU8hGl/ttepffwGrFnwAU5E+1xqs0KYXJTZ0BclsMYHTiP8tpt7x7Gq96KCB2jVKrjeiH11WPx/EPpH5PnylxX/3ouRMFrOvgk2&q=M5nlUJ73GXuntilAKDoq+hAT98FlFWxwHqCDqTfwv82t7UZ4EBu+kr1aHPxNCiSdAuPkb5afws8VRENvP1kP+sUundwEHwCby3IXs2NOnFOXo7BNTDcH9X0gS8tK31i8RcnenIBal39wixswxH0";
		encText = "JxszhzziUqnLbSJ3D1wJ1QYi8zKh2N81N3Jl66%2B0SF4=";
		encText = URLDecoder.decode(encText, "UTF-8");

		AESUtils aes = new AESUtils();
//		aes.setKey("COUNTER!@#GLOBAL");
//		aes.setIV("GLOBAL!@#COUNTER");

		String plainText = AESUtils.decrypt(encText);
		log.debug(plainText);
	}

	//@Test
	public void CDATA()  throws Exception {
		String ci = "SKC-03-counter";
		String ct = "0";
		String sc = "62";
		String sn = "00676";
		String cdata = "at=CDATA&ti=TMS1822VNA001&ci=" + ci + "&sc=" + sc + "&lst=" + DateUtils.getToday() + "&rt=" + DateUtils.getToday() + "&cf=M&ct=" + ct + "&bs=H&rc=0&tv=2.0.0&sn=" + sn;
		String q = AESUtils.encrypt(cdata);

		System.out.println("q=" + q);
		String url = "http://localhost:8080/mms/transfer/data";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("q", q);

		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String response = restTemplate.postForObject(url, params, String.class);

		System.out.println(response);
	}

	//@Test
	public void PRESET()  throws Exception {
		String ci = "SKC-02-counter";
		String sc = "0";
		String cdata = "at=PRESET&ti=TMS1822VNA001&ci=" + ci + "&sc=" + sc + "&tv=3.0.0";
		String q = AESUtils.encrypt(cdata);

		System.out.println("q=" + q);
		String url = "http://49.247.200.147/mms/transfer/data";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("q", q);

		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String response = restTemplate.postForObject(url, params, String.class);

		System.out.println(response);
	}



	//@Test
	public void cdata() throws Exception {
		//String
		String ci = "SKC-03-counter";
		String sc = "30";
		String cdata = "at=CDATA&ti=TMS1822VNA001&ci=" + ci + "&sc=" + sc + "&lst=" + DateUtils.getToday() + "&rt=" + DateUtils.getToday() + "&cf=M&ct=31&bs=H&rc=0&tv=2.0.0&sn=00672";
		String q = AESUtils.encrypt(cdata);
		System.out.println(q);
	}


	//@Test
	public void PRESET_REQUEST_1() throws Exception {
		// INSERT INTO PRESET (id, ci, preset) VALUE (1, 'CMS1744I04002', '37');
		// R2 SC_OK => PRESET 데이터 삭제..

		String R1_ciNo = "at=PRESET&ti=TMS1744KRB001&ci=CMSXXXXXXX001&tv=2.0.0";
		String R1_ciOk = "at=PRESET&ti=TMS1744KRB001&ci=CMS1744I04002&tv=2.0.0";

		String R2_SC_ER = "at=PRESET&ti=TMS1744KRB001&ci=CMS1744I04002&sc=1000&tv=2.0.0";
		String R2_SC_OK = "at=PRESET&ti=TMS1744KRB001&ci=CMS1744I04002&sc=37&tv=2.0.0";
		System.out.println("Request 1 =======================================");
		System.out.println("R1 CI_NO : " + AESUtils.encrypt(R1_ciNo));
		System.out.println("R1 CI_OK : " + AESUtils.encrypt(R1_ciOk));

		System.out.println();
		System.out.println("Request 2 =======================================");
		System.out.println("R2 SC_ER : " + AESUtils.encrypt(R2_SC_ER));
		System.out.println("R2 SC_OK : " + AESUtils.encrypt(R2_SC_OK));

		//

		System.out.println("FAIL : " + AESUtils.encrypt("AUTH_FAIL"));


		// 1.

		System.out.println("RESPONSE-R1 CI_NO : " + resultDecoder("OJvly5Brm%2BKdvH5vN0ppUg%3D%3D"));
		System.out.println("RESPONSE-R1 CI_OK : " + resultDecoder("h54vUdb03%2FGH0cCFbOwKIw%3D%3D"));

		System.out.println("RESPONSE-R2 SC_ER : " + resultDecoder("OJvly5Brm%2BKdvH5vN0ppUg%3D%3D"));
		System.out.println("RESPONSE-R3 SC_OK : " + resultDecoder("aDI0sJuQv7FA0dTb93BCNw%3D%3D"));

	}

	//@Test
	public void UPURL() throws Exception {
		String ti = "BTM1808KRB001";
		String data = "at=UPURL&ti=" + ti;
		String q = AESUtils.encrypt(data);

		log.debug("[UPURL] q={}", q);
		String url = "http://localhost:8080/mms/transfer/data";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("q", q);

		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String response = restTemplate.postForObject(url, params, String.class);

		Scanner scanner = new Scanner(response);
		int i = 0;
		String result = "";
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (i == 2) {
				result = line;
			}
			// process the line
			log.debug("[UPURL] READLINE: {}", line);
			i++;
		}
		scanner.close();


		log.debug("[UPURL] response: {}", result);
		log.debug("[UPURL] response: {}", resultDecoder(result));
	}

	private String resultDecoder(String enc) throws Exception {
		enc = enc.replaceAll("%2A", "\\*");
		return AESUtils.decrypt(URLDecoder.decode(enc, "UTF-8"));

	}
}
