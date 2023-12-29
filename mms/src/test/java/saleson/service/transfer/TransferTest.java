package saleson.service.transfer;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import unused.util.AESUtils;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TransferTest {
	private static final String HOST = "http://localhost:8080";		// http://49.247.200.147

	public TransferTest() {
	}

	public static void main(String[] args) throws Exception {

		TransferTest transfer = new TransferTest();

		log.info("# Send TDATA ----------------------------------------------");
		Tdata tdata = new Tdata("T20000");        // Terminal ID
		//transfer.send(tdata);
		//transfer.sendR2(tdata);


		log.info("# Send CDATA ----------------------------------------------");
		Cdata cdata = Cdata.builder()
				.ti("T1000")                        // Terminal ID
				.ci("Testing123")                        // Counter ID
				.sc("180")                            // Shot Count
				.ct("0")                            // Cycle Time (100ms)
				.sn("00030")                        // Sequence Number
				.lst("20200715080640")                // Last Shot Time
				.ctt("350/1/371/1/419/0/749/5/750/5/756/1/*/*/*/*/*/*/*/*")
				.temp("213/217/211/221/242/20200424152040/271")
				.build();

		//transfer.send(cdata);
		//transfer.sendR2(cdata);

		log.info("# Send CDATA ----------------------------------------------");
		Cdata cdata2 = Cdata.builder()
				.ti("T1000")                        // Terminal ID
				.ci("CMSTesting124")                        // Counter ID
				.sc("240")                            // Shot Count
				.ct("0")                            // Cycle Time (100ms)
				.sn("00035")                        // Sequence Number
				.lst("20200715084240")                // Last Shot Time
				.ctt("350/1/371/1/419/0/749/5/750/5/756/1/*/*/*/*/*/*/*/*")
				.temp("213/217/211/221/242/20200424152040/271")
				.build();

//		transfer.send(cdata2);
		testC2();
	}
	public static void testC2() throws Exception {
		log.info("# C2 Send CDATA ----------------------------------------------");
		Cdata cdata2 = Cdata.builder()
				.ti("T1000")                        // Terminal ID
				.ci("CMS001")                        // Counter ID
				.sc("240")                            // Shot Count
				.ct("0")                            // Cycle Time (100ms)
				.sn("10035")                        // Sequence Number
				.lst("20200715084240")                // Last Shot Time
				.ctt("350/1/371/1/419/0/749/5/750/5/756/1/*/*/*/*/*/*/*/*")
				.temp("213/217/211/221/242/20200424152040/271")
				.build();
		Cdata cdata3 = Cdata.builder()
				.ti("T1000")                        // Terminal ID
				.ci("CMS001")                        // Counter ID
				.sc("340")                            // Shot Count
				.ct("0")                            // Cycle Time (100ms)
				.sn("10036")                        // Sequence Number
				.lst("20200715085240")                // Last Shot Time
				.ctt("350/1/371/1/419/0/749/5/750/5/756/1/*/*/*/*/*/*/*/*")
				.temp("213/217/211/221/242/20200424154040/271")
				.build();
		TransferTest transfer = new TransferTest();
		transfer.send(cdata3);

	}

	private String send(Object data) throws Exception {
		String q = AESUtils.encrypt(data.toString());
		return send(q);
	}
	private String sendR2(Tdata data) throws Exception {
		String q = AESUtils.encrypt(data.getR2Data());
		return send(q);
	}
	private String sendR2(Cdata data) throws Exception {
		String q = AESUtils.encrypt(data.getR2Data());
		return send(q);
	}

	private String send(String encryptedData) throws Exception {
		String q = encryptedData;
		String url = HOST + "/mms/transfer/data";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("q", q);

		RestTemplate restTemplate = new RestTemplateBuilder().build();
		String response = restTemplate.postForObject(url, params, String.class);

		System.out.println(response);
		return response;
	}


	@Getter
	@Setter
	static class Tdata {
		private String ti;

		public Tdata(String ti) {
			this.ti = ti;
		}

		public String getR2Data() {
			return "at=TDATA&id=" + ti + "/3.0.0" +
					"&net=WIFI/Y/192.168.123.10/192.168.123.254/192.168.123.1/192.168.123.2";
		}

		@Override
		public String toString() {
			return "at=TDATA&ti=" + ti +
					"&tv=2.0.0&dh=1&ip=192.168.123.10&gw=192.168.123.254&dn=192.168.123.1";
		}
	}


	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	static class Cdata {
		private String ti;
		private String ci;
		private String sc;
		private String lst;
		private String ct;
		private String sn;
		private String ctt;
		private String temp;

		public String getR2Data() {
			return "at=CDATA&id=" + ti + "/" + ci +
					"&sc=" + sc +
					"&time=" + lst + "/" + lst +
					"&ci=N/" + ct + "/H" +
					"&ctt=" + ctt +
					"&rmi=3/600/20200213120000/20200213180000/Y" +
					"&temp=" + temp +
					"&ei=5/Y" +
					"&sn=" + sn;
		}
		@Override
		public String toString() {
			return "at=CDATA&ti=" + ti +
					"&ci=" + ci +
					"&sc=" + sc +
					"&lst=" + lst +
					"&rt=" + lst +
					"&cf=N&ct=" + ct +
					"&bs=H&rc=0&tv=2.0.0&sn=" + sn;
		}
	}
}


