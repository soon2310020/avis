package saleson.service.data.service;

import static java.util.stream.Collectors.groupingBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.Acceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.extern.slf4j.Slf4j;
import saleson.api.counter.CounterRepository;
import saleson.model.Counter;

@Slf4j
@Component
public class DataMapper {
	private static final String SLASH = "/";
	private static final String COMMA = ",";
	private static final String UNDERSCORE = "_";
	public static final String HDATA = "HDATA";
	public static final String CDATA = "CDATA";
	public static final String ADATA = "ADATA";
	public static final String TDATA = "TDATA";
	public static final String TEST = "TEST";

	public List<DataAcceleration> toDataAccelerationList(Data adata) {

		String[] data = StringUtils.delimitedListToStringArray(adata.getRawData(), SLASH);

		if (data == null || data.length < 3) {
			return Collections.emptyList();
		}

		int measurementCount = Integer.parseInt(data[2].trim());
		List<DataAcceleration> dataAccelerations = new ArrayList<>();

		int dataBeginIndex = 3;
		for (int i = dataBeginIndex; i < measurementCount * 2 + dataBeginIndex; i += 2) {
			if (i + 1 >= data.length) {
				continue;
			}

			DataAcceleration dataAcceleration = DataAcceleration.builder()//
					.dataId(adata.getId())//
					.terminalId(adata.getTerminalId())//
					.counterId(data[1])//
					.rawdataCreatedAt(adata.getUpdatedDate())//
					.measurementDate(data[i])//
					.build();

			String[] values = StringUtils.tokenizeToStringArray(data[i + 1], COMMA + UNDERSCORE);

			int len = values.length;
			for (int j = 0; j < len; j += 2) {
				if (j + 1 >= len) {
					continue;
				}
				String time = values[j];
				String acc = values[j + 1];
				dataAcceleration.addAcceleration(new Acceleration(time, acc));
			}

			dataAccelerations.add(dataAcceleration);
		}

		return dataAccelerations;
	}

	public List<DataAcceleration> toDataAccelerationList(String rawData) {

		String[] data = StringUtils.delimitedListToStringArray(rawData, SLASH);

		if (data == null || data.length < 3) {
			return Collections.emptyList();
		}

		int measurementCount = Integer.parseInt(data[2].trim());
		List<DataAcceleration> dataAccelerations = new ArrayList<>();

		int dataBeginIndex = 3;
		for (int i = dataBeginIndex; i < measurementCount * 2 + dataBeginIndex; i += 2) {
			if (i + 1 >= data.length) {
				continue;
			}

			DataAcceleration dataAcceleration = DataAcceleration.builder()//
					.counterId(data[1])//
					.measurementDate(data[i])//
					.build();

			String[] values = StringUtils.tokenizeToStringArray(data[i + 1], COMMA + UNDERSCORE);

			for (int j = 0; j < values.length; j += 2) {
				if (j + 1 >= values.length) {
					continue;
				}
				String time = values[j];
				String acc = values[j + 1];
				dataAcceleration.addAcceleration(new Acceleration(time, acc));
			}

			dataAccelerations.add(dataAcceleration);
		}

		return dataAccelerations;
	}

	private static List<String> INDEX_KEYS;
	static {
		List<String> indexKeys = Collections.synchronizedList(new ArrayList<>());
		for (char c = 'a'; c <= 'z'; c++) {
			indexKeys.add(String.valueOf(c));
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			indexKeys.add(String.valueOf(c));
		}
		INDEX_KEYS = indexKeys;
	}

	@Deprecated
	public DataCounter toDataCounter(String rawData) {
		String[] data = StringUtils.delimitedListToStringArray(rawData, SLASH);
		if (data == null || data.length < 3 || (!"CDATA".equals(data[0]) && !"HDATA".equals(data[0]))) {
			return null;
		}

		int length = data.length;

		// 1. Counter ID
		String counterId = data[1];
		// 2. Start Time
		String shotStartTime = adjustDateTime(data[2]);
		// 3. End Time
		String shotEndTime = adjustDateTime(length < 4 ? data[2] : data[3]);
		// 4. Shot Count
		int shotCount;
		if (length < 5) {
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(counterId).orElse(null);
			shotCount = counter == null || counter.getShotCount() == null ? 0 : counter.getShotCount();
		} else {
			shotCount = Integer.parseInt(data[4]);
		}
		// 5. Battery Status
		// 6. Status
		String batteryStatus = null;
		String status = null;
		if (length < 7) {
			try {
				BooleanBuilder filter = new BooleanBuilder().and(QDataCounter.dataCounter.counterId.eq(counterId));
				Page<DataCounter> page = BeanUtils.get(DataCounterRepository.class).findAll(filter, PageRequest.of(0, 1, Direction.DESC, "id"));
				if (!page.isEmpty()) {
					DataCounter recentData = page.getContent().get(0);
					batteryStatus = recentData.getBatteryStatus();
					status = recentData.getStatus();
				}
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.LOGIC, "UNEXPECTED_RECENT_DATA", HttpStatus.NOT_IMPLEMENTED, "Retrieve Recent Data Failed", e);
			}
		} else {
			batteryStatus = data[5];
			status = data[6];
		}

