package com.emoldino.api.analysis.resource.composite.datcol.service.accelerationdata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Arrays;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColAccData;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColProcessInfoOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DatColAccelerationDataService {

	private final JPAQueryFactory queryFactory;

	public ArrayList<DatColProcessInfoOut> getAccData(Long dataId) {

		Data data = BeanUtils.get(DataRepository.class).findById(dataId) //
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Data.class, "Id", dataId));

		ArrayList<DatColProcessInfoOut> out = new ArrayList<>();

		String[] dataElem = StringUtils.tokenizeToStringArray(data.getRawData(), "/");

		// *** Data 내 가속도 데이터 추출 ***
		//  14번째 항목 :가속도 샷 수 in Raw, tmp[13]
		//  [12+2N] 번째 항목 : [N]번째 샷의 기록 시작 시간
		//  [12+2N+1] 번째 항목 : [N+1]번째 샷의 가속도 데이터(시간, 값 데이터 순)
		if (dataElem.length < 12) {
			return null;
		}
		int accDataIndex = 12;
		String sensorId = dataElem[12];
		int accShotsNum = Integer.parseInt(dataElem[13]);

		for (int num = 0; num < accShotsNum; num++) {

			// *** Data Organization
			// 향후 원래 데이트 그룹에 필터 된 시간을 기준으로 한 구분법을 정하기 위해 별도의 이름을 저장함. - accDataMapOrigin
			LinkedHashMap<Integer, DatColAccData> rawAccDataMap = new LinkedHashMap<>();
			LinkedHashMap<Integer, DatColAccData> accDataMapOrigin = new LinkedHashMap<>();


			// *** Extract Acceleration Data
			accDataIndex += 2;
			String accRecordStartTimeStr = dataElem[accDataIndex]; // 가속도 샷 기록 시작 시간, 가속도 기록 시작과 샷 기록 시작은 동일한 값임. 서로 다른 값 아님.
			String accDataStr = dataElem[accDataIndex + 1]; // 해당 샷에 대한 가속도 데이터, 시간, 값 순으로 나열되어 있음.

			LocalDateTime accRecordStartTime = LocalDateTime.parse(accRecordStartTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			int weekNum = accRecordStartTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
			String day = accRecordStartTime.toString();

			/*
			*  ============== 이전 주차의 데이터에서 기준 값들을 도출하는 코드, 맨 처음에는 수행하지 않음. ==============
			   # 아래의 코드는 이전 주차의 기준 값을 산출하는 코드임.
			   # 아래의 코드는 계산된 데이터가 한 개 이상일 경우, 이전 데이터의 주차와 현재 데이터의 주차를 비교하여 주차가 다를 경우, 이전 주차의 데이터에서 기준을 산출하라는 의미임.
			   # 서버 구현 시 더 쉽고 효율적인 방법으로 구현 가능할 경우, 아래와 같이 구현하지 않아도 됨.
			   # 흐름은 이전 주차에서 다음 주차로 바뀔 시에 이전 주차에 대한 데이터를 활용하여 기준 값을 산출하는 것임.
			   # 이전 주차와 현재 주차의 비교와 이전 주차에 데이터를 추출하는 코드이나 효율적이지 않을 수 있음.
			 * */
			String[] accDataElem = StringUtils.tokenizeToStringArray(accDataStr, "_"); // 한 사이클 내 가속도 데이터 구분 [시간, 값], [시간, 값], ... 형태로 구분

			// 한 사이클 내 가속도 데이터에 대해 시간과 값을 구분하여 각각 데이터 열로 저장. 데이터 처리 프로세스를 위한 항목 추출
			for (int accDataElemIdx = 0; accDataElemIdx < accDataElem.length; accDataElemIdx++) {
				String[] accTimeValue = StringUtils.tokenizeToStringArray(accDataElem[accDataElemIdx], ",");
				DatColAccData datColOldAccDataOrigin = new DatColAccData();
				datColOldAccDataOrigin.setAccMesuredTime(Double.valueOf(accTimeValue[0]));
				datColOldAccDataOrigin.setAccMesuredValue(Double.valueOf(accTimeValue[1]));
				accDataMapOrigin.put(accDataElemIdx, datColOldAccDataOrigin);

				DatColAccData datColOldAccData = new DatColAccData();
				datColOldAccData.setAccMesuredTime(Double.valueOf(accTimeValue[0]));
				datColOldAccData.setAccMesuredValue(Double.valueOf(accTimeValue[1]));
				rawAccDataMap.put(accDataElemIdx, datColOldAccData);

			}

			Double accDataMaxValue = rawAccDataMap.values().stream() //
					.mapToDouble(DatColAccData::getAccMesuredValue) //
					.max() //
					.orElse(0.0);

			Double accDataMinValue = rawAccDataMap.values().stream() //
					.mapToDouble(DatColAccData::getAccMesuredValue) //
					.min() //
					.orElse(0.0);

			accDataMaxValue = roundDoubleValue(accDataMaxValue, 3);
			accDataMinValue = roundDoubleValue(accDataMinValue, 3);



			/*
			#=======================================================================================================================
			#Step 1. 기록되지 않은 가속도 데이터 생성하기. % 정확한 시간당 변화량을 파악하기 위함.
			#=======================================================================================================================
			  #  가속도 데이터 중 [N]번째 시간과 [N-1]번째 시간의 차이가 0.06보다 크거나 같은 경우,
			  #  [N] 번째 시간 앞에  [N]번째 시간에 -0.03초를 적용한 데이터를 추가함. 이 때 가속도 값은 0임.
			 * */
			int mapIdx = 0;
			int mapSize = rawAccDataMap.size();
			LinkedHashMap<Integer, DatColAccData> accDataMap = new LinkedHashMap<>();
			for (int idx = 0; idx < mapSize; idx++) {

				if (ObjectUtils.isEmpty(rawAccDataMap.get(idx)))
					continue;

				if (idx == 0) {
					DatColAccData accData = new DatColAccData();
					accData.setAccMesuredTime(roundDoubleValue(rawAccDataMap.get(idx).getAccMesuredTime(), 2));
					accData.setAccMesuredValue(rawAccDataMap.get(idx).getAccMesuredValue());
					accDataMap.put(mapIdx, accData);
					mapIdx++;
					continue;
				} else {
					Double accTime1 = roundDoubleValue(rawAccDataMap.get(idx - 1).getAccMesuredTime(), 2);
					Double accValue1 = rawAccDataMap.get(idx - 1).getAccMesuredValue();
					Double accTime2 = roundDoubleValue(rawAccDataMap.get(idx).getAccMesuredTime(), 2);
					Double accValue2 = rawAccDataMap.get(idx).getAccMesuredValue();

					if (roundDoubleValue(accTime2 - accTime1, 2) >= 0.06) { // N번째 시간과 N-1번째 시간의 차이가 0.06초보도 클 경우
						DatColAccData addedAccData = new DatColAccData();
						addedAccData.setAccMesuredTime(roundDoubleValue(accTime2 - 0.03, 2));
						addedAccData.setAccMesuredValue(accDataMinValue);
						accDataMap.put(mapIdx, addedAccData);
						mapIdx++;

						DatColAccData accData = new DatColAccData();
						accData.setAccMesuredTime(accTime2);
						accData.setAccMesuredValue(accValue2);
						accDataMap.put(mapIdx, accData);
						mapIdx++;

					} else {
						DatColAccData accData = new DatColAccData();
						accData.setAccMesuredTime(accTime2);
						accData.setAccMesuredValue(accValue2);
						accDataMap.put(mapIdx, accData);
						mapIdx++;
					}
				}
			}

			/*
			#=======================================================================================================================
			#Step 2. 가속도 데이터에 대해 최대-최소 정규화를 진행함. % 금형과 제품 별로 최대한 동일한 기준을 적용하기 위함.
			#=======================================================================================================================
			* */
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				double value = accData.getAccMesuredValue();
				double normValue = roundDoubleValue((value - accDataMinValue) / (accDataMaxValue - accDataMinValue), 3);
				accData.setNormAccValue(normValue);
				accDataMap.put(idx, accData);
			}

			/*
			#=======================================================================================================================
			#Step 3. 정규화된 가속도 데이터에 1계 도함수 (1차 기울기)를 산출함.
			#=======================================================================================================================
			* */
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				DatColAccData accDataBefore = accDataMap.get(idx - 1);
				if (idx == 0) {
					double normValue = accData.getNormAccValue();
					double gradientValue = roundDoubleValue(normValue / 0.03, 3);
					accData.setFirstGradient(gradientValue);
					accDataMap.put(idx, accData);
				} else {
					double normValue1 = accDataBefore.getNormAccValue();
					double normValue2 = accData.getNormAccValue();
					double accTime1 = accDataBefore.getAccMesuredTime();
					double accTime2 = accData.getAccMesuredTime();
					double gradientValue = roundDoubleValue((normValue2 - normValue1) / (accTime2 - accTime1), 3);
					accData.setFirstGradient(gradientValue);
					accDataMap.put(idx, accData);
				}
			}

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				double gradientValue = accData.getFirstGradient();
				if (gradientValue >= 0.0) {
					accData.setFirstGradientSign(1);
				} else {
					accData.setFirstGradientSign(-1);
				}
				accDataMap.put(idx, accData);
			}

			/*
			#=======================================================================================================================
			#Step 4. 정규화된 가속도 데이터에 2계 도함수 (2차 기울기)를 산출함.
			#=======================================================================================================================
			* */
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				DatColAccData accDataBefore = accDataMap.get(idx - 1);
				if (idx == 0) {
					accData.setSecondGradient(0.0);
				} else {
					double firstGradientValue1 = accDataBefore.getFirstGradient();
					double firstGradientValue2 = accData.getFirstGradient();
					double accTime1 = accDataBefore.getAccMesuredTime();
					double accTime2 = accData.getAccMesuredTime();
					double secondGradientValue = roundDoubleValue((firstGradientValue2 - firstGradientValue1) / (accTime2 - accTime1), 3);
					accData.setSecondGradient(secondGradientValue);
				}
				accDataMap.put(idx, accData);
			}

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				double gradientValue = accData.getSecondGradient();
				if (gradientValue >= 0.0) {
					accData.setSecondGradientSign(1);
				} else {
					accData.setSecondGradientSign(-1);
				}
				accDataMap.put(idx, accData);
			}

			/*
			#=======================================================================================================================
			#Step 5. 가속도 데이터가 위로 볼록일 때, 최대값 찾기 (금형 형개 시점 탐색_1)
			#=======================================================================================================================
			* */
			accDataMap.entrySet() //
					.removeIf(entry -> entry.getValue().getFirstGradient() == 0);
			accDataMap = rearrangeIndex(accDataMap);

			accDataMap.entrySet() //
					.removeIf(entry -> entry.getValue().getSecondGradient() == 0);
			accDataMap = rearrangeIndex(accDataMap);

			if (ObjectUtils.isEmpty(accDataMap))
				continue;

			/*
			#=======================================================================================================================
			#Step 6. 가속도 데이터가 위로 볼록일 때, 최대값 찾기 (금형 형개 시점 탐색_1)
			#=======================================================================================================================
			* */

			// 주석 처리

			/*
			#=======================================================================================================================
			#Step 7. 가속도 데이터가 아래로 볼록일 때, 최소값 찾기 (공정단계별 시점 탐색)
			#=======================================================================================================================
			* */
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				if (idx < accDataMap.size() - 1) {
					int firstGradientSign2 = accData.getFirstGradientSign();
					int secondGradientSign2 = accData.getSecondGradientSign();
					int firstGradientSign1 = 0;
					int secondGradientSign1 = 0;
					if (idx == 0) {
						firstGradientSign1 = accDataMap.get(accDataMap.size() - 1).getFirstGradientSign();
						secondGradientSign1 = accDataMap.get(accDataMap.size() - 1).getSecondGradientSign();
					} else {
						firstGradientSign1 = accDataMap.get(idx - 1).getFirstGradientSign();
						secondGradientSign1 = accDataMap.get(idx - 1).getSecondGradientSign();
					}

					if (firstGradientSign2 > 0 && secondGradientSign2 > 0 && firstGradientSign1 < 0) {
						accData.setDeterminant3(1);
					} else {
						accData.setDeterminant3(0);
					}
				} else {
					accData.setDeterminant3(1);
				}
				accDataMap.put(idx, accData);
			}

			/*
			#=======================================================================================================================
			#Step 8. 특정 피크 값들만 추출하기
			#=======================================================================================================================
			 * */
			accDataMap.entrySet() //
					.removeIf(entry -> entry.getValue().getDeterminant3() == 0);
			accDataMap = rearrangeIndex(accDataMap);

			DatColAccData lastData = accDataMap.get(accDataMap.size() - 1);
			lastData.setFirstGradient(1.000);
			accDataMap.put(accDataMap.size() - 1, lastData);

			accDataMap.entrySet() //
					.removeIf(entry -> entry.getValue().getFirstGradient() < 0.000);
			// Key를 순서대로 0부터 재배치
			accDataMap = rearrangeIndex(accDataMap);

			LinkedHashMap<Integer, DatColAccData> accDataMapTmp = (LinkedHashMap<Integer, DatColAccData>) accDataMap.clone();

			/*
			#=======================================================================================================================
			#Step 8. 근접한 피크 값 제거하기.
			#=======================================================================================================================
			* */

			int limit = accDataMap.size() - 1;

			double average = accDataMap.values().stream() //
					.mapToDouble(DatColAccData::getAccMesuredValue) //
					.limit(limit) //
					.average()//
					.orElse(0.0);

			if (limit == 0) {
				average = accDataMap.get(0).getAccMesuredValue();
			}

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				if (idx < accDataMap.size() - 1) {
					if (accData.getFirstGradient() < 1 && accData.getAccMesuredValue() <= average) {
						accData.setFinalDet(0);
					} else {
						accData.setFinalDet(1);
					}
				} else {
					accData.setFinalDet(1);

				}
				accDataMap.put(idx, accData);
			}

			accDataMap.entrySet() //
					.removeIf(entry -> entry.getValue().getFinalDet() == 0);
			// Key를 순서대로 0부터 재배치
			accDataMap = rearrangeIndex(accDataMap);

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				DatColAccData accDataBefore = accDataMap.get(idx - 1);

				if (idx <= 1) {
					accData.setDeterminant4(1);
				} else {
					if (roundDoubleValue(accData.getAccMesuredTime() - accDataBefore.getAccMesuredTime(), 2) < 0.15) {
						accData.setDeterminant4(0);
					} else {
						accData.setDeterminant4(1);
					}
				}
				accDataMap.put(idx, accData);
			}

			accDataMap.entrySet() //
					.removeIf(entry -> entry.getValue().getDeterminant4() == 0);

			// Key를 순서대로 0부터 재배치
			accDataMap = rearrangeIndex(accDataMap);

			/*
			#=======================================================================================================================
			#Step 9. [Step 8]에서 구분한 각각의 Label의 시간을 기준으로 원래 데이터에 구간을 구분함.
			#=======================================================================================================================
			* */
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				accData.setFilterLabel(idx);
				accDataMap.put(idx, accData);
			}

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				DatColAccData accData = accDataMap.get(idx);
				DatColAccData accDataBefore = accDataMap.get(idx - 1);

				int label = accData.getFilterLabel() - 1;
				double time2 = accData.getAccMesuredTime();

				if (idx == 0) {
					for (int originIdx = 0; originIdx < accDataMapOrigin.size(); originIdx++) {
						DatColAccData originData = accDataMapOrigin.get(originIdx);
						if (originData.getAccMesuredTime() < time2) {
							originData.setAccLabel(label);
							accDataMapOrigin.put(originIdx, originData);
						}
					}
				} else {
					Double time1 = accDataBefore.getAccMesuredTime();
					for (int originIdx = 0; originIdx < accDataMapOrigin.size(); originIdx++) {
						DatColAccData originData = accDataMapOrigin.get(originIdx);
						if (originData.getAccMesuredTime() >= time1 && originData.getAccMesuredTime() < time2) {
							originData.setAccLabel(label);
							accDataMapOrigin.put(originIdx, originData);
						}
					}
				}
			}

			int lastLabel = accDataMap.get(accDataMap.size() - 1).getFilterLabel();
			double lastTime = accDataMap.get(accDataMap.size() - 1).getAccMesuredTime();
			for (int originIdx = 0; originIdx < accDataMapOrigin.size(); originIdx++) {
				DatColAccData originData = accDataMapOrigin.get(originIdx);
				if (originData.getAccMesuredTime() > lastTime) {
					originData.setAccLabel(lastLabel);
					accDataMapOrigin.put(originIdx, originData);
				}
			}

			/*
			#=======================================================================================================================
			#Step 10. [Step 9]에서 적용한 label 구간들에 대해 구간의 첫번째 값을 구간의 시간, 최대 값을 구간의 값으로 추출함.
			#=======================================================================================================================
			* */

			ArrayList<Integer> labelType = (ArrayList<Integer>) accDataMapOrigin.values().stream() //
					.map(DatColAccData::getAccLabel) //
					.distinct() //
					.collect(Collectors.toList());

			LinkedHashMap<Integer, DatColAccData> processPeakDataMap = new LinkedHashMap<>();
			int processPeakIdx = 0;
			for (int idx = 1; idx < labelType.size(); idx++) {
				int tarLabel = labelType.get(idx);
				Map<Integer, DatColAccData> labeledDataMap = accDataMapOrigin.entrySet().stream() //
						.filter(entry -> entry.getValue().getAccLabel() == tarLabel) //
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

				Double time = labeledDataMap.entrySet() //
						.iterator().next().getValue().getAccMesuredTime();

				Double maxValue = labeledDataMap.values().stream() //
						.mapToDouble(DatColAccData::getAccMesuredValue) //
						.max() //
						.orElse(0.0);

				DatColAccData labeledData = new DatColAccData();
				labeledData.setProcessTime(time);
				labeledData.setProcessValue(maxValue);
				processPeakDataMap.put(processPeakIdx, labeledData);
				processPeakIdx++;
			}

			/*
			#=======================================================================================================================
			#Step 11. [Step 10]에서 산출한 process_peak 데이터의 개수에 따른 구간 구분 및 공정시간, 압력 레벨을 산출함.
			#=======================================================================================================================
			* */
			int boundary = -1;
			for (int idx = 0; idx < processPeakDataMap.size() - 1; idx++) {
				double time1 = processPeakDataMap.get(idx).getProcessTime();
				double time2 = processPeakDataMap.get(idx + 1).getProcessTime();
				if (roundDoubleValue(time2 - time1, 1) < 0.3) {
					boundary = idx;
				} else {
					break;
				}
			}

			final int finalBoundary = boundary;
			LinkedHashMap<Integer, DatColAccData> filteredProcessPeakDataMap = processPeakDataMap.entrySet() //
					.stream() //
					.filter(entry -> entry.getKey() >= finalBoundary + 1) //
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

			double injectionTime;
			double packingTime;
			double coolingTime;
			double packingPressure;
			double injectionPressure;

			// Map에서 모든 값들을 Collection<DatColAccData> 형태로 가져옴
			Collection<DatColAccData> values = filteredProcessPeakDataMap.values();
			List<DatColAccData> list = new ArrayList<>(values);
			int processPeakSize = list.size();

			if (processPeakSize <= 1) {
				injectionTime = 0.0;
				packingTime = 0.0;
				coolingTime = 0.0;
				injectionPressure = 0.0;
				packingPressure = 0;
			} else if (processPeakSize == 2) {
				injectionTime = list.get(1).getProcessTime() - list.get(0).getProcessTime();
				packingTime = 0.0;
				coolingTime = 0.0;
				injectionPressure = 0.0;
				packingPressure = 0.0;
			} else if (processPeakSize == 3) {
				injectionTime = 0.0;
				packingTime = list.get(1).getProcessTime() - list.get(0).getProcessTime();
				coolingTime = list.get(2).getProcessTime() - list.get(1).getProcessTime();
				injectionPressure = 0.0;
				packingPressure = list.get(1).getProcessValue();
			} else if (processPeakSize == 4) {
				injectionTime = list.get(1).getProcessTime() - list.get(0).getProcessTime();
				packingTime = list.get(2).getProcessTime() - list.get(1).getProcessTime();
				coolingTime = list.get(3).getProcessTime() - list.get(2).getProcessTime();
				packingPressure = list.get(2).getProcessValue();
				injectionPressure = list.get(1).getProcessValue() + packingPressure;
			} else {
				injectionTime = list.get(1).getProcessTime() - list.get(0).getProcessTime();

				double packingStartTime = list.get(1).getProcessTime();
				double packingEndTime = list.get(2).getProcessTime();
				packingTime = packingEndTime - packingStartTime;

				double moldOpenTime = list.get(list.size() - 1).getProcessTime();
				coolingTime = moldOpenTime - packingEndTime;

				int orderPackingEnd = 2;
				double candiPackingTime;
				double candiProcessTime;
				double candiCoolingTime;
				for (int idx = 3; idx < processPeakSize - 1; idx++) {
					packingEndTime = list.get(idx).getProcessTime();
					candiPackingTime = packingEndTime - packingStartTime;
					candiProcessTime = candiPackingTime + injectionTime;
					candiCoolingTime = moldOpenTime - packingEndTime;
					if (candiProcessTime < (candiCoolingTime * 0.9)) {
						packingTime = list.get(idx).getProcessTime() - packingStartTime;
						coolingTime = moldOpenTime - list.get(idx).getProcessTime();
						orderPackingEnd = idx;
					}
				}

				packingPressure = list.get(orderPackingEnd).getProcessValue();
				injectionPressure = packingPressure + list.get(1).getProcessValue();
			}

			injectionTime = roundDoubleValue(injectionTime, 1);
			packingTime = roundDoubleValue(packingTime, 1);
			coolingTime = roundDoubleValue(coolingTime, 1);
			packingPressure = roundDoubleValue(packingPressure, 0);
			injectionPressure = roundDoubleValue(injectionPressure, 0);

			DatColProcessInfoOut processInfoOut = new DatColProcessInfoOut();
			processInfoOut.setCounterId(sensorId);
			processInfoOut.setWeek(weekNum);
			processInfoOut.setMeasuredTime(accRecordStartTimeStr);
			processInfoOut.setInjectionTime(injectionTime);
			processInfoOut.setPackingTime(packingTime);
			processInfoOut.setCoolingTime(coolingTime);
			processInfoOut.setInjectionPressureIndex(injectionPressure);
			processInfoOut.setPackingPressureIndex(packingPressure);
			out.add(processInfoOut);

		} // Loop End

		// 시간 비교를 어떻게 해야하는지 확인 후 Return
		return out;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<DatColProcessInfoOut> getAccData2(Long dataId) throws CloneNotSupportedException {

		Data data = BeanUtils.get(DataRepository.class).findById(dataId) //
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Data.class, "Id", dataId));

		ArrayList<DatColProcessInfoOut> out = new ArrayList<>();

		String[] dataElem = StringUtils.tokenizeToStringArray(data.getRawData(), "/");
		LinkedHashMap<Integer, Map<String, Double>> processPeakDataMap = new LinkedHashMap<>();

		// *** Data 내 가속도 데이터 추출 ***
		//  14번째 항목 :가속도 샷 수 in Raw, tmp[13]
		//  [12+2N] 번째 항목 : [N]번째 샷의 기록 시작 시간
		//  [12+2N+1] 번째 항목 : [N+1]번째 샷의 가속도 데이터(시간, 값 데이터 순)
		if (dataElem.length < 12) {
			return null;
		}
		int accDataIndex = 12;
		String sensorId = dataElem[12];
		int accShotsNum = Integer.parseInt(dataElem[13]);

		for (int num = 0; num < accShotsNum; num++) {

			processPeakDataMap.clear();
			
			// *** Data Organization
			// 향후 원래 데이트 그룹에 필터 된 시간을 기준으로 한 구분법을 정하기 위해 별도의 이름을 저장함. - accDataMapOrigin

// 2023-04-04 Changed for compatibility with Mason code.

			LinkedHashMap<Integer, DatColAccData> rawAccDataMapDu = new LinkedHashMap<>();
			LinkedHashMap<Integer, DatColAccData> accDataMapOriginDu = new LinkedHashMap<>();

//


			// *** Extract Acceleration Data
			accDataIndex += 2;
			String accRecordStartTimeStr = dataElem[accDataIndex]; // 가속도 샷 기록 시작 시간, 가속도 기록 시작과 샷 기록 시작은 동일한 값임. 서로 다른 값 아님.
			String accDataStr = dataElem[accDataIndex + 1]; // 해당 샷에 대한 가속도 데이터, 시간, 값 순으로 나열되어 있음.

			LocalDateTime accRecordStartTime = LocalDateTime.parse(accRecordStartTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			int weekNum = accRecordStartTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
			String day = accRecordStartTime.toString();

			/*
			*  ============== 이전 주차의 데이터에서 기준 값들을 도출하는 코드, 맨 처음에는 수행하지 않음. ==============
			   # 아래의 코드는 이전 주차의 기준 값을 산출하는 코드임.
			   # 아래의 코드는 계산된 데이터가 한 개 이상일 경우, 이전 데이터의 주차와 현재 데이터의 주차를 비교하여 주차가 다를 경우, 이전 주차의 데이터에서 기준을 산출하라는 의미임.
			   # 서버 구현 시 더 쉽고 효율적인 방법으로 구현 가능할 경우, 아래와 같이 구현하지 않아도 됨.
			   # 흐름은 이전 주차에서 다음 주차로 바뀔 시에 이전 주차에 대한 데이터를 활용하여 기준 값을 산출하는 것임.
			   # 이전 주차와 현재 주차의 비교와 이전 주차에 데이터를 추출하는 코드이나 효율적이지 않을 수 있음.
			 * */
			String[] accDataElem = StringUtils.tokenizeToStringArray(accDataStr, "_"); // 한 사이클 내 가속도 데이터 구분 [시간, 값], [시간, 값], ... 형태로 구분

			// 한 사이클 내 가속도 데이터에 대해 시간과 값을 구분하여 각각 데이터 열로 저장. 데이터 처리 프로세스를 위한 항목 추출
			for (int accDataElemIdx = 0; accDataElemIdx < accDataElem.length; accDataElemIdx++) {
				String[] accTimeValue = StringUtils.tokenizeToStringArray(accDataElem[accDataElemIdx], ",");
				DatColAccData datColAccDataOrigin = new DatColAccData();
				datColAccDataOrigin.setAccMesuredTime(Double.valueOf(accTimeValue[0]));
				datColAccDataOrigin.setAccMesuredValue(Double.valueOf(accTimeValue[1]));
				accDataMapOriginDu.put(accDataElemIdx, datColAccDataOrigin);

				DatColAccData datColAccData = new DatColAccData();
				datColAccData.setAccMesuredTime(Double.valueOf(accTimeValue[0]));
				datColAccData.setAccMesuredValue(roundDoubleValue(Double.valueOf(accTimeValue[1]), 0));
				rawAccDataMapDu.put(accDataElemIdx, datColAccData);

			}
// 2023-04-04, Mason adds Code.
// Everything after that code has been changed.

			Map<Integer, Map<String, Double>> accDataMapOrigin = new HashMap<>();
			Map<Integer, Map<String, Double>> rawAccDataMap = new HashMap<>();



			for (int accDataElemIdx = 0; accDataElemIdx < accDataElem.length; accDataElemIdx++) {
				DatColAccData accData = rawAccDataMapDu.get(accDataElemIdx);
				Map<String, Double> AccDataOrigin = new HashMap<>();
				double iniTime = accData.getAccMesuredTime();
				double iniValue = accData.getAccMesuredValue();

				AccDataOrigin.put("AccMesuredTime", iniTime);

				AccDataOrigin.put("AccMesuredValue", iniValue);

				accDataMapOrigin.put(accDataElemIdx, AccDataOrigin);
				rawAccDataMap.put(accDataElemIdx, AccDataOrigin);
				//needtodebug
			}
			// assuming the key value is stored in a variable called 'key'



			double accDataMaxValue = 0;
			for (int idx = 0; idx < accDataMapOrigin.size(); idx++) {
				Map<String, Double> valueMap = accDataMapOrigin.get(idx);
				double value = valueMap.get("AccMesuredValue");
				if (value > accDataMaxValue) {
					accDataMaxValue = value;
				}
			}

			double accDatMinValue = 100;
			for (int idx = 0; idx < accDataMapOrigin.size(); idx++) {
				Map<String, Double> valueMap = accDataMapOrigin.get(idx);
				double value = valueMap.get("AccMesuredValue");
				if (value < accDatMinValue) {
					accDatMinValue = value;
				}
			}
			int mapSize = rawAccDataMap.size();
			int mapIdx = 0;
			Map<Integer, Map<String, Double>> accDataMap = new HashMap<>();


			if (mapSize == 0) {
				accDataMap = accDataMapOrigin;
			} else {
				for (int idx = 0; idx < mapSize; idx++) {

					if (idx == 0) {
						Map<String, Double> accData = new HashMap<>();

						accData.put("AccMesuredTime", Math.round(rawAccDataMap.get(idx).get("AccMesuredTime")*100.0)/100.0);
						accData.put("AccMesuredValue", Math.round(rawAccDataMap.get(idx).get("AccMesuredValue")*100.0)/100.0);
						accDataMap.put(mapIdx, accData);
						mapIdx++;
						continue;
					} else {
						double accTime1 = Math.round(rawAccDataMap.get(idx-1).get("AccMesuredTime")*100.0)/100.0;
						double accValue1 = Math.round(rawAccDataMap.get(idx-1).get("AccMesuredValue")*1000.0)/1000.0;
						double accTime2 = Math.round(rawAccDataMap.get(idx).get("AccMesuredTime")*100.0)/100.0;
						double accValue2 = Math.round(rawAccDataMap.get(idx).get("AccMesuredValue")*1000.0)/1000.0;
						if (accTime2 - accTime1 >= 0.06) {
							Map<String, Double> accData = new HashMap<>();

							accData.put("AccMesuredTime", accTime2 -0.03);
							accData.put("AccMesuredValue", accDatMinValue);
							accDataMap.put(mapIdx, accData);
							mapIdx++;  // 2023-04-09 Required data is missing due to the absence of that code.

							Map<String, Double> accData1 = new HashMap<>();
							accData1.put("AccMesuredTime", accTime2);
							accData1.put("AccMesuredValue", accValue2);
							accDataMap.put(mapIdx, accData1);
							mapIdx++;
						} else {
							Map<String, Double> accData = new HashMap<>();

							accData.put("AccMesuredTime", accTime2);
							accData.put("AccMesuredValue", accValue2);
							accDataMap.put(mapIdx, accData);
							mapIdx++;
						}
					}
				}
			}



//needtodebug accdataMap 1

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> accData = accDataMap.get(idx);
				double value = accData.get("AccMesuredValue");
				double normValue = Math.round((value - accDatMinValue)/(accDataMaxValue - accDatMinValue)*1000.0)/1000.0;
				accData.put("NormAccValue", normValue);
				accDataMap.put(idx, accData);

			}




//needtodebug accdatamap 2
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> accDataBefore = accDataMap.get(idx-1);
				Map<String, Double> accData = accDataMap.get(idx);

				if (idx == 0) {

					double normValue = accData.get("NormAccValue");
					double gradientValue = Math.round((normValue/0.03)*1000.0)/1000.0;  // 2023-04-09 FistGradient has 3 digits.
					accData.put("FirstGradient", gradientValue);
					accDataMap.put(idx, accData);

				} else {


					double normValue1 = accDataBefore.get("NormAccValue");
					double normValue2 = accData.get("NormAccValue");
					double accTime1 = accDataBefore.get("AccMesuredTime");
					double accTime2 = accData.get("AccMesuredTime");
					double gradientValue = Math.round(((normValue2 - normValue1)/(accTime2 - accTime1))*1000000000.0)/1000000000.0;
					accData.put("FirstGradient", gradientValue);
					accDataMap.put(idx, accData);

				}
			}



