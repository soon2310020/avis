package saleson;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.emoldino.api.analysis.resource.composite.trscol.util.TrsColUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import saleson.api.mold.MoldService;
import saleson.api.preset.PresetService;
import saleson.common.util.StringUtils;
import saleson.model.Preset;
import saleson.model.Transfer;
import saleson.service.transfer.TransferService;
import saleson.service.util.CryptoUtils;
import saleson.service.util.DecryptInfo;
import saleson.service.version.VersionService;

@Slf4j
@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AppTest3 {

	public static final String DELIMITER = "/";

	@Lazy
	@Autowired
	TransferService transferService;

	@Autowired
	PresetService persetService;

	@Autowired
	VersionService versionService;

	@Lazy
	@Autowired
	MoldService moldService;

	/**
	 * R2 버전에서 일부 데이터가 / 구분자로 묶여서 넘어옴. (분기 로직 처리)
	 * @param map
	 * @param k
	 * @param v
	 */
	private void bindMapData(Map<String, Object> map, String k, String v) {

		if (!hasDelimiter(v)) {
			map.put(k, v); // 멀티 밸류값 중 첫번째만.

		} else {
			// * 값은 데이터가 없거나 0인 경우임
			v = v.replaceAll("\\*", "");
			String[] values = StringUtils.delimitedListToStringArray(v, DELIMITER);

			if ("id".equals(k) && values.length == 2) {
				// [TDATA] id = ti / tv    ;    [CDATA] id = ti / ci
				map.put("ti", values[0]);
				map.put("idSecondValue", values[1]);

			} else if ("net".equals(k) && values.length == 6) { // [TDATA] net = nt / dh / ip / gw / dn / dn2
				map.put("nt", values[0]);
				map.put("dh", values[1]);
				map.put("ip", values[2]);
				map.put("gw", values[3]);
				map.put("dn", values[4]);
				map.put("dn2", values[5]);

			} else if ("time".equals(k) && values.length == 2) { // [CDATA] time = lst / rt
				map.put("lst", values[0]);
				map.put("rt", values[1]);

			} else if ("ci".equals(k) && values.length == 3) { // [CDATA] ci = cf / ct / bs
				map.put("cf", values[0]);
				map.put("ct", values[1]);
				map.put("bs", values[2]);

			} else if ("ctt".equals(k)) { // [CDATA] ctt 는 그대로 저장
				map.put("ctt", v);

			} else if ("rmi".equals(k) && values.length == 5) { // [CDATA] rmi = rcn / rtr / rtf / rtl / rat
				map.put("rcn", values[0]);
				map.put("rtr", values[1]);
				map.put("rtf", values[2]);
				map.put("rtl", values[3]);
				map.put("rat", values[4]);

			} else if ("temp".equals(k) && values.length == 7) { // [CDATA] temp = 10분단위 온도1 / 온도2 / 온도3 / 온도4 / 온도5 / 온도1 시간 / 현재온도(tnw) / rtr / rtf / rtl / rat
				map.put("temp", v);

				// 최저온도 / 최고온도
				String date = values[5];
				String current = values[6];

				Long tempCount = Arrays.stream(values).filter(s -> !date.equals(s) && !"".equals(s)).count();

				Integer tlo = null;
				Integer thi = null;
				Integer tav = null;

				if (tempCount > 0) {
					tlo = Arrays.stream(values).filter(s -> !date.equals(s) && !"".equals(s)).map(s -> Integer.parseInt(s)).min(Comparator.comparing(Integer::valueOf)).get();

					thi = Arrays.stream(values).filter(s -> !date.equals(s) && !"".equals(s)).map(s -> Integer.parseInt(s)).max(Comparator.comparing(Integer::valueOf)).get();

					// 평균 온도
					OptionalDouble optionalDouble = IntStream.range(0, values.length).filter(i -> !(i == 5 || values[i].isEmpty() || "*".equals(values[i])))
							.map(i -> Integer.parseInt(values[i])).average();

					if (optionalDouble.isPresent()) {
						tav = (int) Math.round(optionalDouble.getAsDouble());
					}
				}

				map.put("tlo", tlo);
				map.put("thi", thi);
				map.put("tav", tav);
				map.put("tff", date);
				map.put("tnw", "".equals(current) ? null : current);

			} else if ("ei".equals(k) && values.length == 2) { // [CDATA] ei = ac / pw(내가 추가함)
				map.put("ac", values[0]);
				map.put("epw", values[1]);

			} else if ("acc".equals(k)) { // [CDATA] acc는 그대로 저장
				map.put("acc", v);
			}
		}

	}

	/**
	 * 값에 DELIMITER를 포함하는가?
	 * @param v
	 * @return
	 */
	private boolean hasDelimiter(String v) {
		return v != null && v.indexOf(DELIMITER) > -1 ? true : false;
	}

	private String result(String message) {

		if (StringUtils.isEmpty(message)) {
			message = "ERROR_A";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("[STX]").append(System.lineSeparator());
		sb.append(message.length()).append(System.lineSeparator());
		sb.append(message).append(System.lineSeparator());
		sb.append("[ETX]");
		return sb.toString();
	}

	private String resultEncoder(String message, DecryptInfo decryptInfo) {
		String result = "ERROR_B";
		try {
			CryptoUtils cryptoUtils = new CryptoUtils();

			result = cryptoUtils.encrypt(message, decryptInfo.getKey(), decryptInfo.getIv());
			result = URLEncoder.encode(result, "UTF-8");
			result = result.replaceAll("\\*", "%2A");
		} catch (Exception e) {
			log.warn("[TRANSFER] Response encrypt error ({}, {}, {})", message, decryptInfo.getKey(), decryptInfo.getIv(), e);
		}
		return result(result);
	}

	@Test
	public void newdataTest() {
		String res = reg();
		Assert.assertTrue(res.compareTo("ERROR_A") != 0);
	}

	public String reg() {
//at=TDATA&ti=TAP1200000001&rc=0&tv=1.8.3&dh=0&ip=192.168.123.183&gw=192.168.123.1&dn=192.168.123.2

		String q[] = new String[4];

		q[0] = "5mTbw7x2Zy9vsW03gj7FkI9KW/n5+CGZ+Ta6DVj/5NbPE1wR7eaeJyIhJl9nItQqyJ2P2zOaqeT3aURl5399/TL064zpX4MNPKypt+QUdMQPEUcAT/szUDrIL+vdolwzAXiKUunuKX+CddQX8Z5A6LQTG2P+792x3vU3QhZ/c12cybUFDf/mGwhajtBsD7U0hdcrZ0zRKWtsHLWyPjGNJDiSJdf4jYwiXwzyG3SZ1UB+/OjWDOHXceOtAlBq9zlxqCqCWtUsiIuUGOfpnLBARfrcvSjg2f3Y0jj3Uoy3AuQ=";
		q[1] = "5mTbw7x2Zy9vsW03gj7FkOPLHdnWH8wVpIHcEPNbOaMgTWSYot67zyEmNmwQhxk8IxnidBsQIxYxUwiwZvilDFJTxOoR5E6VAmL5jWTVejM7lzyBof8RW8RAWGDeJM+LNZ4wJ7YvCxdidEj5fW2FGMOVRurqyRUD7ggVxKDB1DQd/nMJwVMRS+x1jDQPDLy4YgijvjekdyobPIgFLhYr9lX7n5XfaruI44HnltfacagqeH0hc2SK7MRO8wwlBvPo8xSHHAHIi7qAVyNF53jrolatp5uGQAQN3APb1qXDxv0=";
		q[2] = "5mTbw7x2Zy9vsW03gj7FkMn6G7ZlVxlnkoqsm9+N+3+Wk6vMm05YjfKglFzd4usddZO9MeBpiuX+9G8Xv3GM2F7TWL7kJ9SU5wxEkvekT372x5wTjF7wsW58bGxqDdROiXcM2eTBuKQJ6rRmStarr/d7IpRw2sBleT9oV/pV10FLHjqjvRboBNm66LrmsuvU/ys4ewlzrxmebixz+l031JwmqwbLdGg0sMNNSd9jl3FMzx0mf/Q7qOFkWNxsRkLbt+GzBjKBM7WO791tJuvDWpbFO4Ta+/CBt/Az0Tfy9II=";
		q[3] = "5mTbw7x2Zy9vsW03gj7FkDFX4mAd2T1MaceeFtcDy5zxiOu0s6BhxArR15jTJkr82/3400MUKO1QorQHO4ad3KYPDMKrxRObDAbirRDWd2eaBqc8yVgI8/DCdqMTuqJk8rR0KfDDL0oo4cE+g1awt8g5nLLHlMbYPJG2I40fFIJ2Q4NDa97AvXumOzA1/S4fECVGniGx3UD998wkUSOisxc/bo6U7zZhk6hCXKZaJj8GsNbeEaiOBFl39z1+94aQ/mxAgmoXIXjJ28tiojnIaPaqfTgsTwYkZA7x0aVdX9I=";

		if (q == null || q.length == 0) {
			log.debug("[TRANSFER] ERROR_A : q is null or empty ");
			return result("ERROR_A");
		}
		List<String> list = Arrays.asList(q);
		list.forEach(n -> log.info("[TRANSFER] q = {}", n));

		Instant createdAt = Instant.now(); // 데이터 수신 시간

		List<Transfer> dataList = new ArrayList<>();
		try {

			String at = "";
			DecryptInfo decryptInfo = null;
			for (String encText : q) {
				if (StringUtils.isEmpty(encText)) {
					continue;
				}

				CryptoUtils cryptoUtils = new CryptoUtils();

				decryptInfo = cryptoUtils.decrypt(encText);
				log.info("[TRANSFER] decryptInfo : {}", decryptInfo);

				String plainText = decryptInfo.getDecryptText();
				String params = plainText;
				if (!plainText.startsWith("?")) {
					params = "?" + plainText;
				}

				// object mapping
				MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(params).build().getQueryParams();

				log.info("[TRANSFER] parameters : {}", parameters);

				Map<String, Object> map = new HashMap<>();
				parameters.forEach((k, v) -> {
					// R2 버전에서 일부 데이터가 / 구분자로 묶여서 넘어옴. (분기 로직 처리)
					bindMapData(map, k, v.get(0)); // 멀티 밸류값 중 첫번째만.
				});

				log.info("[TRANSFER] Map Data : {}", map);

				// counterId 값이 있으면 버전 R2 > [CDATA] ci 값으로 할당
				if (map.get("idSecondValue") != null) {
					if ("TDATA".equals(map.get("at"))) {
						map.put("tv", map.get("idSecondValue"));

					} else if ("CDATA".equals(map.get("at"))) {
						map.put("ci", map.get("idSecondValue"));

						// parsing ctt to ulct and llct
						TrsColUtils.parseCtt(map);

					} else if ("ADATA".equals(map.get("at"))) {
						map.put("ci", map.get("idSecondValue"));
					}
					map.remove("idSecondValue");
				}

				ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
				Transfer transfer = mapper.convertValue(map, Transfer.class);
				transfer.setEs(encText);
				transfer.setDs(plainText);
				transfer.setCreatedAt(createdAt);

				dataList.add(transfer);

				at = transfer.getAt();
			}

			// 1. 정상적인 요청 로그를 기록
			transferService.saveLog(dataList);

			// 2. 요청 구분 별 처리
			if ("TDATA".equals(at)) {
				transferService.procTdata(dataList);
				return result("OK");

			} else if ("CDATA".equals(at)) {
//                Thread t = new Thread(() -> {

				transferService.procCdata(dataList);
//                });
//                t.start();

				return result(String.valueOf(dataList.size()));

			} else if ("ADATA".equals(at)) {

				transferService.procAdata(dataList);
				return result(String.valueOf(dataList.size()));

			} else if ("PRESET".equals(at)) {
				Transfer transfer = dataList.get(0);
				Integer shotCount = transfer.getSc(); // PRESET을 등록한 카운터의 최종 Shot 수

				transferService.save(transfer);

				Optional<Preset> optionalPreset;
				// 1. 첫번째 전송 형식 처리
				if (shotCount == null) {
					optionalPreset = persetService.findRecentReady(transfer.getCi());
				}
				// 2. 두번째 전송 형식 처리 (sc 검증)
				else {
					optionalPreset = persetService.verifyAndApply(transfer.getCi(), transfer.getSc());
				}

				// Response
				if (optionalPreset.isPresent()) {
					return resultEncoder(optionalPreset.get().responseToTerminal(transfer), decryptInfo);
				} else {
					return resultEncoder(Preset.failureResponse(transfer), decryptInfo);
				}

			} else if ("UPURL".equals(at)) {
				Transfer transfer = dataList.get(0);
				String upurl = versionService.getUpurlBy(transfer);

				return resultEncoder(upurl, decryptInfo);
			}

			log.debug("[TRANSFER] ERROR_A : at is wrong!");
			return result("ERROR_A");

		} catch (Exception e) {
			e.printStackTrace();
			log.debug("[TRANSFER] ERROR_A : Exception occurred ({})", e.getMessage());
			return result("ERROR_A");

		}
	}
}
