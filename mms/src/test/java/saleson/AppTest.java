package saleson;

import com.emoldino.api.analysis.resource.composite.trscol.util.TrsColUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.junit.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import saleson.api.mold.MoldService;
import saleson.api.preset.PresetService;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.util.DateUtils;
import saleson.common.util.FileUtils;
import saleson.common.util.Md5Utils;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.data.ChartData;
import saleson.service.transfer.TransferService;
import saleson.service.transfer.VersionChecker;
import saleson.service.util.CryptoUtils;
import saleson.service.util.DecryptInfo;
import saleson.service.util.ParamParser;
import saleson.service.version.VersionService;
import unused.util.AESUtils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AppTest {

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

		String s = "5mTbw7x2Zy9vsW03gj7FkEUHztyc2UIQWjEY+i3Ahr88/VlLtsekvJCmDEOZqenuICUwEwAOpWPKH0AxtalC8Myx4hd064zDkwyGwweN8nKlFQTmop8TWAYlB/lVaWd68e7RM58svX/z0hEgbZd1CkuKdZDdyYeoObOaGvJNZ53lBaaTaQCjMzDVZOY6OMb6hv+nZG5lXCQ7QyDBuPFA8n2hRxYM3fMezOso9Oo3q9z6a3xYOL05ZEf6fqQeV7TsuzsqPtvT1bTaZ/eq9gS8tccDlhtsH9w5PADrPUvgQz7x5HEE6nEQyhftCQ+M63iP";
		String q[] = new String[1];
		q[0] = s;
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
				Thread t = new Thread(() -> {

					transferService.procCdata(dataList);
				});
				t.start();

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

	@Test
	void timezoneTest() {
		DateTimeFormatter fomatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

		Set<String> aa = ZoneId.getAvailableZoneIds();
		for (Object o : aa) {
			System.out.println(o);
		}
	}

	@Test
	public void math() {

		double a = 0.0;
		double b = 10.0;

		System.out.println(a == 0);
		System.out.println(b == 10);

		long uptime = 86000L;

		System.out.println((double) uptime / 3600);
		Double c = Math.round(((double) uptime / (60 * 60)) * 10) / 10.0;
		System.out.println(c);
	}

	@Test
	public void enumTest() {

		MisconfigureStatus status = MisconfigureStatus.CONFIRMED;
		MisconfigureStatus matchedStatus = Arrays.asList(MisconfigureStatus.values()).stream().filter(t -> t == status).findFirst().orElse(null);

		assertThat(matchedStatus).isEqualTo(MisconfigureStatus.CONFIRMED);
	}

	@Test
	public void date() {
		Instant instant = Instant.now();
		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Seoul"));
		LocalDateTime seoul = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
		LocalDateTime paris = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Paris"));
		LocalDateTime utc_1 = LocalDateTime.ofInstant(instant, ZoneId.of("UTC+02:00:00"));

		log.debug("seoul: {}", seoul);
		log.debug("Paris: {}", paris);
		log.debug("Minus: {}", seoul.minus(7, ChronoUnit.HOURS));
		log.debug("utc+1: {}", utc_1);

//		System.out.println(LocalDateTime.now());
//		System.out.println(instant);
//		//System.out.println(instant.minus(1, ChronoUnit.MONTHS));
//		System.out.println(zonedDateTime);
//		System.out.println(localDateTime);
//		System.out.println(localDateTimeUtc);

	}

	//@Test
	public void abc() {
		String date = "01/03/2019 9:20 PM";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a").withLocale(Locale.US).withZone(ZoneId.systemDefault());

		LocalDateTime lastShotTime = LocalDateTime.parse(date, formatter);
		Instant instant = lastShotTime.toInstant(ZoneOffset.UTC);
		System.out.println(lastShotTime);
		System.out.println(instant.toString());
	}

	@Test
	public void lombok() {

		double utilizationRate = ((double) 170178 / (double) 1000000) * 100;
		utilizationRate = Math.round(utilizationRate * 10) / 10;
		System.out.println(utilizationRate);
	}

	//@Test
	public void allDate() {

		String a = "20180515";
		String start1 = a.substring(0, 4);

		System.out.println("sss -> " + start1);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

		LocalDate start = DateUtils.getLocalDate("20181118");
		LocalDate end = DateUtils.getLocalDate("20181202").plusDays(1);
		List<ChartData> dates = Stream.iterate(start, date -> date.plusDays(1)).limit(ChronoUnit.DAYS.between(start, end)).map(l -> new ChartData(l.format(formatter), 0, 0))
				.collect(Collectors.toList());

		dates.stream().forEach(d -> System.out.println(d.getTitle() + " ==> " + d.getData()));
	}

	//@Test
	public void test() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

		LocalDateTime lastShotTime = LocalDateTime.parse("20180706123021", formatter);
		Instant instant = lastShotTime.toInstant(ZoneOffset.UTC);
		System.out.println(instant.toString());

		/*DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
				.withLocale(Locale.getDefault())
				.withZone(ZoneId.systemDefault());
		System.out.println(formatter.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())));*/
	}

	//@Test
	public void efficiency() {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

		// 오늘 날짜 기준으로 SUM(uptime_second) 구하기
		String startDate = DateUtils.getToday("yyyyMMdd") + "000000";

		LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
		LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());

		Long currentSeconds = SECONDS.between(startDateTime, endDateTime);
		Long uptimeSeconds = 53000L;

		double efficiency = ((double) uptimeSeconds / (double) currentSeconds) * 100; // (uptime / (24 * 60)) * 100

		System.out.println("uptimeSeconds: " + uptimeSeconds);
		System.out.println("currentSeconds: " + currentSeconds);
		System.out.println("efficiency: " + efficiency);

		// 3. Cycle Time 체크 (L1, L2)
		double baseEffciency = 80; // 기준 efficiency rate : 80%
		int limit1 = 3; // (%)
		int limit2 = 5; // (%)

		double efficiencyL1 = baseEffciency * limit1 * 0.01;
		double efficiencyL2 = baseEffciency * limit2 * 0.01;

		// 4. 기준 범위  ===== -L2 === -L1 === base === +L1 === +L2 =====
		double minusL2 = baseEffciency - efficiencyL2;
		double minusL1 = baseEffciency - efficiencyL1;
		double plusL1 = baseEffciency + efficiencyL1;
		double plusL2 = baseEffciency + efficiencyL2;

		EfficiencyStatus efficiencyStatus = EfficiencyStatus.WITHIN_TOLERANCE;
		if (efficiency <= minusL2 || plusL2 <= efficiency) {
			efficiencyStatus = EfficiencyStatus.OUTSIDE_L2;

		} else if ((minusL2 < efficiency && efficiency <= minusL1) || (plusL1 <= efficiency && efficiency < plusL2)) {
			efficiencyStatus = EfficiencyStatus.OUTSIDE_L1;
		}

		System.out.println("efficiency: " + efficiency);
		System.out.println("baseEffciency: " + baseEffciency);
		System.out.println("===== " + minusL2 + " === " + minusL1 + " === " + baseEffciency + " === " + plusL1 + " === " + plusL2 + " =====");
		System.out.println(efficiencyStatus);

	}

	//@Test
	public void cycleTime() {
		Mold mold = new Mold();
		mold.setContractedCycleTime(49);
		mold.setCycleTimeLimit1(3);
		mold.setCycleTimeLimit2(5);

		double cycleTime = 51.45;

		// 3. Cycle Time 체크 (L1, L2)
		double baseCycleTime = mold.getContractedCycleTime(); // 기준 사이클 타임 (contracted?)
		int limit1 = mold.getCycleTimeLimit1();
		int limit2 = mold.getCycleTimeLimit2();
		double cycleTimeL1 = baseCycleTime * limit1 * 0.01;
		double cycleTimeL2 = baseCycleTime * limit2 * 0.01;

		// 4. 기준 범위  ===== -L2 === -L1 === base === +L1 === +L2 =====
		double minusL2 = baseCycleTime - cycleTimeL2;
		double minusL1 = baseCycleTime - cycleTimeL1;
		double plusL1 = baseCycleTime + cycleTimeL1;
		double plusL2 = baseCycleTime + cycleTimeL2;

		CycleTimeStatus cycleTimeStatus = CycleTimeStatus.WITHIN_TOLERANCE;
		if (cycleTime <= minusL2 || plusL2 <= cycleTime) {
			cycleTimeStatus = CycleTimeStatus.OUTSIDE_L2;

		} else if ((minusL2 < cycleTime && cycleTime <= minusL1) || (plusL1 <= cycleTime && cycleTime < plusL2)) {
			cycleTimeStatus = CycleTimeStatus.OUTSIDE_L1;
		}

		System.out.println("cycleTime: " + cycleTime);
		System.out.println("baseCycleTime: " + baseCycleTime);
		System.out.println("===== " + minusL2 + " === " + minusL1 + " === " + baseCycleTime + " === " + plusL1 + " === " + plusL2 + " =====");
		System.out.println(cycleTimeStatus);

	}

	//@Test
	public void days() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

		double ct = 31.3;
		int sFsc = 100000;
		int sSc = 140275;
		int startShotCount = sSc - sFsc;
		//shotCount = 2;
		int totalShotCount = 0;

		String lastShotDate = "20181002000009";

		LocalDateTime endDateTime = LocalDateTime.parse(lastShotDate, formatter);

		int total = 0;
		boolean hasNext = true;
		do {
			long uptimeSeconds = (long) (startShotCount * (ct / 10));

			LocalDateTime startDate = endDateTime.minus(Duration.ofSeconds(uptimeSeconds));
			LocalDateTime endDate = endDateTime;

			int days = (int) DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
			int shotCount = startShotCount;

			if (days == 0) {
				hasNext = false;

			} else {
				String date = endDateTime.toLocalDate().toString().replaceAll("-", "");
				startDate = LocalDateTime.parse(date + "000000", formatter);
				uptimeSeconds = SECONDS.between(startDate, endDate) + 1;
				shotCount = (int) (((double) uptimeSeconds) / (ct / 10));

				endDateTime = startDate.minus(Duration.ofSeconds(1));

			}

			String fst = formatter.format(startDate);
			String lst = formatter.format(endDate);
			int fsc = startShotCount - shotCount;
			int sc = startShotCount;

			startShotCount = fsc;

			System.out.println("fst: " + fst);
			System.out.println("lst: " + lst);
			System.out.println("fsc: " + fsc);
			System.out.println("sc: " + sc);
			System.out.println("shotCount: " + shotCount);
			System.out.println("uptimeSeconds: " + uptimeSeconds);
			System.out.println("year: " + DateUtils.getYear(lst));
			System.out.println("month: " + DateUtils.getYearMonth(lst));
			System.out.println("week: " + DateUtils.getYearWeek(lst));
			System.out.println("day: " + DateUtils.getDay(lst));

			System.out.println();

			total += shotCount;

		} while (hasNext);

		System.out.println((sSc - sFsc) + " == " + total);
	}

	//@Test
	public void days2() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());
		String fst = "20181001120000";
		String lst = "20181002000009";

		LocalDateTime startDateTime = LocalDateTime.parse(fst, formatter);
		LocalDateTime endDateTime = LocalDateTime.parse(lst, formatter);

		LocalDate firstDate = startDateTime.toLocalDate();
		LocalDate lastDate = endDateTime.toLocalDate();

		//String fst = formatter.format(firstShortTime);

		int days = (int) DAYS.between(firstDate, lastDate);

		System.out.println(days);

		double ct = 31.0;
		int shotCount = 140275;
		int totalShotCount = 0;

		LocalDateTime endDate = endDateTime;
		for (int i = days; i >= 0; i--) {
			String date = endDate.toLocalDate().toString().replaceAll("-", "");
			System.out.println(i + " : " + date);

			LocalDateTime startDate = LocalDateTime.parse(date + "000000", formatter);

			if (i == 0) {
				startDate = startDateTime;
			}

			System.out.println(startDate);
			System.out.println(endDate);

			Long uptimeSeconds = SECONDS.between(startDate, endDate);
			if (i < days) {
				uptimeSeconds += 1;
			}

			System.out.println(uptimeSeconds);

			int sc = (int) (((double) uptimeSeconds) / (ct / 10));

			if (i == 0) {
				sc = shotCount - totalShotCount;
			} else {
				totalShotCount += sc;
			}
			System.out.println("sc : " + sc);

			endDate = startDate.minus(Duration.ofSeconds(1));
			System.out.println();
		}
	}

	//@Test
	public void instant() {

		Statistics previousStatistics = null;

		Cdata cdata = new Cdata();
		cdata.setId(17223L);
		//cdata.setPartId("PS-01");
		cdata.setMoldCode("PF-001");
		cdata.setTi("TMS1744KRB004");
		cdata.setCi("CMS1744I04004");
		cdata.setSc(140275);
		cdata.setLst("20181002011355");
		cdata.setRt("20181002161736");
		cdata.setCt(31.0);
		cdata.setBs("H");
		cdata.setRc(0);
		cdata.setTv("2.0.0");
		cdata.setSn(672);
		cdata.setCreatedAt(Instant.now());
		cdata.setDay("20181002");
		cdata.setMonth("201810");
		cdata.setWeek("201840");
		cdata.setYear("2018");

		Statistics statistics = new Statistics(cdata);

		// shot count
		int shotCount = cdata.getSc();
		if (previousStatistics == null) {
			statistics.setFsc(0);
			statistics.setShotCount(statistics.getSc());
		} else {
			statistics.setFsc(previousStatistics.getSc());
			statistics.setShotCount(cdata.getSc() - previousStatistics.getSc());
		}

		// 사이클 타임과 realShotCount로 시작 시간 계산
		long uptimeSeconds = (long) (statistics.getShotCount() * (statistics.getCt() * 0.1));

		// 작업시간이 0인경우 pass
		if (uptimeSeconds > 0) {
			statistics.setUptimeSeconds(uptimeSeconds);

			// 마지막 Shot 시간
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

			DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

			LocalDateTime lastShotTime = LocalDateTime.parse(statistics.getLst(), formatter);
			LocalDateTime firstShortTime = lastShotTime.minus(Duration.ofSeconds(uptimeSeconds));

			// 처음 shot 시간
			String fst = formatter.format(firstShortTime);

			statistics.setFst(fst);

			// 처음 시작 시간이 어제인 경우 데이터 나누기
			LocalDateTime startDate = LocalDateTime.parse(statistics.getFst(), formatter);
			LocalDateTime endDate = LocalDateTime.parse(statistics.getLst(), formatter);

			//String fst = formatter.format(firstShortTime);

			int days = (int) DAYS.between(startDate, endDate);

			System.out.println(days);
			if (endDate.equals(startDate)) {
				//statisticsRepository.save(statistics);
				System.out.println(statistics);
			} else {
				// 기준일
				String standardDate = endDate + "000000";
				LocalDateTime standardDateTime = LocalDateTime.parse(standardDate, formatter);

				// 기준일 부터 lastShotTime 까지 시간차이 (sec)
				Long uptimeSeconds2 = Duration.between(standardDateTime, lastShotTime).toMinutes() * 60;

				// firstDate의 uptimeSecond 구하기
				Long uptimeSeconds1 = statistics.getUptimeSeconds() - uptimeSeconds2;

				// shotCount 구하기 : uptimeSeconds1 / uptimeSeconds2 비율로 각각의 shotCount 구하기
				int shotCount2 = (int) (statistics.getShotCount() * ((double) uptimeSeconds2 / (double) statistics.getUptimeSeconds()));
				int shotCount1 = statistics.getShotCount() - shotCount2;

				// 2개의 통계 데이터 등록
				try {
					Statistics s1 = (Statistics) statistics.clone();
					s1.setShotCount(shotCount1);
					s1.setUptimeSeconds(uptimeSeconds1);
					s1.setLst(standardDate);

					Statistics s2 = (Statistics) statistics.clone();
					s2.setShotCount(shotCount2);
					s2.setUptimeSeconds(uptimeSeconds2);
					s2.setFst(standardDate);

					System.out.println(s1);
					System.out.println(s2);
					//statisticsRepository.save(s1);
					//statisticsRepository.save(s2);

				} catch (Exception e) {
					log.error("Statistics Clone Error : {}", e.getMessage());
				}
			}
		}

	}

	//@Test
	public void path() throws IOException {
		File file = new File("/Users/dbclose/onlinepowers/file_upload_location/mms/upload", "11.png");
		File file2 = new File("/Users/dbclose/onlinepowers/file_upload_location/mms/upload");

		System.out.println(DateUtils.getToday());
		if (file.exists()) {
			System.out.println("YES");
			System.out.println(file.getAbsolutePath());
			System.out.println(file.getCanonicalPath());
			System.out.println(file.getName());
			System.out.println("Di : " + file2.getName());

		}
		/*System.out.println(FileUtils.getName(file.getAbsolutePath()));
		System.out.println(FileUtils.getParent(file.getAbsolutePath()));*/

		System.out.println("----------------");
		String fileName = file.getAbsolutePath();
		//fileName = "D:\\winddows\\system32\\asd.gif";

		System.out.println(FileUtils.getFileNameWithoutExtension(fileName));
		System.out.println(FileUtils.getExtension(fileName));
		System.out.println(FileUtils.getName(fileName));
		System.out.println(FileUtils.getParent(fileName));
	}

	public void aes() throws Exception {
		String plainText = "at=TDATA&ti=TAP1300000001&rc=0&tv=1.8.3&dh=0&ip=203.228.249.6&gw=203.228.249.1&dn=164.124.101.2";
		String encText = AESUtils.encrypt(plainText);
		System.out.println("원문 : " + plainText);
		System.out.println("암호화 : " + encText);
		System.out.println("복호화 : " + AESUtils.decrypt(encText));
	}

	//@Test
	public void uri() {
		String url = "at=TDATA&ti=TAP1300000001&rc=0&tv=1.8.3&dh=0&ip=203.228.249.6&gw=203.228.249.1&dn=164.124.101.2";
		url = "";
		MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(url).build().getQueryParams();

		Map<String, Object> map = new HashMap<>();

		parameters.forEach((k, v) -> {
			map.put(k, v.get(0)); // 멀티 밸류값 중 첫번째만.
			System.out.println(k + " : " + v.get(0));
		});

		ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
		Transfer transfer = mapper.convertValue(map, Transfer.class);

		System.out.println(transfer);
	}

	//@Test
	public void paramParser() {
		String param = "YDQ+6++wkdtLjxTuV/CWu++0XROINPwXrR7HY0HXse+FD7LUX9Ge4yf6RYnIA1e0vtKM1dp0imnYCaB13X5H8M6dyDsBprtkgV8F/BHzvXqb51VBUGlZ6fNpKrGSJn/VObgDzRuooZTRGtsu3OoI5lqtmhaNiu9BQTUi1OsEoVo=";
		param = param + "&q=" + param;

		ArrayList al = null;
		Transfer dto = null;
		ParamParser pp = new ParamParser();
		try {

			pp.init(param);
			al = pp.getParam();
			Iterator it = al.iterator();
			if (it.hasNext()) {
				dto = (Transfer) it.next();
			}

			String at = pp.getAt();
			System.out.println(at);
			System.out.println(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//@Test
	public void gavata2() {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("1111"));

		String email = "dbclose@gmail.com";
		System.out.println("email ==> " + Md5Utils.md5Hex(email));
		Iterable<String> iterable = Arrays.asList("Testing", "Iterable", "conversion", "to", "Stream");

		String[] asdf = StreamSupport.stream(iterable.spliterator(), false).map(String::toUpperCase).toArray(String[]::new);

	}

	//@Test
	public void gavata() {
		String email = "dbclose@gmail.com";
		System.out.println("email ==> " + Md5Utils.md5Hex(email));

		//https://www.gravatar.com/avatar/b265511f17789cba93f886359a5a48d1
	}

	//@Test
	public void time() {
		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.UK).withZone(ZoneId.systemDefault());

		System.out.println(DATE_TIME_FORMATTER.format(new Date().toInstant()));
	}

	//@Test
	public void time2() {
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault()).withZone(ZoneOffset.UTC);

		System.out.println(formatter.format(new Date().toInstant()));
	}

	//@Test
	public void week() {
		String sDate = "20180103090641";
		LocalDate date = LocalDate.parse(sDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
		int weekNumber = date.get(woy);
		System.out.println("Week: " + weekNumber);

		if (sDate.length() >= 4) {
			System.out.println("year : " + sDate.substring(0, 4));
		}
		if (sDate.length() >= 6) {
			System.out.println("year : " + sDate.substring(0, 6));
		}
		if (sDate.length() >= 8) {
			System.out.println("year : " + sDate.substring(0, 8));
		}

	}

	@Test
	public void date2() {

		System.out.println((double) Math.round(4.6000005 * 10) / 10);
		System.out.println(Math.round(4.6000005 * 10) * 0.1);
		String dateTime = "20191234567890";

		String ok = new StringBuilder().append(dateTime.substring(0, 4)).append("-").append(dateTime.substring(4, 6)).append("-").append(dateTime.substring(6, 8)).append(" ")
				.append(dateTime.substring(8, 10)).append(":").append(dateTime.substring(10, 12)).append(":").append(dateTime.substring(12, 14))

				.toString();

		System.out.println(ok);

	}

	/*//@Test
	public void filter() {
		List<ChartData> chartDataList = new ArrayList<>();
		chartDataList.add(new ChartData("24234-AA-01", "201808", 127));
		chartDataList.add(new ChartData("24234-AA-01",	"201809",	160));
		chartDataList.add(new ChartData("MF-001", "201804", 536));
		chartDataList.add(new ChartData("MF-001", "201805", 51460));
		chartDataList.add(new ChartData("MF-001", "201806", 105113));
		chartDataList.add(new ChartData("MF-001", "201807", 161762));
		chartDataList.add(new ChartData("PF-001", "201805", 44886));
		chartDataList.add(new ChartData("PF-001", "201806", 93513));
		chartDataList.add(new ChartData("PF-001", "201807", 140223));
	
	
	
		List<ChartData> results = new ArrayList<>();
		int oldSc = 0;
		String oldCode = "";
		for (ChartData chartData : chartDataList) {
	
			int quintity = 0;
			if (!oldCode.equals(chartData.getMoldCode())) {
				oldCode = "";
				oldSc = 0;
			} else {
				quintity = chartData.getData() - oldSc;
			}
	
			System.out.println(chartData.getTitle() + " : " + quintity);
	
			oldSc = chartData.getData();
			oldCode = chartData.getMoldCode();
	
	
			if (quintity == 0) {
				continue;
			}
			boolean isMatched = false;
			for (ChartData data : results) {
	
				if (data.getTitle().equals(chartData.getTitle())) {
					data.setData(data.getData() + quintity);
					data.setMoldCount(data.getMoldCount() + 1);
					isMatched = true;
					break;
				}
			}
	
			if (!isMatched) {
				results.add(new ChartData(chartData.getTitle(), quintity, 1));
			}
		}
		System.out.println("=========================");
		results.forEach(c -> {
			System.out.println(c.getTitle() + " : " + c.getData() + " : " + c.getMoldCount());
		});
	}*/

	@Test
	public void mailTest() {
		String user = "noreply@emoldino.com"; // 네이버일 경우 네이버 계정, gmail경우 gmail 계정
		String password = "emoldino1234"; // 패스워드

		// SMTP 서버 정보를 설정한다.
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", 587);

		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.starttls.required", "true");

		Session session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});

		try {
			MimeMessage message = new MimeMessage(session);
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			/*message.setFrom(new InternetAddress(user));
			
			//수신자메일주소
			message.addRecipient(Message.RecipientType.TO, new InternetAddress("skc@onlinepowers.com"));
			
			// Subject
			message.setSubject("안녕하세요 "); //메일 제목을 입력
			
			// Text
			message.setText("내용을 입력하세요");    //메일 내용을 입력*/

			helper.setTo("skc@onlinepowers.com");
			helper.setSubject("asdf");
			helper.setText("asdfasd", true);
			helper.setFrom(user);

			// send the message
			Transport.send(message); ////전송
			System.out.println("message sent successfully...");
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void test2() throws MessagingException {

		String username = "noreply@emoldino.com";
		String password = "emoldino1234";
		String from = "noreply@emoldino.com";

		String recipient = "skc@onlinepowers.com";

		Properties props = new Properties();

		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.from", from);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "587");
		props.setProperty("mail.debug", "true");

		Session session = Session.getInstance(props, null);
		MimeMessage msg = new MimeMessage(session);

		msg.setRecipients(Message.RecipientType.TO, recipient);
		msg.setSubject("JavaMail hello world example");
		msg.setSentDate(new Date());
		msg.setText("Hello, world!\n");

		Transport transport = session.getTransport("smtp");

		transport.connect(username, password);
		transport.sendMessage(msg, msg.getAllRecipients());
		transport.close();
	}
}