//needtodebug aaccdatamap 3


			double maxGradient1 = -1000000000;
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> valueMap = accDataMap.get(idx);
				double value = valueMap.get("FirstGradient");
				if (value > maxGradient1) {
					maxGradient1 = value;
				}
			}

			double minxGradient1 = 1000000000;
			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> valueMap = accDataMap.get(idx);
				double value = valueMap.get("FirstGradient");
				if (value < minxGradient1) {
					minxGradient1 = value;
				}
			}

			double maxGradient =  Math.round(maxGradient1*10000000000.0)/10000000000.0;

			double minxGradient =  Math.round(minxGradient1*10000000000.0)/10000000000.0; // Gradient




			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> accData = accDataMap.get(idx);
				double valueGrad = accData.get("FirstGradient");
				double normGrad = Math.round((valueGrad - minxGradient)/(maxGradient - minxGradient)*1000000000.0)/1000000000.0;
				accData.put("NormFirstGradient", normGrad);
				accDataMap.put(idx, accData);
			}

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				if (idx < accDataMap.size()-1) {
					Map<String, Double> accData = accDataMap.get(idx);
					Map<String, Double> accDataAfter = accDataMap.get(idx+1);
					double time1 = accDataAfter.get("AccMesuredTime");
					double time2 = accData.get("AccMesuredTime");
					double timeDiff = Math.round((time1-time2)*100.0)/100.0;
					accData.put("TimeDiff", timeDiff);
					accDataMap.put(idx, accData);
				} else {
					Map<String, Double> accData = accDataMap.get(idx);
					double timeDiff = 1.00;
					accData.put("TimeDiff", timeDiff);
					accDataMap.put(idx, accData);
				}
			}

			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> accData = accDataMap.get(idx);
				double normalAcc = accData.get("NormAccValue");
				double normalGrad = accData.get("NormFirstGradient");
				double normalMultiple = Math.round((normalAcc*normalGrad)*1000000000.0)/1000000000.0;
				accData.put("NormMultiple", normalMultiple);
				accDataMap.put(idx, accData);
			}