		// 7. Temperature
		// 8. Most Frequent Mold Open Time
		String temperature = "";
		BigDecimal modeOpenTime = BigDecimal.ZERO;
		if (length >= 9) {
			temperature = length < 8 ? "" : data[7];
			if (data[8].matches("\\d+")) {
				modeOpenTime = length < 9 || ObjectUtils.isEmpty(data[8]) ? BigDecimal.ZERO : new BigDecimal(data[8]);
			}
		}

		DataCounter dataCounter = DataCounter.builder()//
				.counterId(counterId)//
				.shotStartTime(shotStartTime)//
				.shotEndTime(shotEndTime)//
				.shotCount(shotCount)//
				.batteryStatus(batteryStatus)//
				.status(status)//
				.temperature(temperature)//
				.modeOpenTime(modeOpenTime)// 최빈mold open time
				.modeCycleTime(BigDecimal.ZERO)//
				.build();

		// Process Abnormal Data
		// Temporarily Logic
		// TO-DO : Data Validation Logic 
		if (length < 11) {
			return dataCounter;
		} else if (length == 11) {
			if ("ADATA".equals(data[10]) || "ADATA".equals(data[9])) {
				return dataCounter;
			}
		} else if (length >= 12) {
			if (!"ADATA".equals(data[11]) || "ADATA".equals(data[10]) || "ADATA".equals(data[9])) {
				return dataCounter;
			}
		}

		// 9. CycleTime Table
		// 10. CycleTime Index
		String indexingTable = data[9];
		String cycleTimeIndexing = data[10];

		int divLength = 4;
		int loopCount = indexingTable.length() / divLength;
		List<CycleTime> indexTables = new ArrayList<>();

		for (int i = 0; i < loopCount; i++) {
			int beginIndex = i * divLength;
			int endIndex = (i + 1) * divLength;
			String cycleTimeValue = indexingTable.substring(beginIndex, endIndex);

			BigDecimal cycleTime = BigDecimal.ZERO;
			try {
				cycleTime = new BigDecimal(cycleTimeValue).divide(new BigDecimal("10"));
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}

			indexTables.add(new CycleTime(INDEX_KEYS.get(i), cycleTime));
		}

		int len = cycleTimeIndexing.length();
		for (int i = 0; i < len; i++) {
			String indexKey = cycleTimeIndexing.substring(i, i + 1);

			// indexKey가 숫자인 경우는 pass
			if (indexKey.chars().allMatch(Character::isDigit)) {
				continue;
			}

			int nextIndex = i + 2 > len ? i + 1 : i + 2;
			String repeatKey = cycleTimeIndexing.substring(i + 1, nextIndex);

			int repeat = 1;
			if (!repeatKey.isEmpty() && repeatKey.chars().allMatch(Character::isDigit)) {
				repeat = Integer.parseInt(repeatKey);
			}

			CycleTime indexTable = indexTables.stream()//
					.filter(t -> indexKey.equals(t.getId()))//
					.findFirst()//
					.orElse(new CycleTime(indexKey, BigDecimal.ZERO));

			for (int j = 0; j < repeat; j++) {
				dataCounter.addCycleTimeIndex(indexTable);
			}
		}

		// 최빈 사이클 타임
		Map<String, Long> grouped = dataCounter.getCycleTimes().stream().collect(groupingBy(CycleTime::getId, Collectors.counting()));

