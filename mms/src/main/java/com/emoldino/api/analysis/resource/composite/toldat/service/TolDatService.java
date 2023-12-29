package com.emoldino.api.analysis.resource.composite.toldat.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColProcessInfoOut;
import com.emoldino.api.analysis.resource.composite.datcol.service.accelerationdata.DatColAccelerationDataService;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut.MldDatItem;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut2;
import com.emoldino.api.analysis.resource.composite.toldat.util.TolDatUtils;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.mold.MoldRepository;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.OutsideUnit;
import saleson.model.Mold;

@Service
@Transactional
public class TolDatService {
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.DATE);

	public TolDatGetOut get(TolDatGetIn input, TimeSetting timeSetting, Pageable pageable) {
		TolDatUtils.adjustAndCheck(input, timeSetting, TIME_SCALE_SUPPORTED);

		if (input.isMock()) {
			List<MldDatItem> list = getMockData();
			TolDatGetOut output = new TolDatGetOut(list, pageable, list.size());
			return output;
		}

		Mold mold = BeanUtils.get(MoldRepository.class).findById(input.getMoldId()).orElse(null);
		if (mold == null) {
			return new TolDatGetOut(Collections.emptyList(), pageable, 0);
		}
		String sensorCode = mold.getCounterCode();
		if (ObjectUtils.isEmpty(sensorCode)) {
			return new TolDatGetOut(Collections.emptyList(), pageable, 0);
		}

		String zoneId = LocationUtils.getZoneIdByLocation(mold.getLocation());
		Pair<Instant, Instant> timeRange = TolDatUtils.getTimeRange(timeSetting, zoneId);
		String fromTimeStr = DateUtils2.format(timeRange.getFirst(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
		String toTimeStr = DateUtils2.format(timeRange.getSecond(), DatePattern.yyyyMMddHHmmss, Zone.GMT);

		List<MldDatItem> list = new ArrayList<>();
		int[] total = { 0 };
		boolean wactEnabled = "WACT".equals(OptionUtils.getFieldValue(ConfigCategory.OPTIMAL_CYCLE_TIME, "strategy", "WACT"));
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		int startIndex = pageNumber * pageSize;
		int endIndex = startIndex + pageSize - 1;
		DataUtils.runEach(DataCounterRepository.class, //
				new BooleanBuilder()//
						.and(Q.dataCounter.counterId.eq(sensorCode))//
						.and(Q.dataCounter.shotEndTime.isNotNull())//
						.and(Q.dataCounter.shotEndTime.isNotEmpty())//
						.and(Q.dataCounter.shotEndTime.goe(fromTimeStr))//
						.and(Q.dataCounter.shotStartTime.isNotNull())//
						.and(Q.dataCounter.shotStartTime.isNotEmpty())//
						.and(Q.dataCounter.shotStartTime.lt(toTimeStr)), //
				Sort.by("shotStartTime"), 100, true, //
				dataCnt -> {
					if (ObjectUtils.isEmpty(dataCnt.getCycleTimes())) {
						return;
					}

					boolean over = total[0] > endIndex;
					int i[] = { total[0] };
					total[0] += dataCnt.getCycleTimes().size();
					if (over) {
						return;
					}

					// Get Accleration Data
					ArrayList<DatColProcessInfoOut> accDataList = new ArrayList<>();
					try {
						accDataList = BeanUtils.get(DatColAccelerationDataService.class).getAccData2(dataCnt.getDataId());
					} catch (Exception e) {
						accDataList = null;
					}

					Instant shotStartInst = DateUtils2.toInstant(dataCnt.getShotStartTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
					Instant instant = shotStartInst;
					List<String> temps = null;

					for (CycleTime cycleTime : dataCnt.getCycleTimes()) {
						String time = DateUtils2.format(instant, DatePattern.yyyyMMddHHmmss, zoneId);
						long passedSec = instant.getEpochSecond() - shotStartInst.getEpochSecond();
						int _10mins = passedSec == 0 ? 0 : ValueUtils.toInteger(Math.max(1L, (passedSec) / 600), 0);

						double ct = cycleTime == null ? 0d : cycleTime.getCycleTime().doubleValue();
						if (ct > 0) {
							instant = instant.plus(Duration.ofMillis(ValueUtils.toLong(ct * 1000, 0L)));
						}

						if (i[0] < startIndex) {
							i[0]++;
							continue;
						} else if (i[0] > endIndex) {
							return;
						}
						i[0]++;

						if (temps == null && !ObjectUtils.isEmpty(dataCnt.getTemperature())) {
							temps = new ArrayList<>();
							String tempStr = dataCnt.getTemperature();
							for (int j = 0; j < tempStr.length(); j += 4) {
								String tempVal = tempStr.substring(j, j + 4);
								if (!ValueUtils.isNumber(tempVal)) {
									continue;
								}
								temps.add(tempVal);
							}
						}

						Double approvedCt = ValueUtils.toDouble(mold.getContractedCycleTimeSeconds(), 0.0);
						Double wact = MoldUtils.getWact(input.getMoldId(), mold.getContractedCycleTime(), time.substring(0, 6)) / 10.0;
						Double actualCt = cycleTime.getCycleTime() == null ? 0.0 : cycleTime.getCycleTime().doubleValue();
						Double baseCt = wactEnabled && wact != null && wact > 0.0 ? wact : approvedCt;

						double ctLl2;
						double ctLl1;
						double ctUl1;
						double ctUl2;
						{
							double cycleTimeL1 = mold.getCycleTimeLimit1Unit() == null || OutsideUnit.PERCENTAGE.equals(mold.getCycleTimeLimit1Unit()) ? //
									baseCt * mold.getCycleTimeLimit1() * 0.01//
									: (double) mold.getCycleTimeLimit1();
							double cycleTimeL2 = mold.getCycleTimeLimit2Unit() == null || OutsideUnit.PERCENTAGE.equals(mold.getCycleTimeLimit2Unit()) ? //
									baseCt * mold.getCycleTimeLimit2() * 0.01//
									: (double) mold.getCycleTimeLimit2();
							ctLl2 = baseCt - cycleTimeL2;
							ctLl1 = baseCt - cycleTimeL1;
							ctUl1 = baseCt + cycleTimeL1;
							ctUl2 = baseCt + cycleTimeL2;
						}

						Double temperature;
						if (ObjectUtils.isEmpty(temps)) {
							temperature = 0.0;
						} else {
							String temp = temps.size() > _10mins ? temps.get(_10mins) : temps.get(temps.size() - 1);
							temperature = ValueUtils.toDouble(temp, 0.0) / 10.0;
						}

						MldDatItem item = new MldDatItem(time, approvedCt, wact, actualCt, ctLl2, ctLl1, ctUl1, ctUl2, temperature);

						if (!ObjectUtils.isEmpty(accDataList)) {
							DatColProcessInfoOut accData = applyAccData(accDataList, DateUtils2.format(instant, DatePattern.yyyyMMddHHmmss, zoneId));
							item.setInjectionTime(accData.getInjectionTime());
							item.setPackingTime(accData.getPackingTime());
							item.setCoolingTime(accData.getCoolingTime());
						} else {
							item.setInjectionTime(0.0);
							item.setPackingTime(0.0);
							item.setCoolingTime(0.0);
						}

						list.add(item);
					}
				});

		TolDatGetOut output = new TolDatGetOut(list, pageable, total[0]);
		return output;
	}

	private TolDatGetOut2 get2(TolDatGetIn input, TimeSetting timeSetting, Pageable pageable) {
		TolDatGetOut reqout = get(input, timeSetting, pageable);
		TolDatGetOut2 output = new TolDatGetOut2(reqout);
		output.setMoldCode(getMoldCode(input));
		output.setDate(getDate(timeSetting));
		return output;
	}

	public void export(TolDatGetIn input, TimeSetting timeSetting, BatchIn batchin, HttpServletResponse response) {
		TolDatUtils.adjust(input, timeSetting);
		if (input.getMoldId() != null) {
			String fileName = getMoldCode(input) + "_" + getDate(timeSetting);
			DatExpUtils.exportByJxls(//
					"MLD_DAT", //
					pageable -> BeanUtils.get(TolDatService.class).get2(input, timeSetting, pageable), //
					100, Sort.unsorted(), //
					fileName, response//
			);
		} else if (MasterFilterMode.SELECTED.equals(batchin.getSelectionMode()) && ObjectUtils.isEmpty(batchin.getSelectedIds())) {
			ValueUtils.assertNotEmpty(input.getMoldId(), "moldId");
		} else if (MasterFilterMode.SELECTED.equals(batchin.getSelectionMode()) && batchin.getSelectedIds().size() == 1) {
			input.setMoldId(batchin.getSelectedIds().get(0));
			export(input, timeSetting, batchin, response);
		} else {
			JPQLQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)//
					.select(Q.mold.id, Q.mold.equipmentCode)//
					.from(Q.mold);
			QueryUtils.applyMoldFilter(query, new HashSet<>(), "COMMON");

			BooleanBuilder filter = new BooleanBuilder()//
					.and(Q.mold.counter.equipmentCode.startsWith("EMA"));
			QueryUtils.applyBatchFilter(filter, batchin, Q.mold.id);
			query.where(filter);

			try (//
					ByteArrayOutputStream bos = new ByteArrayOutputStream(); //
					ZipOutputStream zos = new ZipOutputStream(bos); //
					OutputStream os = response.getOutputStream();//
			) {
				String[] firstFileName = { null };
				int[] otherSize = { 0 };
				DataUtils.runBatch(query, 1000, true, mold -> {
					Long moldId = mold.get(0, Long.class);
					String moldCode = mold.get(1, String.class);
					input.setMoldId(moldId);
					String fileName = moldCode + "_" + getDate(timeSetting);
					if (firstFileName[0] == null) {
						firstFileName[0] = fileName;
					} else {
						otherSize[0]++;
					}
					DatExpUtils.appendByJxls(//
							"MLD_DAT", //
							pageable -> BeanUtils.get(TolDatService.class).get2(input, timeSetting, pageable), //
							100, Sort.unsorted(), //
							fileName, zos//
					);
				});
				zos.close();
				String fileName = firstFileName[0] == null ? "Empty.zip" : (firstFileName[0] + "_AndOther" + otherSize[0] + "Toolings.zip");
				HttpUtils.respondFile(bos, fileName, "application/octet-stream", response);
			} catch (IOException e) {
				throw ValueUtils.toAe(e, "ZIP_FAIL");
			}
		}
	}

	private String getMoldCode(TolDatGetIn input) {
		String moldCode = (String) ThreadUtils.getProp("MldDatService.moldCode." + input.getMoldId(), //
				() -> BeanUtils.get(MoldRepository.class).findById(input.getMoldId()).orElse(new Mold()).getEquipmentCode()//
		);
		return moldCode;
	}

	private String getDate(TimeSetting timeSetting) {
		String date = (String) ThreadUtils.getProp("MldDatService.date." + timeSetting.getTimeValue(), //
				() -> DateUtils2.toOtherPattern(timeSetting.getTimeValue(), DatePattern.yyyyMMdd, DatePattern.yyyy_MM_dd)//
		);
		return date;
	}

	private DatColProcessInfoOut applyAccData(ArrayList<DatColProcessInfoOut> accDataList, String targetAccMesuredTime) {
		int low = 0;
		int high = accDataList.size() - 1;
		long time = Long.valueOf(targetAccMesuredTime);
		while (low <= high) {
			int mid = (low + high) / 2;
			long midAccMesuredTime = Long.valueOf(accDataList.get(mid).getMeasuredTime());

			if (time > midAccMesuredTime) {
				low = mid + 1;
			} else if (time < midAccMesuredTime) {
				high = mid - 1;
			} else {
				return accDataList.get(mid);
			}
		}

		if (low >= accDataList.size()) {
			return accDataList.get(accDataList.size() - 1);
		}
		return accDataList.get(low);
	}

	private List<MldDatItem> getMockData() {
		List<MldDatItem> list = Arrays.asList(//
				new MldDatItem("20221118051021", 44d, 42.2, 46.7, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118051108", 44d, 42.2, 89.5, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118051238", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118051320", 44d, 42.2, 75.5, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118051435", 44d, 42.2, 0.0, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118053606", 44d, 42.2, 12.7, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118053619", 44d, 42.2, 880.9, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055100", 44d, 42.2, 45.7, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055145", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055227", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055309", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055352", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055434", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055516", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118055558", 44d, 42.2, 0.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062134", 44d, 42.2, 46.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062220", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062302", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062344", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062426", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062509", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062551", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062633", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062715", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062757", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062839", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118062921", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063003", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063046", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063128", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063210", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063252", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063334", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063416", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063458", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063540", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063622", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063705", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063747", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063829", 44d, 42.2, 41.9, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063911", 44d, 42.2, 42.3, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118063953", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064035", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064117", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064159", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064241", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064323", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064405", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064448", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064530", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064612", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064654", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064736", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064818", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064900", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118064942", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065024", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065106", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065149", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065231", 44d, 42.2, 42.3, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065313", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065355", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065437", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065519", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065601", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065643", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065725", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065808", 44d, 42.2, 41.9, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065849", 44d, 42.2, 42.3, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118065932", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070014", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070056", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070138", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070220", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070302", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070344", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070426", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070509", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070551", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070633", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070715", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070757", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070839", 44d, 42.2, 41.9, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118070921", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071003", 44d, 42.2, 42.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071045", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071127", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071209", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071251", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071333", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071416", 44d, 42.2, 41.9, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071458", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071540", 44d, 42.2, 42.2, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071622", 44d, 42.2, 42.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118071704", 44d, 42.2, 0.0, 40.0, 42.0, 46.0, 48.0, 40.8), //
				new MldDatItem("20221118082841", 44d, 42.2, 15.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118082856", 44d, 42.2, 36.0, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118082932", 44d, 42.2, 57.1, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118083029", 44d, 42.2, 326.4, 40.0, 42.0, 46.0, 48.0, 40.0), //
				new MldDatItem("20221118083556", 44d, 42.2, 361.9, 40.0, 42.0, 46.0, 48.0, 39.8), //
				new MldDatItem("20221118084157", 44d, 42.2, 16.9, 40.0, 42.0, 46.0, 48.0, 39.8), //
				new MldDatItem("20221118084214", 44d, 42.2, 16.0, 40.0, 42.0, 46.0, 48.0, 39.8), //
				new MldDatItem("20221118084230", 44d, 42.2, 10.4, 40.0, 42.0, 46.0, 48.0, 39.8), //
				new MldDatItem("20221118084241", 44d, 42.2, 11.1, 40.0, 42.0, 46.0, 48.0, 39.8), //
				new MldDatItem("20221118084252", 44d, 42.2, 27.5, 40.0, 42.0, 46.0, 48.0, 39.8), //
				new MldDatItem("20221118084319", 44d, 42.2, 0.0, 40.0, 42.0, 46.0, 48.0, 39.0)//
		);

		return list;
	}

}