//needtodebug accdatamap4

			ArrayList<Double> medianArr = new ArrayList<>(accDataMap.size());


			for (int idx = 0; idx < accDataMap.size(); idx++) {
				Map<String, Double> accData = accDataMap.get(idx);
				double normTmp = accData.get("NormMultiple");
				medianArr.add(normTmp);
			}

			medianArr.sort(Comparator.naturalOrder());



			double normMedianMulti;
			int n = medianArr.size();
			if (n % 2 == 0) {
				normMedianMulti = (medianArr.get((n/2)-1) + medianArr.get(n/2))/ 2.0;
			} else {
				normMedianMulti = medianArr.get((n/2));

			}


			accDataMap.entrySet()
					.removeIf(entry -> (entry.getValue().get("NormMultiple") < normMedianMulti && entry.getValue().get("TimeDiff") <= 0.1));
			if(ObjectUtils.isEmpty(accDataMap)) continue;
			int maxKey = Collections.max(accDataMap.keySet());

			Map<Integer, Map<String, Double>> accDataMap1 = new HashMap<>();

			int idxx = 0;
			for (int idx = 0; idx < maxKey+1; idx++) {
				Map<String, Double> accData = accDataMap.get(idx);
				if (accData != null) {
					accDataMap1.put(idxx, accData);
					idxx = idxx+1 ;
				}
			}

			accDataMap= accDataMap1;


