package com.emoldino.api.analysis.resource.composite.datcol.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommand;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.QDeviceCommand;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.util.MoldDataUtils;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostItem;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostOut;
import com.emoldino.api.analysis.resource.composite.trscol.util.TrsColUtils;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ServerUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.common.enumeration.PresetStatus;
import saleson.model.Cdata;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.Preset;
import saleson.model.QPreset;
import saleson.model.Statistics;
import saleson.model.Transfer;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatColUtils {

	public static Map<String, String> separates(String rawdata) {
		// Parse CDATA/.../ADATA/.../TDATA/.../TEST/...
		Map<String, String> map = new LinkedHashMap<>();
		map.put("TEST", "");
		map.put("TDATA", "");
		map.put("ADATA", "");
		map.put("CDATA", "");
		map.put("HDATA", "");

		for (Map.Entry<String, String> e : map.entrySet()) {
			String key = e.getKey();
			if (rawdata.indexOf(key) > -1) {
				int beginIndex = rawdata.indexOf(key);
				String parsed = rawdata.substring(beginIndex);
				rawdata = rawdata.substring(0, beginIndex == 0 ? 0 : beginIndex - 1);
				map.put(e.getKey(), parsed);
			}
		}

		return map;
	}

	private static final List<String> TYPES = Arrays.asList("HDATA", "CDATA", "ADATA", "TDATA", "TEST");

	public static String toRawdata(Map<String, String> map) {
		if (ObjectUtils.isEmpty(map)) {
			return "";
		}

		StringBuilder buf = new StringBuilder();
		TYPES.forEach(type -> {
			String value = map.get(type);
			if (ObjectUtils.isEmpty(value)) {
				return;
			}
			buf.append(buf.length() == 0 ? "" : "/").append(value);
		});

		return buf.toString();
	}

	private static long FINAL_REQ_TIME;
	private static long FINAL_REQ_SEQ;

	public static synchronized String newRequestId() {
		long time = DateUtils2.newInstant().toEpochMilli();
		if (FINAL_REQ_TIME != time) {
			FINAL_REQ_TIME = time;
			FINAL_REQ_SEQ = 0;
		}
		String requestId = ValueUtils.abbreviate(time + "-" + FINAL_REQ_SEQ++ + "-" + ServerUtils.getName(), 50);
		return requestId;
	}

	public static void forward(Data3Collected item, String targetUrl) {
		LogicUtils.assertNotNull(item, "item");
		LogicUtils.assertNotEmpty(targetUrl, "targetUrl");

		String url = targetUrl + "/api/analysis/dat-col";
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Em-Forward", "true");
		HttpUtils.call(HttpMethod.POST, url, //
				null, null, //
				DatColPostIn.builder()//
						.requestId(ObjectUtils.isEmpty(item.getRequestId()) ? DatColUtils.newRequestId() : item.getRequestId())//
						.brokerId(item.getBrokerId())//
						.brokerType(item.getBrokerType())//
						.brokerSwVersion(item.getBrokerSwVersion())//
						.brokerTime(DateUtils2.format(item.getSentAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT))//
						.content(Arrays.asList(DatColPostItem.builder()//
								.deviceId(item.getDeviceId())//
								.deviceType(item.getDeviceType())//
								.deviceSwVersion(item.getDeviceSwVersion())//
								.deviceTime(DateUtils2.format(item.getOccurredAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT))//
								.dataType(item.getDataType())//
								.data(item.getData())//
//								private int lastCommandIndexNo;
								.build()))//
						.build(), //
				headers, //
				DatColPostOut.class, null, 0);
	}

	public static Integer getTotalShotCount(String data) {
		if (ObjectUtils.isEmpty(data)) {
			return 0;
		}

		String[] datas = data.split("/");

		if (datas.length < 5) {
			return 0;
		}

		return Integer.valueOf(datas[4]);
	}

	public static String modifyRawDataAtIndex(String rawData, int index, String newData) {
		if (!StringUtils.hasText(rawData)) {
			return null;
		}

		String[] datas = rawData.split("/");

		if (index >= 0 && index < datas.length) {
			if (index == 4) {
				newData = String.format("%7s", newData).replace(" ", "0");
			}
			datas[index] = newData;
			rawData = String.join("/", datas);
		}

		return rawData;
	}

	public static Pair<String, Integer> getMaxTffAndMaxShotCount(String counterCode, String moldCode) {
		String maxTff = BeanUtils.get(JPAQueryFactory.class) //
				.select(Q.statistics.tff.max()) //
				.from(Q.statistics) //
				.where(Q.statistics.ci.eq(counterCode) //
						.and(Q.statistics.moldCode.eq(moldCode))) //
				.fetchFirst();

		if (ObjectUtils.isEmpty(maxTff)) {
			return Pair.of(null, 0);
		}

		int maxShotCount = BeanUtils.get(JPAQueryFactory.class) //
				.select(Q.statistics.sc.max()) //
				.from(Q.statistics) //
				.where(Q.statistics.ci.eq(counterCode) //
						.and(Q.statistics.moldCode.eq(moldCode)) //
						.and(Q.statistics.tff.eq(maxTff))) //		
				.fetchFirst();

		return Pair.of(maxTff, maxShotCount);
	}

	/* Reset Common Method*/
	// Get Last Reset Command for 3rd Gen Sensor
	public static DeviceCommand getLastResetCommand(String deviceId) {
		QDeviceCommand table = QDeviceCommand.deviceCommand;
		return BeanUtils.get(JPAQueryFactory.class) //
				.selectFrom(table) //
				.where(table.deviceId.eq(deviceId) //
						.and(table.command.eq("1")) //
						.and(table.status.eq("RELEASED"))) //
				.orderBy(table.updatedAt.desc(), table.id.desc())//
				.fetchFirst();
	}

	// Get Last Reset Command for 2nd Gen Sensor
	public static Preset getLastPresetCommand(String deviceId) {
		QPreset table = QPreset.preset1;
		return BeanUtils.get(JPAQueryFactory.class) //
				.selectFrom(table) //
				.where(table.ci.eq(deviceId)//
						.and(table.presetStatus.eq(PresetStatus.APPLIED))) //
				.orderBy(table.updatedAt.desc(), table.id.desc()) //
				.fetchFirst();
	}

	// Get Total ShotCount for 3rd Gen Sensor
	public static Integer calTotalShotCount(String deviceId, DeviceCommand resetCommand) {
		int sumShotCount = 0;

		if (ObjectUtils.isEmpty(resetCommand)) {
			sumShotCount = BeanUtils.get(JPAQueryFactory.class) //
					.select(Q.statistics.shotCount.sum()) //
					.from(Q.statistics) //
					.where(Q.statistics.ci.eq(deviceId))//							
					.fetchFirst();
		} else {
			String zoneId = LocationUtils.getZoneIdByCounterCode(deviceId);
			String resetTime = DateUtils2.format(resetCommand.getUpdatedAt(), DatePattern.yyyyMMddHHmmss, zoneId);

			sumShotCount = BeanUtils.get(JPAQueryFactory.class) //
					.select(Q.statistics.shotCount.sum()) //
					.from(Q.statistics) //
					.where(Q.statistics.ci.eq(deviceId)//
							.and(Q.statistics.tff.gt(resetTime))) //
					.fetchFirst();
		}

		return sumShotCount;
	}

	public static boolean isValidCdata(String rawData, Long data3Id) {
		final String digitPattern = "^[0-9]+$";
		final String digitCharPattern = "^[a-zA-Z0-9]+$";
		String[] data = StringUtils.delimitedListToStringArray(rawData, "/");

		// 1. Validation to determine if the data array length is less than 10
		int length = data.length;
		if (data == null || length < 10) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] Data is short. Data Length = " + length, "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		// 1. Validation to determine if the data consists of only numbers
		if (!data[1].matches(digitPattern) || !data[2].matches(digitPattern) || !data[3].matches(digitPattern) || //
				!data[4].matches(digitPattern) || !data[6].matches(digitPattern) || //
				!data[7].matches(digitPattern) || !data[8].matches(digitPattern)) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] Data does not consist of numbers only", "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		// 2. Validation to determine if the data is numeric or character		
		if (!data[5].matches(digitCharPattern) || !data[9].matches(digitCharPattern)) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] Special characters exist in the data.", "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		// 3. Validation to determine if the length of time data is 14
		if (data[1].length() != 14 || data[2].length() != 14) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] The length of the data(time) is short", "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		// 4. Validation to determine if the length of Total Shot Count data is over 8
		if (data[3].length() > 8) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] The length of the data(Total ShotCount) is too long", "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		// 5. Validation to determine if the length of Battery status, Counter Status is 1
		if (data[4].length() != 1 || data[5].length() != 1) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] The length of the data(Status) does not 1", "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		// 6. Validation to determine if the data is a multiple of 4 in length
		if (data[6].length() % 4 != 0 || data[8].length() % 4 != 0) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "RAW_DATA_VALIDATION", HttpStatus.BAD_REQUEST, //
					"[Data Validation] Data length is not a multiple of 4", "Data3Collected Id = " + data3Id + ", RawData(Cdata) = " + rawData);
			return false;
		}

		return true;
	}

	public static List<Transfer> toTransfers(List<DataCounter> dataCounters) {
		List<Transfer> result = new ArrayList<>();
		dataCounters.forEach(data -> {
			String zoneId = LocationUtils.getZoneIdByTerminalCode(data.getTerminalId());

			Map<String, List<CycleTime>> shotsByShotEndTimeMap = new LinkedHashMap<>();
			Map<String, String> tempsByShotEndTimeMap = new LinkedHashMap<>();
			MoldDataUtils.populateByTime(data, shotsByShotEndTimeMap, tempsByShotEndTimeMap, zoneId);

			shotsByShotEndTimeMap.forEach((shotEndTime, cycleTimes) -> {
				String tempStr = tempsByShotEndTimeMap.get(shotEndTime);
				Transfer transfer = DatColUtils.toTransfer(data, shotEndTime, cycleTimes, tempStr, zoneId);
				result.add(transfer);
			});
		});
		return result;
	}

	public static Transfer toTransfer(DataCounter data, String shotEndTime, List<CycleTime> cycleTimes, String tempStr, String zoneId) {
		Instant recvTime = DateUtils2.toInstant(data.getReadTime(), DatePattern.yyyy_MM_dd_HH_mm_ss_SSS, Zone.GMT);
		String recvTimeStr = DateUtils2.format(recvTime, DatePattern.yyyyMMddHHmmss, zoneId);

		Transfer transfer = new Transfer();
		transfer.setAt("CDATA");
		transfer.setTi(data.getTerminalId());
		transfer.setCi(data.getCounterId());
		transfer.setSc(data.getShotCount());
		transfer.setRt(recvTimeStr);
		transfer.setCf("N");
		transfer.setSn(data.getDataId().intValue());
		transfer.setBs(isBatteryStatusOk(data.getStatus()) ? "H" : "L");
		transfer.setTff(shotEndTime);

		setCtValues(transfer, cycleTimes, shotEndTime, zoneId);
		setTempValues(transfer, tempStr);

		return transfer;
	}

	public static void setCtValues(Transfer transfer, List<CycleTime> cycleTimes, String shotEndTime, String zoneId) { //
		if (ObjectUtils.isEmpty(cycleTimes)) {
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(transfer.getCi()).orElse(null);
			String lst = counter == null || counter.getLastShotAt() == null ? transfer.getTff() : DateUtils2.format(counter.getLastShotAt(), DatePattern.yyyyMMddHHmmss, zoneId);
			transfer.setLst(lst);
			transfer.setCt(0.0);
			transfer.setCtt("/");
		} else {
			if (StringUtils.isEmpty(transfer.getTff())) {
				transfer.setTff(shotEndTime);
			}

			transfer.setLst(shotEndTime);
			Map<Integer, Integer> ctt = new HashMap<>();
			cycleTimes.forEach(cycleTime -> {
				int key = cycleTime.getCycleTime().setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(10)).intValue();
				if (ctt.containsKey(key)) {
					return;
				}
				Long shots = cycleTimes.stream()//
						.filter(x -> x.getId().equals(cycleTime.getId()))//
						.count();
				ctt.put(cycleTime.getCycleTime().setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(10)).intValue(), shots.intValue());
			});
			// LinkedHashMap preserve the ordering of elements in which they are inserted
			LinkedHashMap<Integer, Integer> reverseSortedMap = new LinkedHashMap<>();

			// Use Comparator.reverseOrder() for reverse ordering
			ctt.entrySet().stream()//
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))//
					.forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
			transfer.setCt(Double.valueOf(reverseSortedMap.keySet().stream().findFirst().get()));
			final String[] cttString = { "" };
			reverseSortedMap.forEach((ct, sc) -> {
				cttString[0] += ct + "/" + sc + "/";
			});
			cttString[0] = cttString[0].substring(0, cttString[0].length() - 1);
			transfer.setCtt(cttString[0]);
		}

		Map<String, Object> map = new HashMap<>();
		try {
			map.put("ctt", transfer.getCtt());
			map.put("ct", ValueUtils.toInteger(transfer.getCt(), 0).toString());
			TrsColUtils.parseCtt(map);
			if (map.containsKey("ulct")) {
				transfer.setUlct(Double.valueOf(map.get("ulct").toString()));
				transfer.setLlct(Double.valueOf(map.get("llct").toString()));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	public static void setTempValues(Transfer transfer, String tempStr) {
		Map<String, Object> map = new HashMap<>();
		try {
			bindTemp(map, tempStr); // temperature min, max, avg
			if (!map.isEmpty()) {
				transfer.setTlo((Integer) map.get("tlo"));
				transfer.setThi((Integer) map.get("thi"));
				transfer.setTav((Integer) map.get("tav"));
				transfer.setTnw(ValueUtils.toInteger(map.get("tnw"), 0));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "TEMPERATURE_PARSE_FAIL", HttpStatus.NOT_IMPLEMENTED, "Temperature Parsing Failed", e);
		}
		transfer.setTemp(tempStr);
	}

	private static void bindTemp(Map<String, Object> map, String value) {
		if (StringUtils.isEmpty(value)) {
			return;
		}

		String[] values = StringUtils.delimitedListToStringArray(value, "/");

		if (values.length < 3) {
			return;
		}

		// 2rd Gen 10분단위 온도1 / 온도2 / 온도3 / 온도4 / 온도5 / 온도1 시간 / 현재온도(tnw) / rtr / rtf / rtl / rat
		// 3rd Gen: (Recorded Every 10Mins) Temp1 / Temp2 / Temp3 / Temp4 / Temp5 / Last Temp Time / Last Temp  
		map.put("temp", value);

		String tff = null;
		String current = null;

		try {
			tff = values[values.length - 2];
			current = values[values.length - 1];

			int tlo = Integer.MAX_VALUE; // Minimum of temperature
			int thi = Integer.MIN_VALUE; // Maximum of temperature 
			int tav = 0; // Average of temperature
			int tempCount = 0;

			for (String str : values) {
				if (!ObjectUtils.isEmpty(str) && str.length() < 5 && ValueUtils.isNumber(str)) {
					int temp = Integer.parseInt(str);
					tlo = Math.min(tlo, temp);
					thi = Math.max(thi, temp);
					tav += temp;
					tempCount++;
				}
			}

			if (tempCount > 0) {
				tav = (int) Math.round(tav / ValueUtils.toDouble(tempCount, 0d));
				map.put("tav", tav);
			}

			if (tlo != Integer.MAX_VALUE) {
				map.put("tlo", tlo);
			}
			if (thi != Integer.MIN_VALUE) {
				map.put("thi", thi);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!StringUtils.isEmpty(tff)) {
			map.put("tff", tff);
		}
		if (!StringUtils.isEmpty(current)) {
			map.put("tnw", current);
		}
	}

	private static final int AOK = 0;
	private static final int BAT = 1;
	private static final int ACC = 2;
	private static final int TEM = 4;
	private static final int BTN = 8;

	public static boolean isBatteryStatusOk(String status) {
		return isSensorStatusOk(BAT, status);
	}

	private static boolean isSensorStatusOk(int sensorType, String status) {
		if (StringUtils.isEmpty(status)) {
			return true;
		}
		if (status.length() == 1) {
			status = "0x" + status;
		}
		try {
			return Integer.decode(status) / sensorType % 2 == 0;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return true;
		}
	}

	public static void populateValidShot(Cdata cdata, Statistics statistics, Mold mold, Boolean needAdjust) {
		try {
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(statistics.getCi())//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Counter.class, "EquipmentCode", statistics.getCi())); //

			if (statistics.getShotCount() == null) {
				initCdataAndStatisticsVal(cdata, statistics);
				return;
			}

			int thi = cdata.getThi() != null ? cdata.getThi().intValue() : 0;
			int tlo = cdata.getTlo() != null ? cdata.getTlo().intValue() : 0;
			Integer tdev = thi - tlo;

			if (mold == null) {
				if (statistics.getMoldId() == null) {
					initCdataAndStatisticsVal(cdata, statistics);
					return;
				}
				mold = BeanUtils.get(MoldService.class).findByIdWithoutAdditionalInfo(statistics.getMoldId());
				if (mold == null) {
					initCdataAndStatisticsVal(cdata, statistics);
					return;
				}
			}

			double wact = MoldUtils.getWact(mold.getId(), mold.getContractedCycleTime(), cdata.getMonth());

			Double ctVal = cdata.getCt();
			String cttVal = cdata.getCtt();
			Double llctVal = cdata.getLlct();
			Double ulctVal = cdata.getUlct();

			Double ctValStatistics = statistics.getCt();
			Integer shotCountVal = statistics.getShotCount();
			Double llctValStatistics = statistics.getLlct();
			Double ulctValStatistics = statistics.getUlct();

			if (Math.abs(tdev) <= 20 && statistics.getShotCount() <= 10) {
				resetValues(cdata, statistics);
			} else {
				double ctMin = (wact - 50) / 3 + 50;
				double ctMax = (wact - 50) * 2 + 50;

				if (ctMin >= cdata.getCt() || cdata.getCt() >= ctMax) {
					resetValues(cdata, statistics);
				}
			}

			cdata.setCtVal(ctVal);
			cdata.setCttVal(cttVal);
			cdata.setLlctVal(llctVal);
			cdata.setUlctVal(ulctVal);

			statistics.setLlctVal(llctValStatistics);
			statistics.setUlctVal(ulctValStatistics);
			statistics.setCtVal(ctValStatistics);
			statistics.setShotCountVal(shotCountVal);
			if (statistics.getShotCountVal() != 0 && !needAdjust) {
				mold.setLastShotAtVal(DateUtils2.maxReasonable(mold.getLastShotAt(), mold.getLastShotAtVal(), null));
				BeanUtils.get(MoldRepository.class).save(mold);
				if (counter != null) {
					counter.setLastShotAtVal(counter.getLastShotAt());
					BeanUtils.get(CounterRepository.class).save(counter);
				}
			}
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "VALID_SHOT_FAIL", HttpStatus.EXPECTATION_FAILED, "Error Occurred during Filtering Valid Shot", e);
		}

	}

	public static void initCdataAndStatisticsVal(Cdata cdata, Statistics statistics) {
		if (statistics.getShotCount() == null || statistics.getShotCount() < 0) {
			statistics.setShotCount(0);
		}
		statistics.setCtVal(statistics.getCt());
		statistics.setLlctVal(statistics.getLlct());
		statistics.setUlctVal(statistics.getUlct());
		statistics.setShotCountVal(statistics.getShotCount());
		if (cdata != null) {
			cdata.setCttVal(cdata.getCtt());
			cdata.setCtVal(cdata.getCt());
			cdata.setLlctVal(cdata.getLlctVal());
			cdata.setUlctVal(cdata.getUlct());
		}
	}

	private static void resetValues(Cdata cdata, Statistics statistics) {
		cdata.setCtVal(0d);
		cdata.setCttVal("///////////////////");
		cdata.setLlctVal(0d);
		cdata.setUlctVal(0d);

		statistics.setCtVal(0d);
		statistics.setLlctVal(0d);
		statistics.setUlctVal(0d);
		statistics.setShotCountVal(0);
	}

	public static Integer calcIncrShotCount(String ctt) {
		int i = 0;
		int incrShotCount = 0;

		if (!ObjectUtils.isEmpty(ctt)) {
			for (String str : StringUtils.tokenizeToStringArray(ctt, "/")) {
				if (i++ % 2 != 1) {
					continue;
				}
				if (!NumberUtils.isCreatable(str)) {
					continue;
				}
				incrShotCount += ValueUtils.toInteger(str, 0);
			}
		}
		return incrShotCount;
	}

	protected void testStatus() {
		System.out.println(Integer.toHexString(AOK | AOK | AOK | AOK));
		System.out.println(Integer.toHexString(AOK | AOK | AOK | BAT));
		System.out.println(Integer.toHexString(AOK | AOK | ACC | AOK));
		System.out.println(Integer.toHexString(AOK | AOK | ACC | BAT));
		System.out.println(Integer.toHexString(AOK | TEM | AOK | AOK));
		System.out.println(Integer.toHexString(AOK | TEM | AOK | BAT));
		System.out.println(Integer.toHexString(AOK | TEM | ACC | AOK));
		System.out.println(Integer.toHexString(AOK | TEM | ACC | BAT));
		System.out.println(Integer.toHexString(BTN | AOK | AOK | AOK));
		System.out.println(Integer.toHexString(BTN | AOK | AOK | BAT));
		System.out.println(Integer.toHexString(BTN | AOK | ACC | AOK));
		System.out.println(Integer.toHexString(BTN | AOK | ACC | BAT));
		System.out.println(Integer.toHexString(BTN | TEM | AOK | AOK));
		System.out.println(Integer.toHexString(BTN | TEM | AOK | BAT));
		System.out.println(Integer.toHexString(BTN | TEM | ACC | AOK));
		System.out.println(Integer.toHexString(BTN | TEM | ACC | BAT));

		System.out.println(Integer.decode("0x0"));
		System.out.println(Integer.decode("0x1"));
		System.out.println(Integer.decode("0x2"));
		System.out.println(Integer.decode("0x3"));
		System.out.println(Integer.decode("0x4"));
		System.out.println(Integer.decode("0x5"));
		System.out.println(Integer.decode("0x6"));
		System.out.println(Integer.decode("0x7"));
		System.out.println(Integer.decode("0x8"));
		System.out.println(Integer.decode("0x9"));
		System.out.println(Integer.decode("0xA"));
		System.out.println(Integer.decode("0xB"));
		System.out.println(Integer.decode("0xC"));
		System.out.println(Integer.decode("0xD"));
		System.out.println(Integer.decode("0xE"));
		System.out.println(Integer.decode("0xF"));

		int v = BAT;

		System.out.println(isSensorStatusOk(v, "0x0"));
		System.out.println(isSensorStatusOk(v, "0x1"));
		System.out.println(isSensorStatusOk(v, "0x2"));
		System.out.println(isSensorStatusOk(v, "0x3"));
		System.out.println(isSensorStatusOk(v, "0x4"));
		System.out.println(isSensorStatusOk(v, "0x5"));
		System.out.println(isSensorStatusOk(v, "0x6"));
		System.out.println(isSensorStatusOk(v, "0x7"));
		System.out.println(isSensorStatusOk(v, "0x8"));
		System.out.println(isSensorStatusOk(v, "0x9"));
		System.out.println(isSensorStatusOk(v, "0xA"));
		System.out.println(isSensorStatusOk(v, "0xB"));
		System.out.println(isSensorStatusOk(v, "0xC"));
		System.out.println(isSensorStatusOk(v, "0xD"));
		System.out.println(isSensorStatusOk(v, "0xE"));
		System.out.println(isSensorStatusOk(v, "0xF"));
	}

}