		String modeKey = grouped.entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue)).map(Map.Entry::getKey).orElse("a");

		BigDecimal modeCycleTime = dataCounter.getCycleTimes().stream()//
				.filter(c -> c.getId().equals(modeKey))//
				.map(c -> c.getCycleTime())//
				.findFirst()//
				.orElse(BigDecimal.ZERO);

		dataCounter.setModeCycleTime(modeCycleTime);
		return dataCounter;
	}

	// bug fix - cycletime indexing
	// 2023.07.11 Mickey.Park
	public DataCounter toDataCounter2(String rawData) {
		String[] data = StringUtils.delimitedListToStringArray(rawData, SLASH);
		if (data == null || data.length < 3 || (!"CDATA".equals(data[0]) && !"HDATA".equals(data[0]))) {
			return null;
		}

		int length = data.length;

		// 1. Counter ID
		String counterId = data[1];
		// 2. Start Time
		String shotStartTime = adjustDateTime(data[2]);
		// 3. End Time
		String shotEndTime = adjustDateTime(length < 4 ? data[2] : data[3]);
		// 4. Shot Count
		int shotCount;
		if (length < 5) {
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(counterId).orElse(null);
			shotCount = counter == null || counter.getShotCount() == null ? 0 : counter.getShotCount();
		} else {
			shotCount = Integer.parseInt(data[4]);
		}
		// 5. Battery Status
		// 6. Status
		String batteryStatus = null;
		String status = null;
		if (length < 7) {
			try {
				BooleanBuilder filter = new BooleanBuilder().and(QDataCounter.dataCounter.counterId.eq(counterId));
				Page<DataCounter> page = BeanUtils.get(DataCounterRepository.class).findAll(filter, PageRequest.of(0, 1, Direction.DESC, "id"));
				if (!page.isEmpty()) {
					DataCounter recentData = page.getContent().get(0);
					batteryStatus = recentData.getBatteryStatus();
					status = recentData.getStatus();
				}
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.LOGIC, "UNEXPECTED_RECENT_DATA", HttpStatus.NOT_IMPLEMENTED, "Retrieve Recent Data Failed", e);
			}
		} else {
			batteryStatus = data[5];
			status = data[6];
		}

		// 7. Temperature
		// 8. Most Frequent Mold Open Time
		String temperature = "";
		BigDecimal modeOpenTime = BigDecimal.ZERO;
		if (length >= 9) {
			temperature = length < 8 ? "" : data[7];
			if (data[8].matches("\\d+")) {
				modeOpenTime = length < 9 || ObjectUtils.isEmpty(data[8]) ? BigDecimal.ZERO : new BigDecimal(data[8]);
			}
		}

		DataCounter dataCounter = DataCounter.builder()//
				.counterId(counterId)//
				.shotStartTime(shotStartTime)//
				.shotEndTime(shotEndTime)//
				.shotCount(shotCount)//
				.batteryStatus(batteryStatus)//
				.status(status)//
				.temperature(temperature)//
				.modeOpenTime(modeOpenTime)// 최빈mold open time
				.modeCycleTime(BigDecimal.ZERO)//
				.build();

		// Process Abnormal Data
		// Temporarily Logic
		// TO-DO : Data Validation Logic 
		if (length < 11) {
			return dataCounter;
		} else if (length == 11) {
			if ("ADATA".equals(data[10]) || "ADATA".equals(data[9])) {
				return dataCounter;
			}
		} else if (length >= 12) {
			if (!"ADATA".equals(data[11]) || "ADATA".equals(data[10]) || "ADATA".equals(data[9])) {
				return dataCounter;
			}
		}

		// 9. CycleTime Table
		// 10. CycleTime Index
		String indexingTable = data[9];
		String cycleTimeIndexing = data[10];

		int divLength = 4;
		int loopCount = indexingTable.length() / divLength;
		ConcurrentHashMap<String, CycleTime> indexTable = new ConcurrentHashMap<String, CycleTime>();

		for (int i = 0; i < loopCount; i++) {
			int beginIndex = i * divLength;
			int endIndex = (i + 1) * divLength;
			String cycleTimeValue = indexingTable.substring(beginIndex, endIndex);

			BigDecimal cycleTime = BigDecimal.ZERO;
			try {
				cycleTime = new BigDecimal(cycleTimeValue).divide(new BigDecimal("10"));
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
				return dataCounter;
			}
			indexTable.put(INDEX_KEYS.get(i), new CycleTime(INDEX_KEYS.get(i), cycleTime));
		}

		// RegExr Group 1 : No digit, Group 2: Digit or None
		if (StringUtils.hasText(indexingTable) && StringUtils.hasText(cycleTimeIndexing)) {
			Pattern pattern = Pattern.compile("(\\D)(\\d+)?");
			Matcher matcher = pattern.matcher(cycleTimeIndexing);

			while (matcher.find()) {
				char idxKey = matcher.group(1).charAt(0);
				String repeatStr = matcher.group(2);

				int repeat = (repeatStr != null) ? Integer.parseInt(repeatStr) : 1;

				for (int i = 0; i < repeat; i++) {
					if (ObjectUtils.isEmpty(indexTable.get(String.valueOf(idxKey)))) {
						dataCounter.addCycleTimeIndex(indexTable.get(INDEX_KEYS.get(loopCount - 1)));
					} else {
						dataCounter.addCycleTimeIndex(indexTable.get(String.valueOf(idxKey)));
					}
				}
			}

			// Get Frequency CycleTime 
			if (!ObjectUtils.isEmpty(dataCounter.getCycleTimes())) {

				Map<BigDecimal, Long> cycleTimeMap = dataCounter.getCycleTimes().stream() //
						.filter(Objects::nonNull) // Remove null elements
						.collect(Collectors.groupingBy(CycleTime::getCycleTime, Collectors.counting()));

				BigDecimal mostFrequentCycleTime = cycleTimeMap.entrySet().stream() //
						.max(Map.Entry.comparingByValue()) //
						.map(Map.Entry::getKey) //
						.orElse(null);

				dataCounter.setModeCycleTime(mostFrequentCycleTime);
			}
		}
		return dataCounter;
	}

	private static String adjustDateTime(String value) {
		if (ObjectUtils.isEmpty(value) || value.length() <= 14) {
			return value;
		}
		value = value.substring(value.length() - 14);
		return value;
	}

}