//needtodebug accdataamp
			if (accDataMap.size() == 0) {
				continue;
			} else if (accDataMap.size() == 1) {
				Map<String, Double> accData = accDataMap.get(0);
				accData.put("TimeDiff", 1.0);
				accDataMap.put(0, accData);
			} else {
				for (int idx = 0; idx < accDataMap.size(); idx++) {
					if (idx < accDataMap.size()-1) {
						Map<String, Double> accData = accDataMap.get(idx);
						Map<String, Double> accDataAfter = accDataMap.get(idx+1);

						double time1 = accDataAfter.get("AccMesuredTime");
						double time2 = accData.get("AccMesuredTime");
						double timeDiff = Math.round((time1-time2)*100.0)/100.0;
						accData.put("TimeDiff", timeDiff);
						accDataMap.put(idx, accData);
					} else {
						Map<String, Double> accData = accDataMap.get(idx);
						double timeDiff = 1.00;
						accData.put("TimeDiff", timeDiff);
						accDataMap.put(idx, accData);
					}
				}

//needtodebug accdatamp

			}
			Map<Integer, Map<String, Double>> previousGpAccDataMap = new HashMap<>(accDataMap);  // 2023-04-09 Added code to make the accDataMap a new Map rather than concatenate it.
//			LinkedHashMap<Integer, DatColAccData> processPeakDataMap = new LinkedHashMap<>();


