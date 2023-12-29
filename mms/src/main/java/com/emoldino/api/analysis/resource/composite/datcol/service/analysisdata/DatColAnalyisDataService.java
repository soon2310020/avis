package com.emoldino.api.analysis.resource.composite.datcol.service.analysisdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Convert;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.JsonCycleTimeConverter;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.framework.util.BeanUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import saleson.model.Transfer;
import saleson.service.data.service.DataMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class DatColAnalyisDataService {

	private final DataMapper dataMapper;

	public AnalysisResult getAnalysisResult(String rawData) {

		Map<String, String> map = DatColUtils.separates(rawData);

		if (!map.get(DataMapper.CDATA).isEmpty()) {
			String[] datas = StringUtils.delimitedListToStringArray(rawData, "/");
			if (datas == null || datas.length < 3) {
				return null;
			}

			String counterId = datas[1];
			String recordStartTime = datas[2];
			String recordEndTime = datas[3];
			String totalShotCount = datas[4];

			// CycleTime Index List
			String indexListStr = datas[9];
			ConcurrentHashMap<String, CycleTime> indexMap = generateIndexMap(indexListStr);

			// CycleTime List
			String cycleTimeIndex = datas[10];
			List<CycleTime> cycleTimes = generateCycleTimeList(cycleTimeIndex, indexMap);

			return AnalysisResult.builder() //					
					.counterId(counterId) //
					.recordStartTime(recordStartTime) //
					.recordEndTime(recordEndTime) //
					.totalShotCount(Integer.valueOf(totalShotCount)) //
					.cycleTimeIndex(cycleTimeIndex) //
					.shotCountByRawData(cycleTimes.size()) //
					.cycleTimes(cycleTimes) //
					.build();

		} else {
			return null;
		}
	}

	public List<AnalysisResult> getAnalysisResultByDataId(Long dataId) {

		Data data = BeanUtils.get(DataRepository.class).findById(dataId).orElse(null);
		Map<String, String> map = DatColUtils.separates(data.getRawData());

		if (!map.get(DataMapper.CDATA).isEmpty()) {
			String[] datas = StringUtils.delimitedListToStringArray(data.getRawData(), "/");
			if (datas == null || datas.length < 3) {
				return null;
			}

			String counterId = datas[1];
			String recordStartTime = datas[2];
			String recordEndTime = datas[3];
			String totalShotCount = datas[4];

			// CycleTime Index List
			String indexListStr = datas[9];
			ConcurrentHashMap<String, CycleTime> indexMap = generateIndexMap(indexListStr);

			// CycleTime List
			String cycleTimeIndex = datas[10];
			List<CycleTime> cycleTimes = generateCycleTimeList(cycleTimeIndex, indexMap);

			// Ctt (Real Logic)
			String[] ci = { null };
			List<DataCounter> dataCounters = new ArrayList<>();
			{
				DataCounter item = null;
				if (!map.get(DataMapper.CDATA).isEmpty()) {
					item = dataMapper.toDataCounter2(map.get(DataMapper.CDATA));
				} else if (!map.get(DataMapper.HDATA).isEmpty()) {
					item = dataMapper.toDataCounter2(map.get(DataMapper.HDATA));
				}
				if (item != null) {
					if (ci[0] == null) {
						ci[0] = item.getCounterId();
					}
					item.setDataId(data.getId());
					item.setTerminalId(data.getTerminalId());
					item.setReadTime(data.getReadTime());
					item.setRawdataCreatedAt(data.getUpdatedDate());
					dataCounters.add(item);
				}
			}

			if (!ObjectUtils.isEmpty(dataCounters)) {
				List<DataCounter> list = dataCounters.stream()//
						.sorted(Comparator.comparing(DataCounter::getShotStartTime))//
						.collect(Collectors.toList());

				List<Transfer> transfers = DatColUtils.toTransfers(list);
				List<AnalysisResult> results = new ArrayList<>();
				transfers.forEach(transfer -> {
					results.add(AnalysisResult.builder() //		
							.dataId(data.getId())// 
							.counterId(counterId) //
							.recordStartTime(recordStartTime) //
							.recordEndTime(recordEndTime) //
							.tff(transfer.getTff())//
							.totalShotCount(Integer.valueOf(totalShotCount)) //
							.cycleTimeIndex(cycleTimeIndex) //
							.shotCountByRawData(cycleTimes.size()) //
							.cycleTimes(cycleTimes) //
							.ctt(transfer.getCtt()) //
							.shotCountByCtt(DatColUtils.calcIncrShotCount(transfer.getCtt())) //
							.thi(transfer.getThi()) //
							.tlo(transfer.getTlo()) //
							.tav(transfer.getTav())//
							.temp(transfer.getTemp()) //
							.build());
				});

				return results;

			} else {
				return Arrays.asList(AnalysisResult.builder() //					
						.counterId(counterId) //
						.recordStartTime(recordStartTime) //
						.recordEndTime(recordEndTime) //
						.totalShotCount(Integer.valueOf(totalShotCount)) //
						.cycleTimeIndex(cycleTimeIndex) //
						.shotCountByRawData(cycleTimes.size()) //
						.cycleTimes(cycleTimes) //
						.build());
			}
		} else {
			return null;
		}
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

	private static ConcurrentHashMap<String, CycleTime> generateIndexMap(String indexListStr) {
		int divLength = 4;
		ConcurrentHashMap<String, CycleTime> indexMap = new ConcurrentHashMap<String, CycleTime>();
		for (int i = 0; i < indexListStr.length() / divLength; i++) {
			int beginIndex = i * divLength;
			int endIndex = (i + 1) * divLength;
			String cycleTimeValue = indexListStr.substring(beginIndex, endIndex);

			BigDecimal cycleTime = BigDecimal.ZERO;
			try {
				cycleTime = new BigDecimal(cycleTimeValue).divide(new BigDecimal("10"));
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
			indexMap.put(INDEX_KEYS.get(i), new CycleTime(INDEX_KEYS.get(i), cycleTime));
		}

		return indexMap;
	}

	private static List<CycleTime> generateCycleTimeList(String cycleTimeIndex, ConcurrentHashMap<String, CycleTime> map) {
		List<CycleTime> cycleTimeList = new ArrayList<>();
		Pattern pattern = Pattern.compile("(\\D)(\\d+)?");
		Matcher matcher = pattern.matcher(cycleTimeIndex);

		while (matcher.find()) {
			char idxKey = matcher.group(1).charAt(0);
			String repeatStr = matcher.group(2);

			int repeat = (repeatStr != null) ? Integer.parseInt(repeatStr) : 1;
			for (int i = 0; i < repeat; i++) {
				cycleTimeList.add(map.get(String.valueOf(idxKey)));
			}
		}
		return cycleTimeList;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@lombok.Data
	public static class AnalysisResult {
		Long dataId;
		String counterId;
		String recordStartTime;
		String recordEndTime;
		String tff;
		int totalShotCount;
		String cycleTimeIndex;
		int shotCountByRawData;
		@Convert(converter = JsonCycleTimeConverter.class)
		List<CycleTime> cycleTimes;
		String ctt;
		int shotCountByCtt;
		int thi;
		int tlo;
		int tav;
		String temp;
	}
}