//needtodebug previousGpaccdatamap

			accDataMap.entrySet()
					.removeIf(entry -> (entry.getValue().get("TimeDiff") < 0.5));
			if(ObjectUtils.isEmpty(accDataMap)) continue;
			int maxSize = Collections.max(accDataMap.keySet());


			Map<Integer, Map<String, Double>> accDataMapTwo = new HashMap<>();
			int MapIdxx= 0;

			for (int idx = 0; idx < maxSize+1; idx++) {
				Map<String, Double> accData = accDataMap.get(idx);

				if (accData != null) {
					accDataMapTwo.put(MapIdxx, accData);
					MapIdxx = MapIdxx+1 ;
				}
			}
			accDataMap= accDataMapTwo;

//needtodebug accdatamap

			if (accDataMap.size() < 1 ) {
				continue;
			} else {

				double fistTimeDiff = accDataMap.get(0).get("AccMesuredTime");
				previousGpAccDataMap.entrySet()
						.removeIf(entry -> (entry.getValue().get("AccMesuredTime") >= fistTimeDiff));



				if (previousGpAccDataMap.size() == 0) {
					accDataMap.entrySet()
							.removeIf(entry -> (entry.getValue().get("AccMesuredTime") <= fistTimeDiff));

				} else {
					if(ObjectUtils.isEmpty(previousGpAccDataMap)) continue;
					int maxSizeOne = Collections.max(previousGpAccDataMap.keySet());

					Map<Integer, Map<String, Double>> previousGpAccDataMapOne = new HashMap<>();

					int PreIdx= 0;
					for (int idx = 0; idx < maxSizeOne+1; idx++) {
						Map<String, Double> accData = previousGpAccDataMap.get(idx);
						if (accData != null) {
							previousGpAccDataMapOne.put(PreIdx, accData);
							PreIdx = PreIdx+1 ;  // 2023-04-09 MapIdx is error, PreIdx is correct
						}
					}


					previousGpAccDataMap = previousGpAccDataMapOne;




					//needtodebug previousgpaccdatamap
					double maxAccValue = -10000;
					for (int idx = 0; idx < previousGpAccDataMap.size(); idx++) {
						Map<String, Double> accData = previousGpAccDataMap.get(idx);
						double value = accData.get("AccMesuredValue");
						if (value > maxAccValue) {
							maxAccValue = value;
						}
					}

					double finalMaxAccValue = maxAccValue;


					previousGpAccDataMap.entrySet()
							.removeIf(entry -> (entry.getValue().get("AccMesuredValue") != finalMaxAccValue));
					if(ObjectUtils.isEmpty(previousGpAccDataMap)) continue;
					int maxSizeTwo = Collections.max(previousGpAccDataMap.keySet());

					Map<Integer, Map<String, Double>> previousGpAccDataMapTwo = new HashMap<>();

					int TwoIdx= 0;
					for (int idx = 0; idx < maxSizeTwo+1; idx++) {
						Map<String, Double> accData = previousGpAccDataMap.get(idx);
						if (accData != null) {
							previousGpAccDataMapTwo.put(TwoIdx, accData);
							TwoIdx = TwoIdx+1 ;
						}
					}
					double maxAccValueTime = previousGpAccDataMapTwo.get(0).get("AccMesuredTime");

					if (fistTimeDiff - maxAccValueTime < 0.15) {
						accDataMap.entrySet()
								.removeIf(entry -> (entry.getValue().get("AccMesuredTime") <= fistTimeDiff));
					}
				}

				Map<Integer, Map<String, Double>> accDataMapThe = new HashMap<>();

				if(ObjectUtils.isEmpty(accDataMap)) continue;
				int maxSizeThe = Collections.max(accDataMap.keySet());

				int theIdx= 0;
				for (int idx = 0; idx < maxSizeThe+1; idx++) {
					Map<String, Double> accData = accDataMap.get(idx);
					if (accData != null) {
						accDataMapThe.put(theIdx, accData);
						theIdx = theIdx+1 ;
					}
				}


				accDataMap = accDataMapThe;

//needtodebug accdatamp
				for (int idx = 0; idx < accDataMap.size(); idx++) {
					Map<String, Double> accData = accDataMap.get(idx);
					double labelTmp = idx+1;
					accData.put("FilterLabel", labelTmp);
					accDataMap.put(idx, accData);
				}


				int labelIdx = 0;
				if (accDataMap.size() == 0) {
					for (int targetIdx = 0; targetIdx < accDataMapOrigin.size(); targetIdx++) {
						Map<String, Double> labeledAccData = accDataMapOrigin.get(targetIdx);

						labeledAccData.put("FilterLabel", 0.0);
						accDataMapOrigin.put(labelIdx++, labeledAccData);
					}
				} else {
					for (int idx = 0; idx < accDataMap.size(); idx++) {
						Map<String, Double> accData = accDataMap.get(idx);
						double label = accData.get("FilterLabel") - 1.0;
						double time2 = accData.get("AccMesuredTime");
						if (idx == 0) {
							for (int targetIdx = 0; targetIdx < accDataMapOrigin.size(); targetIdx++) {
								double targetTime = accDataMapOrigin.get(targetIdx).get("AccMesuredTime");
								if (targetTime <= time2-0.15) {
									Map<String, Double> labeledAccData = accDataMapOrigin.get(targetIdx);
									labeledAccData.put("FilterLabel", label);
									accDataMapOrigin.put(labelIdx++, labeledAccData);

								} else if (targetTime > time2-0.15 && targetTime <= time2) {
									Map<String, Double> labeledAccData = accDataMapOrigin.get(targetIdx);
									labeledAccData.put("FilterLabel", label+1);
									accDataMapOrigin.put(labelIdx++, labeledAccData);
								}
							}
						} else {
							Map<String, Double> accDataBefore = accDataMap.get(idx-1);
							double label1 = accData.get("FilterLabel"); // 2023-04-09

							double time1 = accDataBefore.get("AccMesuredTime");
							for (int targetIdx = 0; targetIdx < accDataMapOrigin.size(); targetIdx++) {
								double targetTime = accDataMapOrigin.get(targetIdx).get("AccMesuredTime");
								if (targetTime > time1 && targetTime <= time2) {
									Map<String, Double> labeledAccData = accDataMapOrigin.get(targetIdx);
									labeledAccData.put("FilterLabel", label1); // 2023-04-09
									accDataMapOrigin.put(labelIdx++, labeledAccData);
								}
							}
						}
					}
				}

				double lastAccLabel = accDataMap.get(accDataMap.size() - 1).get("FilterLabel");
				double lastAccTime = accDataMap.get(accDataMap.size() - 1).get("AccMesuredTime");

				for (int targetIdx = 0; targetIdx < accDataMapOrigin.size(); targetIdx++) {
					double targetTime = accDataMapOrigin.get(targetIdx).get("AccMesuredTime");
					if (targetTime > lastAccTime) {
						Map<String, Double> labeledAccData = accDataMapOrigin.get(targetIdx);
						labeledAccData.put("FilterLabel", lastAccLabel);
						accDataMapOrigin.put(labelIdx++, labeledAccData);
					}
				}

//needtodebug accdatamaporigin

				accDataMapOrigin.entrySet()
						.removeIf(entry -> (entry.getValue().get("FilterLabel") <= 0.0));
				if(ObjectUtils.isEmpty(accDataMap)) continue;
				int maxSizeFor = Collections.max(accDataMapOrigin.keySet());


				Map<Integer, Map<String, Double>> accDataMapOriginOne = new HashMap<>();
				int ForIdx= 0;

				for (int idx = 0; idx < maxSizeFor+1; idx++) {
					Map<String, Double> accData = accDataMapOrigin.get(idx);

					if (accData != null) {
						accDataMapOriginOne.put(ForIdx, accData);
						ForIdx = ForIdx+1 ;
					}
				}
				accDataMapOrigin= accDataMapOriginOne;

//needtodebug accdatamaporigin
				ArrayList<Double> aac = new ArrayList<>(accDataMapOrigin.size());


				Collection<Map<String, Double>> allInnerMaps = accDataMapOrigin.values();

				List<Double> valuesForKey1 = allInnerMaps.stream()
						.filter(map -> map.containsKey("FilterLabel"))
						.map(map -> map.get("FilterLabel"))
						.collect(Collectors.toList());

				ArrayList<Double> labelType1 = new ArrayList<>(valuesForKey1);

				Set<Double> uniqueValues = new HashSet<>(valuesForKey1);
				ArrayList<Double> labelType = new ArrayList<>(uniqueValues);
				Collections.sort(labelType);


				int processPeakIdx = 0;





				for (int idx = 0; idx < labelType.size(); idx++) {
					double tarLabel = labelType.get(idx);


					List<Double> accMesuredValues = accDataMapOrigin.values().stream()
							.filter(map -> map.get("FilterLabel").equals(tarLabel)) // FilterLabel이 1인 데이터 필터링
							.map(map -> map.get("AccMesuredValue"))
							.collect(Collectors.toList());
					double maxValue = Collections.max(accMesuredValues);

					Map<Integer, Map<String, Double>> tmpDataMap = new HashMap<>(accDataMapOrigin);
					tmpDataMap.entrySet()
							.removeIf(entry -> (entry.getValue().get("FilterLabel") != tarLabel));



					Map<Integer, Map<String, Double>> tmpDataMapOne = new HashMap<>();
					if(ObjectUtils.isEmpty(tmpDataMap)) continue;
					int maxSizeFiv = Collections.max(tmpDataMap.keySet());

					int fivIdx= 0;

					for (int idxs = 0; idxs < maxSizeFiv+1; idxs++) {
						Map<String, Double> accData = tmpDataMap.get(idxs);

						if (accData != null) {
							tmpDataMapOne.put(fivIdx, accData);
							fivIdx = fivIdx+1 ;
						}
					}
					tmpDataMap= tmpDataMapOne;




					Optional<Double> firstAccMesuredTime = tmpDataMap.values().stream()
							.filter(map -> map.get("AccMesuredValue").equals(maxValue))
							.map(map -> map.get("AccMesuredTime"))
							.findFirst();

					double time = firstAccMesuredTime.get();

					double maxValueRe = Math.round(maxValue*1.0)/1.0;

					Map<String, Double> labeledData = new HashMap<>();;

					labeledData.put("ProcessTime", time);
					labeledData.put("ProcessValue", maxValueRe);
					processPeakDataMap.put(processPeakIdx++, labeledData);
//needtodebug processpeakdatamap
				}								
			}

			// 2023.04.21 Mickey Park 
			// 0.2초 이하 데이터 제거
			processPeakDataMap.entrySet() //
						.removeIf(entry -> (entry.getValue().get("ProcessTime") <= 0.2));
			
			
// 2023-04-04 Mason code ended.


			/*
			#=======================================================================================================================
			#Step 17. calculate the process information. Using Mickey's code as itself.
			#=======================================================================================================================
			* */
			Collection<Map<String, Double>> values = processPeakDataMap.values();
			List<Map<String, Double>> list = new ArrayList<>(values);

			double injectionTime;
			double packingTime;
			double coolingTime;
			double packingPressure;
			double injectionPressure;

			int processPeakSize = list.size();



			if (processPeakSize <= 1) {
				injectionTime = 0.0;
				packingTime = 0.0;
				coolingTime = 0.0;
				injectionPressure = 0.0;
				packingPressure = 0;
			} else if (processPeakSize == 2) {
				injectionTime = list.get(1).get("ProcessTime") - list.get(0).get("ProcessTime");
				packingTime = 0.0;
				coolingTime = 0.0;
				injectionPressure = 0.0;
				packingPressure = 0.0;
			} else if (processPeakSize == 3) {
				injectionTime = 0.0;
				packingTime = list.get(1).get("ProcessTime") - list.get(0).get("ProcessTime");
				coolingTime = list.get(2).get("ProcessTime") - list.get(1).get("ProcessTime");
				injectionPressure = 0.0;
				packingPressure = list.get(1).get("ProcessValue");
			} else if (processPeakSize == 4) {
				injectionTime = list.get(1).get("ProcessTime") - list.get(0).get("ProcessTime");
				packingTime = list.get(2).get("ProcessTime") - list.get(1).get("ProcessTime");
				coolingTime = list.get(3).get("ProcessTime") - list.get(2).get("ProcessTime");
				packingPressure = list.get(2).get("ProcessValue");
				injectionPressure = list.get(1).get("ProcessValue") + packingPressure;
			} else {
				injectionTime = list.get(1).get("ProcessTime") - list.get(0).get("ProcessTime");

				double packingStartTime = list.get(1).get("ProcessTime");
				double packingEndTime = list.get(2).get("ProcessTime");
				packingTime = packingEndTime - packingStartTime;

				double moldOpenTime = list.get(list.size() - 1).get("ProcessTime");
				coolingTime = moldOpenTime - packingEndTime;

				int orderPackingEnd = 2;
				double candiPackingTime;
				double candiProcessTime;
				double candiCoolingTime;
				for (int idx = 3; idx < processPeakSize - 1; idx++) {
					packingEndTime = list.get(idx).get("ProcessTime");

					candiPackingTime = packingEndTime - packingStartTime;
					candiProcessTime = candiPackingTime + injectionTime;
					candiCoolingTime = moldOpenTime - packingEndTime;
					if (candiProcessTime < (candiCoolingTime * 0.65)) {
						packingTime = list.get(idx).get("ProcessTime") - packingStartTime;
						coolingTime = moldOpenTime - list.get(idx).get("ProcessTime");
						orderPackingEnd = idx;
					}
				}

				packingPressure = list.get(orderPackingEnd).get("ProcessValue");
				injectionPressure = packingPressure + list.get(1).get("ProcessValue");
			}

			injectionTime = Math.round((injectionTime)*10.0)/10.0;
			packingTime = Math.round((packingTime)*10.0)/10.0;
			coolingTime = Math.round((coolingTime)*10.0)/10.0;
			packingPressure = Math.round((packingPressure)*1.0)/1.0;
			injectionPressure = Math.round((injectionPressure)*1.0)/1.0;

			// 2023-04-09 I also replaced the code to get the injection time, ... etc. with my own code.
			// 2023-04-09 I also made sure to output all of the resulting values as a Double, which is the same type as the value that is connected to processInfoOut in Mickey's code.

			DatColProcessInfoOut processInfoOut = new DatColProcessInfoOut();
			processInfoOut.setCounterId(sensorId);
			processInfoOut.setWeek(weekNum);
			processInfoOut.setMeasuredTime(accRecordStartTimeStr);
			processInfoOut.setInjectionTime(injectionTime);
			processInfoOut.setPackingTime(packingTime);
			processInfoOut.setCoolingTime(coolingTime);
			processInfoOut.setInjectionPressureIndex(injectionPressure);
			processInfoOut.setPackingPressureIndex(packingPressure);
			out.add(processInfoOut);

		} // Loop End
		return out;
	}


	private LinkedHashMap<Integer, DatColAccData> rearrangeIndex(LinkedHashMap<Integer, DatColAccData> accDataMap) {
		LinkedHashMap<Integer, DatColAccData> out = new LinkedHashMap<>();

		AtomicInteger counter = new AtomicInteger(0);
		accDataMap.entrySet().stream() //
				.sorted(Map.Entry.comparingByKey()) //
				.forEach(entry -> out.put(counter.getAndIncrement(), entry.getValue()));

		return out;
	}

	private ArrayList<Integer> getFilteredLabelTypeList(LinkedHashMap<Integer, DatColAccData> map) {
		return (ArrayList<Integer>) map.values().stream() //
				.map(DatColAccData::getFilterLabel) //
				.distinct() //
				.collect(Collectors.toList());
	}

	private ArrayList<Integer> getAccLabelTypeList(LinkedHashMap<Integer, DatColAccData> map) {
		return (ArrayList<Integer>) map.values().stream() //
				.map(DatColAccData::getAccLabel) //
				.distinct() //
				.collect(Collectors.toList());
	}

	// TO-DO : digit을 Dynamic 하게 변경
	public double roundDoubleValue(double value, int digit) {

		String roundResultStr = String.format("%.0f", value);
		if (digit == 1) {
			roundResultStr = String.format("%.1f", value);
		} else if (digit == 2) {
			roundResultStr = String.format("%.2f", value);
		} else if (digit == 3) {
			roundResultStr = String.format("%.3f", value);
		} else if (digit == 4) {
			roundResultStr = String.format("%.4f", value);
		} else if (digit == 5) {
			roundResultStr = String.format("%.5f", value);
		} else if (digit == 6) {
			roundResultStr = String.format("%.6f", value);
		} else if (digit == 7) {
			roundResultStr = String.format("%.7f", value);
		} else if (digit == 8) {
			roundResultStr = String.format("%.8f", value);
		} else if (digit == 9) {
			roundResultStr = String.format("%.9f", value);
		} else if (digit == 10) {
			roundResultStr = String.format("%.10f", value);
		} else if (digit == 0) {
			roundResultStr = String.format("%.0f", value);
		}

		return Double.valueOf(roundResultStr);
	}

	// Print Map
	private void printMap(String subject, LinkedHashMap<Integer, DatColAccData> map) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println(subject + ", [Data] = " + mapper.writeValueAsString(map) + " [Map Size] = " + map.size());
		} catch (JsonProcessingException e) {
			System.out.println(e.getMessage());
		}
	}
}
