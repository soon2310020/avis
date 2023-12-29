package saleson.api.mold;

import static saleson.common.config.Const.WIDTH_WARNING;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.util.MoldDataUtils;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut;
import com.emoldino.api.analysis.resource.composite.toldat.service.TolDatService;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.ActiveStatus;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.QueryUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.mold.payload.ExportPayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.common.config.Const;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.PeriodType;
import saleson.common.enumeration.RangeType;
import saleson.common.util.DateUtils;
import saleson.common.util.ExcelCommonUtils;
import saleson.common.util.ExcelUtils;
import saleson.common.util.FileUtils;
import saleson.common.util.StringUtils;
import saleson.dto.common.TwoObject;
import saleson.dto.exports.dynamic.MoldItem;
import saleson.dto.exports.dynamic.MoldRow;
import saleson.model.Mold;
import saleson.model.data.AvgCavityStatisticsData;
import saleson.model.data.ChartData;
import saleson.service.util.NumberUtils;

@Slf4j
@Service
public class DynamicExportService {
    @Autowired
    MoldRepository moldRepository;

    @Lazy
    @Autowired
    MoldService moldService;
    @Autowired
    ExcelUtils excelUtils;

    @Autowired
    private TolDatService service;

/*
    @Value("${dynamic.export.oneToolingPerFile:false}")
    Boolean exportOneToolingPerFile;
*/
    Boolean exportOneToolingPerFile=true;

    //export dynamic
    private String getDataTypeLabel(ExportPayload payload) {
        String lastLabel = "cycletime";
        if (payload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
            lastLabel = "cycletime";
        } else if (payload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT)) {
            lastLabel = "shotcount";
        }
        //todo;
        return lastLabel;
    }

    public String getExportDynamicFileName(ExportPayload payload) {
        String timeRange = getTimeRangeLabel(payload, false);
        String frequency = getDataFrequencyTitle(payload.getFrequency()).toLowerCase();
/*
        String dataTypeLabel = getDataTypeLabel(payload);
        return timeRange + "_" + frequency + "_" + dataTypeLabel + ".xlsx";
*/
        return timeRange + "_" + frequency + ".xlsx";
    }
    public String getExportDynamicFileNamePerTooling(ExportPayload payload,String moldCode) {
        return replaceSpecialCharacter(moldCode)+"_"+getExportDynamicFileName(payload);
    }

    public String getExportDynamicSheetName(ExportPayload payload, Instant time) {
        String timeRange = getTimeRangeLabel(payload, false);
        if(RangeType.CUSTOM_RANGE.equals(payload.getRangeType())){
            timeRange=timeRange.replace("From ","");
        }
        String frequency = getDataFrequencyTitle(payload.getFrequency()).toLowerCase();
//        String dataTypeLabel = getDataTypeLabel(payload);
        if (payload.getFrequency().equals(DateViewType.HOUR)) {
            String timeSheet = "";
            if (time != null) {
                timeSheet = DateUtils.getDate(time, DateUtils.YYYY_MM_DD_DATE_FORMAT);
                if (RangeType.CUSTOM_RANGE.equals(payload.getRangeType())) {
                    timeRange = timeSheet;
                } else {
                    timeRange += "_" + timeSheet;
                }
            }
        }
        String sheetName = payload.isToolingForm() ? timeRange : timeRange + "_" + frequency;
        return sheetName;
    }

    public static List<ChartData> updateCycleTimeDataList(List<ChartData> chartDataList, Mold mold) {
        double baseCycleTime = mold.getContractedCycleTimeToCalculation() / 10.0; // 기준 사이클 타임 (contracted? ) --> 초로 계산
        List<ChartData> data =  updateCycleTimeDataList(chartDataList,mold,baseCycleTime,null).getFirst();
        data.stream().sorted().collect(Collectors.toList());
        return data;
    }

	public static Pair<List<ChartData>, ChartData> updateCycleTimeDataList(List<ChartData> chartDataList, Mold mold, double baseCycleTime,
			Map<String, AvgCavityStatisticsData> cavities) {

		// 3. Cycle Time 체크 (L1, L2)
//        double baseCycleTime = mold.getContractedCycleTime() / 10.0; // 기준 사이클 타임 (contracted? ) --> 초로 계산
		int limit1 = mold.getCycleTimeLimit1();
		int limit2 = mold.getCycleTimeLimit2();
		double cycleTimeL1 = mold.getCycleTimeLimit1Unit() == null || OutsideUnit.PERCENTAGE.equals(mold.getCycleTimeLimit1Unit()) ? baseCycleTime * limit1 * 0.01
				: (double) mold.getCycleTimeLimit1();
		double cycleTimeL2 = mold.getCycleTimeLimit2Unit() == null || OutsideUnit.PERCENTAGE.equals(mold.getCycleTimeLimit2Unit()) ? baseCycleTime * limit2 * 0.01
				: (double) mold.getCycleTimeLimit2();

		// 3-1. 기준 범위  ===== -L2 === -L1 === base === +L1 === +L2 =====
		double ctLl2 = baseCycleTime - cycleTimeL2;
		double ctLl1 = baseCycleTime - cycleTimeL1;
		double ctUl1 = baseCycleTime + cycleTimeL1;
		double ctUl2 = baseCycleTime + cycleTimeL2;

		for (ChartData chartData : chartDataList) {
			// L1, L2
			chartData.setCycleTimeMinusL1(ctLl1);
			chartData.setCycleTimeMinusL2(ctLl2);
			chartData.setCycleTimePlusL1(ctUl1);
			chartData.setCycleTimePlusL2(ctUl2);

			chartData.setContractedCycleTime(baseCycleTime); // 초로 계산 (100ms -> sec)

			double cycleTime = chartData.getCycleTime();
			if (MoldDataUtils.isCtOutOfL2(cycleTime, ctLl2, ctUl2)) {
				chartData.setCycleTimeWithin(0.0);
				chartData.setCycleTimeL1(0.0);
				chartData.setCycleTimeL2(cycleTime);

			} else if (MoldDataUtils.isCtOutOfL1(cycleTime, ctLl2, ctLl1, ctUl1, ctUl2)) {
				chartData.setCycleTimeWithin(0.0);
				chartData.setCycleTimeL1(cycleTime);
				chartData.setCycleTimeL2(0.0);
			} else {
				chartData.setCycleTimeWithin(cycleTime);
				chartData.setCycleTimeL1(0.0);
				chartData.setCycleTimeL2(0.0);
			}
			if (cavities != null) {
				Integer totalCavity = cavities.containsKey(chartData.getTitle()) ? cavities.get(chartData.getTitle()).getTotalCavity() : 0;
				if (chartData.getData() == 0) {
					chartData.setAvgCavities(NumberUtils.roundOffNumber(0.0));
				} else {
					Double avgCavities = (double) totalCavity / (double) chartData.getData();
					chartData.setAvgCavities(NumberUtils.roundOffNumber(avgCavities));
				}
			}
		}

		/*

		List<ChartData> data = chartDataList.stream().sorted().collect(Collectors.toList());

		List<ChartData> chartDataList1 = data;

		return chartDataList1;
		*/
		ChartData defaultCycleTimeValue = ChartData.builder().cycleTimeMinusL1(ctLl1).cycleTimeMinusL2(ctLl2).cycleTimePlusL1(ctUl1).cycleTimePlusL2(ctUl2).build();
		return Pair.of(chartDataList, defaultCycleTimeValue);
	}

/*
    private List<ChartData> buildHourStatisticsDetailsExport(String compressedData, String code, String title) {
        List<ChartData> list = moldService.buildHourStatisticsDetails(compressedData);
        list.stream().forEach(item -> {
            item.setMoldCode(code);
            item.setTitle(title);
            item.setIndexInHour(list.indexOf(item));
        });
        return list;
    }
    private List<ChartData> buildHourStatisticsTempDetailsExport(List<ChartData> rawData, String code, String title) {
        List<ChartData> list = moldService.buildHourStatisticsTempDetails(rawData,title);
        list.stream().forEach(item -> {
            item.setMoldCode(code);
            item.setTitle(title);
            item.setIndexInHour(list.indexOf(item));
        });
        return list;
    }
*/

    private MoldItem convertChartToMoldItem(String moldCode, ChartData chartData, ExportPayload exportPayload) {
        MoldItem moldItem = new MoldItem();
        moldItem.setMoldCode(moldCode);
        moldItem.setIndexInHour(chartData.getIndexInHour());
        String shots = chartData.getData() != null ? chartData.getData() + " shot" : "";
        if (chartData.getData() > 1) shots += "s";
        moldItem.setShortCount(shots);
        if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
            moldItem.setCt(chartData.getCycleTime() != null ? chartData.getCycleTime() + "s" : "");
            if (chartData.getCycleTimeL2() > 0)
                moldItem.setCycleTimeStatus(CycleTimeStatus.OUTSIDE_L2);
            else if (chartData.getCycleTimeL1() > 0)
                moldItem.setCycleTimeStatus(CycleTimeStatus.OUTSIDE_L1);
            else if (chartData.getCycleTimeWithin() > 0)
                moldItem.setCycleTimeStatus(CycleTimeStatus.WITHIN_TOLERANCE);
        }
        moldItem.setTitle(chartData.getTitle());
        updateHeaderTable(moldItem, exportPayload);
        return moldItem;
    }
    public static MoldItem convertChartToMoldItemNew(Mold mold, ChartData chartData, ExportPayload exportPayload) {
        String moldCode = mold.getEquipmentCode();
        MoldItem moldItem = new MoldItem();
        moldItem.setMoldCode(moldCode);
        moldItem.setIndexInHour(chartData.getIndexInHour());
        String shots = chartData.getData() != null ? chartData.getData().toString() : "";

        moldItem.setShortCount(shots);
        if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
            moldItem.setCt(chartData.getCycleTime() != null ? chartData.getCycleTime() + "" : "");
            if (chartData.getCycleTimeL2() > 0)
                moldItem.setCycleTimeStatus(CycleTimeStatus.OUTSIDE_L2);
            else if (chartData.getCycleTimeL1() > 0)
                moldItem.setCycleTimeStatus(CycleTimeStatus.OUTSIDE_L1);
            else if (chartData.getCycleTimeWithin() > 0)
                moldItem.setCycleTimeStatus(CycleTimeStatus.WITHIN_TOLERANCE);
        }
        if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME)) {

            moldItem.setUptimeHour(chartData.getUptimeHour()!=null?chartData.getUptimeHour().toString():"");
            if(chartData.getUptimeHour()!=null){
                PeriodType type = PeriodType.DAILY;
                String date = chartData.getTitle();
                if (DateViewType.WEEK.equals(exportPayload.getFrequency())) {
                    type = PeriodType.WEEKLY;
                } else if (DateViewType.MONTH.equals(exportPayload.getFrequency())) {
                    type = PeriodType.MONTHLY;
                    date = (chartData.getTitle() + "01").substring(0, 8);
                }
                EfficiencyStatus efficiencyStatus
                        = MoldService.getEfficiencyStatus(mold, chartData.getUptime(), date, type).getFirst();
                moldItem.setUptimeStatus(efficiencyStatus);
            }
        }
        if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE)) {
            moldItem.setTemperature(chartData.getTav()!=null?(chartData.getTav().doubleValue()/10)+"":"");
        }

        moldItem.setTitle(chartData.getTitle());
        updateHeaderTable(moldItem, exportPayload);
        //for new template
//        moldItem.setGroupHeader(moldItem.getChildHeader());
        return moldItem;
    }

    private static void updateHeaderTable(final MoldItem moldItem, ExportPayload exportPayload) {
        DateViewType dateViewType = exportPayload.getFrequency();
        RangeType rangeType = exportPayload.getRangeType();
        if (StringUtils.isEmpty(moldItem.getTitle()) || dateViewType == null) return;
        Instant minDate = DateUtils.getInstant(exportPayload.getFromDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT);
        Instant maxDate = DateUtils.getInstant(exportPayload.getToDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT);

        String time = moldItem.getTitle();
        Calendar calendar = DateUtils.getCalendar();
        int year = Integer.valueOf(time.substring(0, 4));
        String childHeader = "";
        String groupHeader = "Actual CT";
        String dataType = exportPayload.getDataTypes().get(0);
        switch (dataType) {
            case Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT:
                groupHeader = "Shot Count";
                break;
        }
        moldItem.setGroupHeaderView(groupHeader);
        Instant timeValue = null;
        if (DateViewType.MONTH.equals(dateViewType)) {
            int month = Integer.valueOf(time.substring(4, 6));
            calendar.set(year, month - 1, 1);
//			calendar.set(Calendar.HOUR_OF_DAY,0);
            if (Arrays.asList(RangeType.YEARLY).contains(rangeType)) {
                groupHeader = groupHeader + " (Year " + year + ")";
            } else if (Arrays.asList(RangeType.MONTHLY).contains(rangeType)) {
                groupHeader = groupHeader + " (" + DateUtils.getDate(calendar.toInstant(), DateUtils.MMMM) + ")";
            }
            childHeader = DateUtils.getDate(calendar.toInstant(), DateUtils.MMM);
            timeValue = calendar.toInstant();
            if (Arrays.asList(RangeType.CUSTOM_RANGE).contains(rangeType)) {
                childHeader += " (" + getRangeDateInColByString(timeValue, dateViewType, minDate, maxDate) + ")";
            }
        } else if (DateViewType.WEEK.equals(dateViewType)) {
            int weekValue = Integer.valueOf(time.substring(4, 6));
            calendar.setWeekDate(year, weekValue, Calendar.SUNDAY);
            timeValue = calendar.toInstant();
            if (Arrays.asList(RangeType.YEARLY).contains(rangeType))
                groupHeader = groupHeader + " (Year " + year + ")";
            else if (Arrays.asList(RangeType.MONTHLY).contains(rangeType))
                groupHeader = groupHeader + " (" + DateUtils.getDate(calendar.toInstant(), DateUtils.MMMM) + ")";
            else if (Arrays.asList(RangeType.WEEKLY).contains(rangeType))
                groupHeader = groupHeader + " (Week " + weekValue + ")";

            childHeader = "Week " + weekValue;
            if (Arrays.asList(RangeType.CUSTOM_RANGE, RangeType.MONTHLY, RangeType.YEARLY).contains(rangeType)) {
                childHeader += " (" + getRangeDateInColByString(timeValue, dateViewType, minDate, maxDate) + ")";
            }
        } else if (DateViewType.DAY.equals(dateViewType)) {
            int month = Integer.valueOf(time.substring(4, 6));
            int day = Integer.valueOf(time.substring(6, 8));
            calendar.set(year, month - 1, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            timeValue = calendar.toInstant();

            if (Arrays.asList(RangeType.YEARLY).contains(rangeType))
                groupHeader = groupHeader + " (Year " + year + ")";
            else if (Arrays.asList(RangeType.MONTHLY).contains(rangeType))
                groupHeader = groupHeader + " (" + DateUtils.getDate(calendar.toInstant(), DateUtils.MMMM) + ")";
            else if (Arrays.asList(RangeType.WEEKLY).contains(rangeType))
                groupHeader = groupHeader + " (Week " + DateUtils.getWeekOfYear(time.substring(0, 8) + "000000") + ")";


            childHeader = DateUtils.getDate(calendar.toInstant(), DateUtils.YYYY_MM_dd);

        } else if (DateViewType.HOUR.equals(dateViewType)) {
            int month = Integer.valueOf(time.substring(4, 6));
            int day = Integer.valueOf(time.substring(6, 8));
            int hour = Integer.valueOf(time.substring(8, 10));
            calendar.set(year, month - 1, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            timeValue = calendar.toInstant();
            childHeader = DateUtils.getDate(timeValue, DateUtils.HH_mm);
            groupHeader = groupHeader + " (" + DateUtils.getDate(timeValue, DateUtils.YYYY_MM_dd) + ")";
//            calendar.add(Calendar.HOUR, 1);
            Instant endTimeValue=calendar.toInstant();
            String endTimeHeader = DateUtils.getDate(endTimeValue, DateUtils.HH_mm);
            childHeader = childHeader + " --> " + endTimeHeader;
            if(exportPayload.isToolingForm()){
                groupHeader = DateUtils.getDate(timeValue, DateUtils.YYYY_MM_dd);
                childHeader = DateUtils.getDate(endTimeValue, DateUtils.HH);
//                if(childHeader.equals("00")) childHeader="24";

            }

        }

        moldItem.setChildHeader(childHeader);
        moldItem.setGroupHeader(groupHeader);
        moldItem.setTimeSort(timeValue);
    }

    private static String getRangeDateInColByString(Instant startDate, DateViewType dateViewType
            , Instant minDate, Instant maxDate) {
        TwoObject<Instant, Instant> range = getRangeDateByDateViewType(startDate, dateViewType, minDate, maxDate);
        if (range.getLeft().equals(range.getRight()) || DateUtils.getDate(range.getLeft(), DateUtils.YYYY_MM_dd).equals(DateUtils.getDate(range.getRight(), DateUtils.YYYY_MM_dd))) {
            return DateUtils.getDate(range.getLeft(), DateUtils.YYYY_MM_dd);
        }
        return getFromToString(range.getLeft(), range.getRight());
    }

    public static TwoObject<Instant, Instant> getRangeDateByDateViewType(Instant startDate, DateViewType dateViewType
            , Instant minDate, Instant maxDate) {
        Instant endDate = Instant.now();
        Calendar calendar = DateUtils.getCalendar();
        calendar.setTimeInMillis(startDate.toEpochMilli());
        if (DateViewType.WEEK.equals(dateViewType)) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            endDate = calendar.toInstant();
        } else if (DateViewType.MONTH.equals(dateViewType)) {
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 0);
            endDate = calendar.toInstant();
        }
        if (minDate != null && startDate.isBefore(minDate)) startDate = minDate;
        if (maxDate != null && endDate.isAfter(maxDate)) endDate = maxDate;

        return new TwoObject<>(startDate, endDate);
    }
/*

    @Deprecated
    public List<MoldRow> getMoldRowData(ExportPayload exportPayload, List<Mold> moldList) {
        List<MoldRow> moldRows = new ArrayList<>();
//		Map<Long, List<ChartData>> mapDataListResult = new HashMap<>();
        Map<Long, Mold> moldMap = new HashMap<>();
        if (moldList != null) moldList.stream().forEach(m -> moldMap.put(m.getId(), m));

        List<ChartData> chartDataListTotal = new ArrayList<>();
        if (exportPayload.getFrequency().equals(DateViewType.HOUR)
                && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
            ChartPayload payload = new ChartPayload();
            payload.addChartDataType(ChartDataType.CYCLE_TIME_ANALYSIS);
            payload.setDateViewType(exportPayload.getFrequency());
            payload.setStartDate(exportPayload.getFromDate());
            payload.setEndDate(exportPayload.getToDate());

            List<ChartData> rawData = moldRepository.findHourDetailsStatistics(payload, exportPayload.getIds());
//            Set<String> existDataSet = new HashSet<>();
            if (rawData != null && rawData.size() > 0) {
                rawData.stream().forEach(rw -> {
//                    if (existDataSet.contains(rw.getMoldCode())) return;
//                    existDataSet.add(rw.getMoldCode());
                    String compressedData = rw.getCompressedData();
                    String title = rw.getTitle();
                    chartDataListTotal.addAll(buildHourStatisticsDetailsExport(compressedData, rw.getMoldCode(), title));
                });
            }

        } else {
            List<ChartData> rawData = moldRepository.findDataExportDynamic(exportPayload, moldList);
            chartDataListTotal.addAll(rawData);
        }

        Map<Long, List<ChartData>> mapDataList = new HashMap<>();
        chartDataListTotal.stream().forEach(c -> {
            Long moldId = Long.valueOf(c.getMoldCode());
            List<ChartData> chartDataList;
            if (mapDataList.containsKey(moldId)) {
                chartDataList = mapDataList.get(moldId);
            } else {
                chartDataList = new ArrayList<>();
                mapDataList.put(moldId, chartDataList);
            }
            chartDataList.add(c);
        });

        moldList.stream().forEach(mold -> {
//        });
//        moldMap.keySet().forEach(k -> {
//            Mold mold = moldMap.get(k);
            Long k = mold.getId();
            MoldRow moldRow = new MoldRow();
            moldRow.setMoldCode(mold.getEquipmentCode());
            double baseCycleTime = mold.getContractedCycleTimeToCalculation() / 10.0;
            moldRow.setApprovedCT(baseCycleTime + "s");
            List<MoldItem> moldItemList = new ArrayList<>();
            moldRow.setMoldItemList(moldItemList);
            moldRow.setAvaliable(EquipmentStatus.AVAILABLE.equals(mold.getEquipmentStatus()));
            if (mold.getCounter() != null && mold.getCounter().getEquipmentCode() != null && mold.getCounter().getEquipmentCode().startsWith("NCM")) {
                moldRow.setNewCounter(true);
            } else{
                moldRow.setNewCounter(false);
                if (mold.getCounter() != null)
                    moldRow.setCounterCode(mold.getCounter().getEquipmentCode());
            }

            List<ChartData> chartDataList = mapDataList.containsKey(k) ? mapDataList.get(k) : new ArrayList<>();
            List<ChartData> chartDataList1 = chartDataList;
            if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
                chartDataList1 = updateCycleTimeDataList(chartDataList, mold);
            }
//			mapDataListResult.put(k,chartDataList1);
            chartDataList1.stream().forEach(chartData -> {
                MoldItem moldItem = convertChartToMoldItem(mold.getEquipmentCode(), chartData, exportPayload);
                moldItemList.add(moldItem);
            });
            moldRows.add(moldRow);
        });
        return moldRows;
    }
*/
    public List<MoldRow> getMoldRowDataNew(ExportPayload exportPayload, List<Mold> moldList) {
        List<MoldRow> moldRows = new ArrayList<>();
        Map<Long, Mold> moldMap = new HashMap<>();
        if (moldList != null) moldList.stream().forEach(m -> moldMap.put(m.getId(), m));

        List<ChartData> chartDataListFull = new ArrayList<>();
        List<ChartData> chartShotDataListTotal = new ArrayList<>();
        List<ChartData> chartCycleDataListTotal = new ArrayList<>();
        List<ChartData> chartUptimeDataListTotal = new ArrayList<>();//not hourly
        List<ChartData> chartTempDataListTotal = new ArrayList<>();//only hourly

        if(exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT)){
            List<ChartData> rawData = moldRepository.findDataExportDynamicNew(ChartDataType.QUANTITY,exportPayload, moldList);
            chartShotDataListTotal.addAll(rawData);
        }
        if(exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)){
            List<ChartData> rawData = moldRepository.findDataExportDynamicNew(ChartDataType.CYCLE_TIME,exportPayload, moldList);
            chartCycleDataListTotal.addAll(rawData);
        }
        if (exportPayload.getFrequency().equals(DateViewType.HOUR)
                && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE)) {
            List<ChartData> rawData = moldRepository.findDataExportDynamicNew(ChartDataType.TEMPERATURE_ANALYSIS,exportPayload, moldList);
            chartTempDataListTotal.addAll(rawData);
        }

        if (!exportPayload.getFrequency().equals(DateViewType.HOUR)
                && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME)) {
            List<ChartData> rawData = moldRepository.findDataExportDynamicNew(ChartDataType.UPTIME,exportPayload, moldList);
            chartUptimeDataListTotal.addAll(rawData);
        }

        Map<String,ChartData> chartDataMapFull= new HashMap<>();
        chartCycleDataListTotal.stream().forEach(d->chartDataMapFull.put(getKeyChartObject(d),d));

        chartDataListFull.addAll(chartCycleDataListTotal);

        chartShotDataListTotal.stream().forEach(d->{
            String key=getKeyChartObject(d);
            if(chartDataMapFull.containsKey(key)){
            ChartData chartData=chartDataMapFull.get(key);
                chartData.setData(d.getData());
            }else {
                chartDataListFull.add(d);
                chartDataMapFull.put(key,d);
            }

        });
        chartUptimeDataListTotal.stream().forEach(d->{
            String key=getKeyChartObject(d);
            if(chartDataMapFull.containsKey(key)){
            ChartData chartData=chartDataMapFull.get(key);
             chartData.setUptime(d.getUptime());
            }else {
                chartDataListFull.add(d);
                chartDataMapFull.put(key,d);
            }

        });
        chartTempDataListTotal.stream().forEach(d->{
            String key=getKeyChartObject(d);
            if(chartDataMapFull.containsKey(key)){
            ChartData chartData=chartDataMapFull.get(key);
             chartData.setTav(d.getTav());
             chartData.setThi(d.getThi());
             chartData.setTlo(d.getTlo());
            }else {
                chartDataListFull.add(d);
                chartDataMapFull.put(key,d);
            }

        });

        Map<Long, List<ChartData>> mapDataList = new HashMap<>();
        chartDataListFull.stream().forEach(c -> {
            Long moldId = Long.valueOf(c.getMoldCode());
            List<ChartData> chartDataList;
            if (mapDataList.containsKey(moldId)) {
                chartDataList = mapDataList.get(moldId);
            } else {
                chartDataList = new ArrayList<>();
                mapDataList.put(moldId, chartDataList);
            }
            chartDataList.add(c);
        });

        moldList.stream().forEach(mold -> {
            Long k = mold.getId();
            MoldRow moldRow = new MoldRow();
            moldRow.setMoldId(mold.getId());
            moldRow.setMoldCode(mold.getEquipmentCode());
            double baseCycleTime = mold.getContractedCycleTimeToCalculation() / 10.0;
            moldRow.setApprovedCT(baseCycleTime + "");
            moldRow.setUptimeTarget((mold.getUptimeTarget() != null ? mold.getUptimeTarget() : 90) + "%");
            List<MoldItem> moldItemList = new ArrayList<>();
            moldRow.setMoldItemList(moldItemList);
            moldRow.setAvaliable(EquipmentStatus.AVAILABLE.equals(mold.getEquipmentStatus()));
            if (mold.getCounter() != null && mold.getCounter().getEquipmentCode() != null
                && (mold.getCounter().getEquipmentCode().startsWith("NCM") || mold.getCounter().getEquipmentCode().startsWith("EMA"))
            ) {
                moldRow.setNewCounter(true);
            } else{
                moldRow.setNewCounter(false);
//                if (mold.getCounter() != null)
//                    moldRow.setCounterCode(mold.getCounter().getEquipmentCode());
            }
            if (mold.getCounter() != null)
                moldRow.setCounterCode(mold.getCounter().getEquipmentCode());

            List<ChartData> chartDataList = mapDataList.containsKey(k) ? mapDataList.get(k) : new ArrayList<>();
            List<ChartData> chartDataList1 = chartDataList;
            if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
                chartDataList1 = updateCycleTimeDataList(chartDataList, mold);
            }
            chartDataList1.stream().forEach(chartData -> {
                MoldItem moldItem = convertChartToMoldItemNew(mold, chartData, exportPayload);
                moldItemList.add(moldItem);
            });
            moldRows.add(moldRow);
        });
        return moldRows;
    }
    public static String getKeyChartObject(ChartData chartData){
        String key="";
        key=chartData.getMoldCode()+"-"+chartData.getTitle()+"-"+chartData.getIndexInHour();
        return key;
    }

    public void exportExcelDynamic(HttpServletResponse response,
                                   ExportPayload exportPayload
            , Pageable pageable) {
/*
        List<Mold> moldList;
        if (exportPayload.getIds() == null || exportPayload.getIds().size() == 0) {
            moldList = moldRepository.findAllByOrderByIdDesc();
        } else {
            if (pageable != null && pageable.getSort() != null) {
                Pageable pageableNew = Pageable.unpaged();
                if (pageable.getSort() != null) {
                    pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
                }
                MoldPayload payload = new MoldPayload();
                payload.setIds(exportPayload.getIds());
                Page<Mold> pageContent = moldService.findAll(payload.getPredicate(), pageableNew, false);
                moldList = pageContent.getContent();
            } else
                moldList = moldRepository.findByIdInOrderByIdDesc(exportPayload.getIds());
        }

        generateRageTimeFromTime(exportPayload);
        List<MoldRow> moldRowList = getMoldRowDataNew(exportPayload, moldList);
*/
        List<MoldRow> moldRowList = prepareDataForFile(exportPayload,pageable);
        log.info("Start make excel file for {} toolings",moldRowList.size());
        ByteArrayOutputStream byteArrayOutputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + getExportDynamicFileName(exportPayload));
            Path path = exportExcelDynamicMoldNew(moldRowList, exportPayload);
            outputStream.write(Files.readAllBytes(path));
            outputStream.close();
            Files.delete(path);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        return exportExcelDynamicMoldNew(moldRowList, exportPayload);
    }
    private List<MoldRow> prepareDataForFile(final ExportPayload exportPayload, Pageable pageable){
        log.info("Start prepareDataForFile");
        List<Mold> moldList;
        if (exportPayload.getIds() == null || exportPayload.getIds().size() == 0) {
            moldList = moldRepository.findAllByOrderByIdDesc();
        } else {
            if (pageable != null && pageable.getSort() != null) {
                Pageable pageableNew = Pageable.unpaged();
                if (pageable.getSort() != null) {
                    pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
                }
                MoldPayload payload = new MoldPayload();
                payload.setIds(exportPayload.getIds());
                QueryUtils.includeDisabled(Q.mold);
                Page<Mold> pageContent = moldService.findAll(payload.getPredicate(), pageableNew, false,null, "COMMON", ActiveStatus.ENABLED);
                moldList = pageContent.getContent();
            } else
                moldList = moldRepository.findByIdInOrderByIdDesc(exportPayload.getIds());
        }

        generateRageTimeFromTime(exportPayload);
        List<MoldRow> moldRowList = getMoldRowDataNew(exportPayload, moldList);
        log.info("End prepareDataForFile {} molds",moldRowList.size());
        return moldRowList;
    }

    public static void generateRageTimeFromTime(ExportPayload exportPayload) {
        if (exportPayload.getRangeType().equals(RangeType.CUSTOM_RANGE) || StringUtils.isEmpty(exportPayload.getTime()))
            return;
        String time = exportPayload.getTime();
        Calendar calendar = DateUtils.getCalendar();
        int year = Integer.valueOf(time.substring(0, 4));
        Instant fromDate = Instant.now();
        Instant toDate = Instant.now();
        if (exportPayload.getRangeType().equals(RangeType.WEEKLY)) {

            int weekValue = Integer.valueOf(time.substring(4,6));
            calendar.setWeekDate(year, weekValue, Calendar.SUNDAY);
            fromDate = calendar.toInstant();
            calendar.add(Calendar.DATE, 6);
            toDate = calendar.toInstant();
        } else if (exportPayload.getRangeType().equals(RangeType.MONTHLY)) {

            fromDate = DateUtils.getInstant(time + "01000000", DateUtils.DEFAULT_DATE_FORMAT);
            calendar.setTimeInMillis(fromDate.toEpochMilli());
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            toDate = calendar.toInstant();
        }
        if (exportPayload.getRangeType().equals(RangeType.YEARLY)) {
//            timeRange = "Year " + time;
            fromDate = DateUtils.getInstant(time + "0101000000", DateUtils.DEFAULT_DATE_FORMAT);
            calendar.setTimeInMillis(fromDate.toEpochMilli());
            calendar.add(Calendar.YEAR, 1);
            calendar.add(Calendar.DATE, -1);
            toDate = calendar.toInstant();
        }
        exportPayload.setFromDate(DateUtils.getDate(fromDate, DateUtils.YYYY_MM_DD_DATE_FORMAT));
        exportPayload.setToDate(DateUtils.getDate(toDate, DateUtils.YYYY_MM_DD_DATE_FORMAT));

    }

    public static String getFromToString(Instant start, Instant to) {
        return getFromToString(start,to,true);
    }
    public static String getFromToString(Instant start, Instant to,boolean isContent) {
        String format=DateUtils.YYYY_MM_DD_DATE_FORMAT;
        if(isContent) format=DateUtils.YYYY_MM_dd;
        return getFromToString(start,to,format);
    }
    private static String getFromToString(Instant start, Instant to,String format) {
        return "From " + DateUtils.getDate(start, format) + " to " + DateUtils.getDate(to, format);
    }

    private String getTimeRangeLabel(ExportPayload exportPayload, boolean isExportContent) {
        String timeRange = exportPayload.getTime();

        Calendar calendar = DateUtils.getCalendar();
        if (exportPayload.getRangeType().equals(RangeType.CUSTOM_RANGE)) {
            Instant from = DateUtils.getInstant(exportPayload.getFromDate() + "000000", DateUtils.DEFAULT_DATE_FORMAT);
            Instant to = DateUtils.getInstant(exportPayload.getToDate() + "000000", DateUtils.DEFAULT_DATE_FORMAT);
            timeRange = getFromToString(from, to,isExportContent);
        } else if (exportPayload.getRangeType().equals(RangeType.WEEKLY)) {

            String time = exportPayload.getTime();
            int year = Integer.parseInt(time.substring(0, 4));
            int weekValue = Integer.parseInt(time.substring(4, 6));
            timeRange = "Week " + weekValue;
            if (isExportContent) {
                calendar.setWeekDate(year, weekValue, Calendar.SUNDAY);
                Instant timeValue = calendar.toInstant();
                TwoObject<Instant, Instant> range = getRangeDateByDateViewType(timeValue, DateViewType.WEEK, null, null);
                timeRange += " (" + getFromToString(range.getLeft(), range.getRight()) + ")";
            }
        } else if (exportPayload.getRangeType().equals(RangeType.MONTHLY)) {

            String time = exportPayload.getTime();
            Instant date = DateUtils.getInstant(time + "01000000", DateUtils.DEFAULT_DATE_FORMAT);
            timeRange = DateUtils.getDate(date, DateUtils.MMMM);
            if (isExportContent) {
                TwoObject<Instant, Instant> range = getRangeDateByDateViewType(date, DateViewType.MONTH, null, null);
                timeRange += " (" + getFromToString(range.getLeft(), range.getRight()) + ")";
            }
        } else if (exportPayload.getRangeType().equals(RangeType.YEARLY)) {
            String time = exportPayload.getTime();
            timeRange = "Year " + time;
            if (isExportContent) {
                calendar.set(Integer.valueOf(time), 0, 1);
                Instant start = calendar.toInstant();
                calendar.set(Integer.valueOf(time) + 1, 0, 0);
                Instant end = calendar.toInstant();
                timeRange += " (" + getFromToString(start, end) + ")";
            }
        }

        return timeRange;
    }

    private String getDataFrequencyTitle(DateViewType viewType) {
        if (DateViewType.HOUR.equals(viewType)) {
            return "Hourly";
        }
        if (DateViewType.DAY.equals(viewType)) {
            return "Daily";
        }
        if (DateViewType.WEEK.equals(viewType)) {
            return "Weekly";
        }
        if (DateViewType.MONTH.equals(viewType)) {
            return "Monthly";
        }
        return viewType.getTitle();
    }

/*
    @Deprecated
    private ByteArrayOutputStream exportExcelDynamicMold(List<MoldRow> moldRowList, ExportPayload exportPayload) {
        InputStream fileTemplate = ExcelUtils.getFileTemplateExcel("dynamic/TemplateExportCycleTime.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            final boolean isViewHourly = exportPayload.getFrequency().equals(DateViewType.HOUR);
            if (!isViewHourly)
                workbook.setSheetName(0, getExportDynamicSheetName(exportPayload, null));
            int rowGroup = 3;
            int rowHeader = 4;
            int rowDataTemp = 5;

//            Default
            int indexOfBasicInformation = 0;
            int indexStyleL1 = 6;
            int indexStyleL2 = 9;

            int indexWarning = 1;
            final int[] indexDynamicData = {3};
            final boolean isCycleTimeExport = exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME);
            //style for lable
            CellStyle headerGroupStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowGroup, indexOfBasicInformation);
            CellStyle headerChildStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexOfBasicInformation);
            CellStyle valueStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowDataTemp, indexOfBasicInformation);
            CellStyle valueStyleL1 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL1);
            CellStyle valueStyleL2 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL2);
            CellStyle styleL1 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL1);
            CellStyle styleL2 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL2);
            CellStyle warningStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexWarning);
//            warningStyle.setWrapText(false);
            ExcelCommonUtils.setFullBorderStyle(valueStyleL1);
            ExcelCommonUtils.setFullBorderStyle(valueStyleL2);

            boolean isOneRowHeader = false;
            if (DateViewType.WEEK.equals(exportPayload.getFrequency()) && RangeType.WEEKLY.equals(exportPayload.getRangeType())
                    || DateViewType.MONTH.equals(exportPayload.getFrequency()) && RangeType.MONTHLY.equals(exportPayload.getRangeType())
            ) isOneRowHeader = true;

            if (isViewHourly || isOneRowHeader) {
                rowHeader = rowGroup;
                rowDataTemp = 4;
            }
            final int rowData = rowDataTemp;

            //make data header common
            String timeRange = getTimeRangeLabel(exportPayload, true);
            ExcelCommonUtils.getOrCreateCell(sheet, 0, 2).setCellValue(timeRange);
            ExcelCommonUtils.getOrCreateCell(sheet, 1, 2).setCellValue(getDataFrequencyTitle(exportPayload.getFrequency()));

            if (isCycleTimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, 0, 0, indexStyleL1, indexStyleL1 + 1, styleL1, "Outside L1");
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, 0, 0, indexStyleL2, indexStyleL2 + 1, styleL2, "Outside L2");
            } else {
                Cell c1 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL1 - 1);
                Cell c2 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL1);
                Cell c3 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL2 - 1);
                Cell c4 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL2);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c1);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c2);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c3);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c4);
            }

            // header column statistics
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, 0, 0, headerGroupStyle, "SEQ");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, 1, 1, headerGroupStyle, "Tooling ID");
            if (isCycleTimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, 2, 2, headerGroupStyle, "Approved CT");
            } else {
                indexDynamicData[0] = 2;
            }

            //build header list
            List<MoldItem> groupHeaderListTemp = new ArrayList<>();
            Map<String, List<MoldItem>> mapChildHeader = new HashMap<>();
            for (int i = 0; i < moldRowList.size(); i++) {
                MoldRow moldRow = moldRowList.get(i);
                if (moldRow.getMoldItemList() != null) {
                    moldRow.getMoldItemList().forEach(moldItem -> {
                        Set<String> headerSet = groupHeaderListTemp.stream().map(h -> h.getGroupHeader()).collect(Collectors.toSet());
                        if (!headerSet.contains(moldItem.getGroupHeader())) {
                            groupHeaderListTemp.add(moldItem);
                        }

                        List<MoldItem> childHeaderList;
                        if (!mapChildHeader.containsKey(moldItem.getGroupHeader())) {
                            childHeaderList = new ArrayList<>();
                            mapChildHeader.put(moldItem.getGroupHeader(), childHeaderList);
                        } else {
                            childHeaderList = mapChildHeader.get(moldItem.getGroupHeader());
                        }
                        Set<String> childHeaderSet = childHeaderList.stream().map(h -> h.getChildHeader()).collect(Collectors.toSet());
                        if (childHeaderSet.contains(moldItem.getChildHeader())) {
                            MoldItem old = childHeaderList.stream().filter(h -> h.getChildHeader().equalsIgnoreCase(moldItem.getChildHeader())).findFirst().orElse(null);
                            if (old.getMaxItemInHour() <= moldItem.getIndexInHour())
                                old.setMaxItemInHour(moldItem.getIndexInHour() + 1);
                        } else {
                            childHeaderList.add(moldItem);
                        }

                    });

                }
            }
            //sort column
            List<MoldItem> groupHeaderList = groupHeaderListTemp.stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < groupHeaderList.size(); i++) {
                MoldItem groupHeader = groupHeaderList.get(i);
                if (i == 0 || isViewHourly) groupHeader.setLastIndexCol(indexDynamicData[0]);
                else {
                    MoldItem groupHeaderBefore = groupHeaderList.get(i - 1);
                    groupHeader.setLastIndexCol(groupHeaderBefore.getLastIndexCol() + groupHeaderBefore.getNumColInGroup());
                }
                List<MoldItem> childHeaderList = mapChildHeader.get(groupHeader.getGroupHeader());
                childHeaderList = childHeaderList.stream().sorted().collect(Collectors.toList());
                mapChildHeader.put(groupHeader.getGroupHeader(), childHeaderList);

                int numColInGroup = childHeaderList.stream().map(c -> c.getMaxItemInHour()).mapToInt(Integer::intValue).sum();
                groupHeader.setNumColInGroup(numColInGroup);

            }

            //write header to file
            ExcelCommonUtils.getOrCreateRow(sheet, rowGroup).setHeight((short) -1);//auto resize
            ExcelCommonUtils.getOrCreateRow(sheet, rowHeader).setHeight((short) -1);//auto resize

            //build sheet for Hourly
            Map<String, Sheet> mapSheet = new HashMap<>();
            if (isViewHourly) {
                //for empty data
                if(groupHeaderList.isEmpty()) {
                    MoldItem groupHeader= new MoldItem();
                    groupHeader.setTitle(exportPayload.getFromDate()+"00");
                    groupHeader.setNumColInGroup(1);
                    groupHeader.setLastIndexCol(indexDynamicData[0]);
                    updateHeaderTable(groupHeader,exportPayload);
                    groupHeaderList.add(groupHeader);
                    mapChildHeader.put(groupHeader.getGroupHeader(), Arrays.asList(groupHeader));
                }

                for (int sheetIndex = 0; sheetIndex < groupHeaderList.size(); sheetIndex++) {
                    MoldItem groupHeader = groupHeaderList.get(sheetIndex);
                    Sheet sheetCopy = sheet;
                    String sheetName = getExportDynamicSheetName(exportPayload, groupHeader.getTimeSort());
                    if (sheetIndex == 0) {
                        workbook.setSheetName(0, sheetName);
                    } else {
                        sheetCopy = workbook.cloneSheet(0, sheetName);
                    }
                    mapSheet.put(groupHeader.getGroupHeader(), sheetCopy);
                }
            }
            //write header to file
            for (int i = 0; i < groupHeaderList.size(); i++) {
                MoldItem groupHeader = groupHeaderList.get(i);
                int startCol = groupHeader.getLastIndexCol();
                int endCol = startCol + groupHeader.getNumColInGroup() - 1;
                int indexColCurrent = startCol;//for set with in hour
                Sheet sheetWorking = sheet;
                if (isViewHourly)
                    sheetWorking = mapSheet.get(groupHeader.getGroupHeader());
                List<MoldItem> childHeaderList = mapChildHeader.get(groupHeader.getGroupHeader());
                for (int indexChild = 0; indexChild < childHeaderList.size(); indexChild++) {
                    MoldItem childHeader = childHeaderList.get(indexChild);
                    if (isViewHourly) {
                        for (int indexInHour = 0; indexInHour < childHeader.getMaxItemInHour(); indexInHour++) {
                            sheetWorking.setColumnWidth(indexColCurrent, 5000);
                            indexColCurrent++;
                        }
                    } else if (!isOneRowHeader) {
                        Cell subHearCell = ExcelCommonUtils.getOrCreateCell(sheetWorking, rowHeader, indexChild + startCol);
                        subHearCell.setCellStyle(headerChildStyle);
                        subHearCell.setCellValue(childHeader.getChildHeader());
                        sheetWorking.setColumnWidth(indexChild + startCol, 5000);
                    }
                }
                ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, rowGroup, rowGroup, startCol, endCol, headerGroupStyle, groupHeader.getGroupHeaderView());
            }

            int numRowPerMoldTemp = 2;
            if (isViewHourly) {
                numRowPerMoldTemp = 3;
            }
            if (!isCycleTimeExport) {
                numRowPerMoldTemp--;
            }
            final int numRowPerMold = numRowPerMoldTemp;
            //Export Data
            if (isViewHourly) {
                groupHeaderList.stream().forEach(groupHeader -> {
                    Sheet sheetWorking = mapSheet.get(groupHeader.getGroupHeader());
                    final int[] startRow = {rowData};
                    final int[] index = {1};
                    final int[] indexCell = {0};
                    //for resize
                    TwoObject<Boolean,Integer> existWarningAndColsize= new TwoObject<>(false,1);
                    moldRowList.stream().forEach(moldRow -> {
                        for (int num = 0; num < numRowPerMold; num++)
                            ExcelCommonUtils.getOrCreateRow(sheetWorking, startRow[0] + num).setHeight((short) -1);//auto resize height

                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                , valueStyle, String.valueOf(index[0]));
                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                , valueStyle, moldRow.getMoldCode());
                        if (isCycleTimeExport)
                            ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                    , valueStyle, moldRow.getApprovedCT());

                        List<MoldItem> childHearList = mapChildHeader.get(groupHeader.getGroupHeader());

                        boolean existsData=false;
                        if (!moldRow.isNewCounter())
                            existsData = childHearList.stream().filter(childHear -> {
                                List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                        .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                        .collect(Collectors.toList());
                                if (itemChildListValue != null && itemChildListValue.size() > 0) return true;
                                return false;
                            }).findAny().isPresent();

                        //check new counter
                        if (!moldRow.isNewCounter() && (!existsData || !StringUtils.isEmpty(moldRow.getCounterCode()))) {
                            final int totalColData = childHearList.stream().map(h -> h.getMaxItemInHour()).reduce(0, (a, b) -> a + b);
                            String message = "No data available because the tooling is not attached to any counter";
                            if (!StringUtils.isEmpty(moldRow.getCounterCode()) )
                                message = "No hourly data available because the tooling is attached to old generation counter (Sensor ID: " + moldRow.getCounterCode() + ")";
                            ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0] + (totalColData > 1 ? totalColData - 1 : 0)
                                    , warningStyle
                                    , message);
                            existWarningAndColsize.setLeft(true);
                            existWarningAndColsize.setRight(totalColData);
                        } else {
                            childHearList.forEach(childHear -> {
                                int startRowChild = startRow[0];

                                ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRowChild, startRowChild++, indexCell[0], indexCell[0] + (childHear.getMaxItemInHour() - 1)
                                        , headerChildStyle, childHear.getChildHeader());


                                    List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                            .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                            .collect(Collectors.toList());
                                    for (int indexVal = 0; indexVal < childHear.getMaxItemInHour(); indexVal++) {
                                        //init col with
//                                        if (startRowChild == rowData)
                                            sheetWorking.setColumnWidth(indexCell[0] + indexVal, 5000);

                                        String cycleTime = "";
                                        String shot = "";
                                        CellStyle cellStyle = valueStyle;
                                        if (itemChildListValue.size() > indexVal) {
                                            MoldItem item = itemChildListValue.get(indexVal);
                                            cycleTime = item.getCt();
                                            shot = item.getShortCount();
                                            if (CycleTimeStatus.OUTSIDE_L2.equals(item.getCycleTimeStatus())) {
                                                cellStyle = valueStyleL2;
                                            } else if (CycleTimeStatus.OUTSIDE_L1.equals(item.getCycleTimeStatus())) {
                                                cellStyle = valueStyleL1;
                                            }
                                        }
                                        int indexRowChild = 0;
                                        if (isCycleTimeExport)
                                            ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild + indexRowChild++, indexCell[0] + indexVal, cellStyle, cycleTime);
                                        ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild + indexRowChild, indexCell[0] + indexVal, valueStyle, shot);
                                    }
                                indexCell[0] += childHear.getMaxItemInHour();

                            });
                        }

                        indexCell[0] = 0;
                        startRow[0] += numRowPerMold;
                        index[0]++;
                    });
                    if(existWarningAndColsize.getLeft() && existWarningAndColsize.getRight()<3){
                        int colShow = isCycleTimeExport ? 3 : 2;
                        if (existWarningAndColsize.getRight().equals(2)) {
                            sheetWorking.setColumnWidth(colShow, 7500);
                            sheetWorking.setColumnWidth(colShow+1, 7500);
                        } else {
                            sheetWorking.autoSizeColumn(colShow, true);
                        }

                    }

                });
            } else {
                Sheet sheetWorking = sheet;
                final int[] startRow = {rowData};
                final int[] index = {1};
                final int[] indexCell = {0};
                moldRowList.stream().forEach(moldRow -> {
                for (int num = 0; num < numRowPerMold; num++)
                    ExcelCommonUtils.getOrCreateRow(sheetWorking, startRow[0] + num).setHeight((short) -1);//auto resize height

                ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                        , valueStyle, String.valueOf(index[0]));
                ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                        , valueStyle, moldRow.getMoldCode());
                if (isCycleTimeExport)
                    ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                            , valueStyle, moldRow.getApprovedCT());

                groupHeaderList.stream().forEach(groupHeader -> {
                    List<MoldItem> childHearList = mapChildHeader.get(groupHeader.getGroupHeader());
                    childHearList.forEach(childHear -> {
                        int startRowChild = startRow[0];
                        List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                .collect(Collectors.toList());
                        for (int indexVal = 0; indexVal < childHear.getMaxItemInHour(); indexVal++) {
                            //init col with
                            if (startRowChild == rowData)
                                sheetWorking.setColumnWidth(indexCell[0] + indexVal, 5000);

                            String cycleTime = "";
                            String shot = "";
                            CellStyle cellStyle = valueStyle;
                            if (itemChildListValue.size() > indexVal) {
                                MoldItem item = itemChildListValue.get(indexVal);
                                cycleTime = item.getCt();
                                shot = item.getShortCount();
                                if (CycleTimeStatus.OUTSIDE_L2.equals(item.getCycleTimeStatus())) {
                                    cellStyle = valueStyleL2;
                                } else if (CycleTimeStatus.OUTSIDE_L1.equals(item.getCycleTimeStatus())) {
                                    cellStyle = valueStyleL1;
                                }
                            }
                            int indexRowChild = 0;
                            if (isCycleTimeExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild + indexRowChild++, indexCell[0] + indexVal, cellStyle, cycleTime);
                            ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild + indexRowChild, indexCell[0] + indexVal, valueStyle, shot);
                        }
                        indexCell[0] += childHear.getMaxItemInHour();
                    });
                });

                indexCell[0] = 0;
                startRow[0] += numRowPerMold;
                index[0]++;
            });
            }
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }
*/

    private Path exportExcelDynamicMoldNew(List<MoldRow> moldRowList, ExportPayload exportPayload) {
        InputStream fileTemplate = ExcelUtils.getFileTemplateExcel("dynamic/TemplateExportCycleTime.xlsx");

//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedOutputStream outputStream=null;
        FileOutputStream fos = null;
        Path tempPathFile = null;

        try {
            tempPathFile = Files.createTempFile("dynamic", null);
            fos = new FileOutputStream(tempPathFile.toFile());
            outputStream = new BufferedOutputStream(fos,4096);
            log.info("Start exportExcelDynamicMoldNew mold List: {}",moldRowList.stream().map(m->m.getMoldCode()).collect(Collectors.joining()));
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            log.info("Start exportExcelDynamicMoldNew File");

            Sheet sheet = workbook.getSheetAt(0);
            final boolean isViewHourly = exportPayload.getFrequency().equals(DateViewType.HOUR);
            if (!isViewHourly)
                workbook.setSheetName(0, getExportDynamicSheetName(exportPayload, null));
            int rowDate = 2;
            int rowGroup = 3;
            int rowHeader = 4;
            int rowDataTemp = 5;

//            Default
            int indexOfBasicInformation = 0;
            int indexStyleL1 = 6;
            int indexStyleL2 = 9;

            int indexWarning = 1;
            final int[] indexDynamicData = {2};
            final boolean isShotCountExport = exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT);
            final boolean isCycleTimeExport = exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME);
            final boolean isUptimeExport = !DateViewType.HOUR.equals(exportPayload.getFrequency()) && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME);
            final boolean isTempExport = DateViewType.HOUR.equals(exportPayload.getFrequency()) && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE);

            //style for lable
            CellStyle styleLabelGeneral = ExcelCommonUtils.cloneCellStyle(sheet, 0, 1);
            CellStyle headerGroupStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowGroup, indexOfBasicInformation);
            CellStyle headerChildStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexOfBasicInformation);
            CellStyle headerChildStyleLeft = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexOfBasicInformation);
            headerChildStyleLeft.setAlignment(HorizontalAlignment.LEFT);
            CellStyle valueStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowDataTemp, indexOfBasicInformation);
            CellStyle valueStyleLeft = ExcelCommonUtils.cloneCellStyle(sheet, rowDataTemp, indexOfBasicInformation);
            valueStyleLeft.setAlignment(HorizontalAlignment.LEFT);
            CellStyle valueStyleL1 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL1);
            CellStyle valueStyleL2 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL2);
            CellStyle styleL1 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL1);
            CellStyle styleL2 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL2);
            CellStyle warningStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexWarning);
//            warningStyle.setWrapText(false);
            ExcelCommonUtils.setFullBorderStyle(valueStyleL1);
            ExcelCommonUtils.setFullBorderStyle(valueStyleL2);

/*
            boolean isOneRowHeader = false;
            if (DateViewType.WEEK.equals(exportPayload.getFrequency()) && RangeType.WEEKLY.equals(exportPayload.getRangeType())
                    || DateViewType.MONTH.equals(exportPayload.getFrequency()) && RangeType.MONTHLY.equals(exportPayload.getRangeType())
            ) isOneRowHeader = true;

            if (isViewHourly || isOneRowHeader) {
                rowHeader = rowGroup;
                rowDataTemp = 4;
            }
*/

            //make data header common
            String timeRange = getTimeRangeLabel(exportPayload, true);
            ExcelCommonUtils.getOrCreateCell(sheet, 0, 2).setCellValue(timeRange);
            ExcelCommonUtils.getOrCreateCell(sheet, 1, 2).setCellValue(getDataFrequencyTitle(exportPayload.getFrequency()));
            //add date for hourly
            if(DateViewType.HOUR.equals(exportPayload.getFrequency())){
                ExcelCommonUtils.insertRow(sheet,rowDate);
                ExcelCommonUtils.writeCellValue(sheet, rowDate, 1,styleLabelGeneral,"Date");
                rowGroup++;
                rowHeader++;
                rowDataTemp++;
            }

            if (isCycleTimeExport || isUptimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, 0, 0, indexStyleL1, indexStyleL1 + 1, styleL1, "Outside L1");
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, 0, 0, indexStyleL2, indexStyleL2 + 1, styleL2, "Outside L2");
            } else {
                Cell c1 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL1 - 1);
                Cell c2 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL1);
                Cell c3 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL2 - 1);
                Cell c4 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL2);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c1);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c2);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c3);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c4);
            }

            // header column statistics
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, 0, 0, headerGroupStyle, "SEQ");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, 1, 1, headerGroupStyle, "Tooling ID");
            if (isCycleTimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Approved CT");
            }
            if (isUptimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Uptime Target");
            }

            //build header list
            Pair<List<MoldItem>,Map<String, List<MoldItem>>> makerHeaderFull=makerHeaderFull(exportPayload);
            List<MoldItem> groupHeaderListTemp=makerHeaderFull.getFirst();
            Map<String, List<MoldItem>> mapChildHeader=makerHeaderFull.getSecond();
//            List<MoldItem> groupHeaderListTemp = new ArrayList<>();
            List<String> fieldHearList=new ArrayList<>();
//            Map<String, List<MoldItem>> mapChildHeader = new HashMap<>();
            for (int i = 0; i < moldRowList.size(); i++) {
                MoldRow moldRow = moldRowList.get(i);
                if (moldRow.getMoldItemList() != null) {
                    addMoldItemToHeader(moldRow.getMoldItemList(),groupHeaderListTemp,mapChildHeader);
/*
                    moldRow.getMoldItemList().forEach(moldItem -> {
                        Set<String> headerSet = groupHeaderListTemp.stream().map(h -> h.getGroupHeader()).collect(Collectors.toSet());
                        if (!headerSet.contains(moldItem.getGroupHeader())) {
                            groupHeaderListTemp.add(moldItem);
                        }

                        List<MoldItem> childHeaderList;
                        if (!mapChildHeader.containsKey(moldItem.getGroupHeader())) {
                            childHeaderList = new ArrayList<>();
                            mapChildHeader.put(moldItem.getGroupHeader(), childHeaderList);
                        } else {
                            childHeaderList = mapChildHeader.get(moldItem.getGroupHeader());
                        }
                        Set<String> childHeaderSet = childHeaderList.stream().map(h -> h.getChildHeader()).collect(Collectors.toSet());
                        if (childHeaderSet.contains(moldItem.getChildHeader())) {
                            MoldItem old = childHeaderList.stream().filter(h -> h.getChildHeader().equalsIgnoreCase(moldItem.getChildHeader())).findFirst().orElse(null);
                            if (old.getMaxItemInHour() <= moldItem.getIndexInHour())
                                old.setMaxItemInHour(moldItem.getIndexInHour() + 1);
                        } else {
                            childHeaderList.add(moldItem);
                        }

                    });
*/

                }
            }
            if(isShotCountExport)
                fieldHearList.add("Shot Count");
            if(isCycleTimeExport)
                fieldHearList.add("Cycle Time");
            if(isUptimeExport)
                fieldHearList.add("Uptime");
            if(isTempExport)
                fieldHearList.add("Temperature");
            //sort column
            List<MoldItem> groupHeaderList = groupHeaderListTemp.stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < groupHeaderList.size(); i++) {
                MoldItem groupHeader = groupHeaderList.get(i);
                if (i == 0 || isViewHourly) groupHeader.setLastIndexCol(indexDynamicData[0]);
                else {
                    MoldItem groupHeaderBefore = groupHeaderList.get(i - 1);
                    groupHeader.setLastIndexCol(groupHeaderBefore.getLastIndexCol() + groupHeaderBefore.getNumColInGroup());
                }
                List<MoldItem> childHeaderList = mapChildHeader.get(groupHeader.getGroupHeader());
                childHeaderList = childHeaderList.stream().sorted().collect(Collectors.toList());
                mapChildHeader.put(groupHeader.getGroupHeader(), childHeaderList);

                int numColInGroup = childHeaderList.stream().map(c -> c.getMaxItemInHour() * fieldHearList.size()).mapToInt(Integer::intValue).sum();//getMaxItemInHour=1 for new file
                groupHeader.setNumColInGroup(numColInGroup);
            }

            //write header to file
            ExcelCommonUtils.getOrCreateRow(sheet, rowGroup).setHeight((short) -1);//auto resize
            ExcelCommonUtils.getOrCreateRow(sheet, rowHeader).setHeight((short) -1);//auto resize
            log.info("Done build general file");
            //build sheet for Hourly
            Map<String, Sheet> mapSheet = new HashMap<>();
//            if (isViewHourly) {
                //for empty data
                if(groupHeaderList.isEmpty()) {
                    MoldItem groupHeader= new MoldItem();
                    if(DateViewType.WEEK.equals(exportPayload.getFrequency())){
                        groupHeader.setTitle(exportPayload.getTime());
                    }else
                    groupHeader.setTitle(exportPayload.getFromDate()+"00");
                    groupHeader.setNumColInGroup(1);
                    groupHeader.setLastIndexCol(indexDynamicData[0]);
                    updateHeaderTable(groupHeader,exportPayload);
                    groupHeaderList.add(groupHeader);
                    mapChildHeader.put(groupHeader.getGroupHeader(), Arrays.asList(groupHeader));
                }
                if (isViewHourly) {
                    log.info("Start build default sheet for hourly");
                for (int sheetIndex = 0; sheetIndex < groupHeaderList.size(); sheetIndex++) {
                    MoldItem groupHeader = groupHeaderList.get(sheetIndex);
                    Sheet sheetCopy = sheet;
                    String sheetName = getExportDynamicSheetName(exportPayload, groupHeader.getTimeSort());
                    if (sheetIndex == 0) {
                        workbook.setSheetName(0, sheetName);
                    } else {
                        sheetCopy = workbook.cloneSheet(0, sheetName);
                    }
                    if (groupHeader.getTimeSort() != null) {
                        String dateSheet = DateUtils.getDate(groupHeader.getTimeSort(), DateUtils.YYYY_MM_dd);
                        ExcelCommonUtils.writeCellValue(sheetCopy, rowDate, 2, styleLabelGeneral, dateSheet);
                    }

                    mapSheet.put(groupHeader.getGroupHeader(), sheetCopy);
                }
                    log.info("End build default sheet for hourly: {} sheets",mapSheet.size());
            }
            //write header to file
            log.info("Start write header to file");
            for (int i = 0; i < groupHeaderList.size(); i++) {
                MoldItem groupHeader = groupHeaderList.get(i);
                int startCol = groupHeader.getLastIndexCol();
                int endCol = startCol + groupHeader.getNumColInGroup() - 1;
                int indexColCurrent = startCol;//for set with in hour
                Sheet sheetWorking = sheet;
                if (isViewHourly)
                    sheetWorking = mapSheet.get(groupHeader.getGroupHeader());
                List<MoldItem> childHeaderList = mapChildHeader.get(groupHeader.getGroupHeader());
                for (int indexChild = 0; indexChild < childHeaderList.size(); indexChild++) {
                    MoldItem childHeader = childHeaderList.get(indexChild);
/*
                    if (isViewHourly) {
                        for (int indexInHour = 0; indexInHour < childHeader.getMaxItemInHour(); indexInHour++) {
                            sheetWorking.setColumnWidth(indexColCurrent, 5000);
                            indexColCurrent++;
                        }
                    } else if (!isOneRowHeader) {

                    Cell subHearCell = ExcelCommonUtils.getOrCreateCell(sheetWorking, rowHeader, indexChild + startCol);
                        subHearCell.setCellStyle(headerChildStyle);
                        subHearCell.setCellValue(childHeader.getChildHeader());
                        sheetWorking.setColumnWidth(indexChild + startCol, 5000);
                    }
*/

                    int startChildCol=startCol + indexChild * fieldHearList.size();
                    int endChildCol=startCol + (indexChild + 1) * fieldHearList.size() - 1;

                    for(int indexCol=0;indexCol<fieldHearList.size();indexCol++){
                        Cell subHearCell = ExcelCommonUtils.getOrCreateCell(sheetWorking, rowHeader, startChildCol+indexCol);
                        subHearCell.setCellStyle(headerChildStyle);
                        subHearCell.setCellValue(fieldHearList.get(indexCol));
                        sheetWorking.setColumnWidth(startChildCol+indexCol, 5000);
                    }
                    ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, rowGroup, rowGroup
                            , startChildCol, endChildCol
                            , headerGroupStyle, childHeader.getChildHeader());
                }
//                ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, rowGroup, rowGroup, startCol, endCol, headerGroupStyle, groupHeader.getGroupHeaderView());
            }
            log.info("End write header to file");

/*

            int numRowPerMoldTemp = 2;
            if (isViewHourly) {
                numRowPerMoldTemp = 3;
            }
            if (!isCycleTimeExport) {
                numRowPerMoldTemp--;
            }
            final int numRowPerMold = numRowPerMoldTemp;
*/
            final int rowData = rowDataTemp;

            final int numRowPerMold = 1;
            //Export Data
            log.info("Start write data to workbook");
            if (isViewHourly) {
                groupHeaderList.stream().forEach(groupHeader -> {
                    Sheet sheetWorking = mapSheet.get(groupHeader.getGroupHeader());
                    log.info("Start write data for sheet: {}",groupHeader.getGroupHeader());
                    final int[] startRow = {rowData};
                    final int[] index = {1};
                    final int[] indexCell = {0};
                    //for resize
                    TwoObject<Boolean,Integer> existWarningAndColsize= new TwoObject<>(false,1);
                    moldRowList.stream().forEach(moldRow -> {
                        for (int num = 0; num < numRowPerMold; num++)
                            ExcelCommonUtils.getOrCreateRow(sheetWorking, startRow[0] + num).setHeight((short) -1);//auto resize height

                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                , valueStyle, String.valueOf(index[0]));
                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                , valueStyle, moldRow.getMoldCode());
                        if (isCycleTimeExport)
                            ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                    , valueStyle, moldRow.getApprovedCT());
                        if (isUptimeExport)
                            ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                    , valueStyle, moldRow.getUptimeTarget());

                        List<MoldItem> childHearList = mapChildHeader.get(groupHeader.getGroupHeader());

                        boolean existsData=false;
                        if (!moldRow.isNewCounter())
                            existsData = childHearList.stream().filter(childHear -> {
                                List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                        .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                        .collect(Collectors.toList());
                                if (itemChildListValue != null && itemChildListValue.size() > 0) return true;
                                return false;
                            }).findAny().isPresent();

                        //check new counter
                        if (!moldRow.isNewCounter() && (!existsData || !StringUtils.isEmpty(moldRow.getCounterCode()))) {
                            final int totalColData = childHearList.stream().map(h -> h.getMaxItemInHour()*fieldHearList.size()).reduce(0, (a, b) -> a + b);
                            String message = "No data available because the tooling is not attached to any counter";
                            if (!StringUtils.isEmpty(moldRow.getCounterCode()) )
                                message = "No hourly data available because the tooling is attached to old generation counter (Sensor ID: " + moldRow.getCounterCode() + ")";
                            ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0] + (totalColData > 1 ? totalColData - 1 : 0)
                                    , warningStyle
                                    , message);
                            existWarningAndColsize.setLeft(true);
                            existWarningAndColsize.setRight(totalColData);
                        } else {
                            childHearList.forEach(childHear -> {
                                int startRowChild = startRow[0];

/*
                                ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRowChild, startRowChild++, indexCell[0], indexCell[0] + (childHear.getMaxItemInHour()*fieldHearList.size() - 1)
                                        , headerChildStyle, childHear.getChildHeader());
*/


                                List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                        .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                        .collect(Collectors.toList());
                                for (int indexVal = 0; indexVal < childHear.getMaxItemInHour(); indexVal++) {
                                    //init col with
//                                        if (startRowChild == rowData)
                                    sheetWorking.setColumnWidth(indexCell[0] + indexVal, 5000);


                                    String cycleTime = "";
                                    String shot = "";
                                    String uptime = "";
                                    String temp = "";
                                    CellStyle cellStyle = valueStyle;
                                    CellStyle cellStyleUptime = valueStyle;
                                    if (itemChildListValue.size() > indexVal) {
                                        MoldItem item = itemChildListValue.get(indexVal);
                                        cycleTime = item.getCt();
                                        shot = item.getShortCount();
                                        uptime=item.getUptimeHour();
                                        temp=item.getTemperature();
                                        if (CycleTimeStatus.OUTSIDE_L2.equals(item.getCycleTimeStatus())) {
                                            cellStyle = valueStyleL2;
                                        } else if (CycleTimeStatus.OUTSIDE_L1.equals(item.getCycleTimeStatus())) {
                                            cellStyle = valueStyleL1;
                                        }
                                        if (EfficiencyStatus.OUTSIDE_L2.equals(item.getUptimeStatus())) {
                                            cellStyleUptime = valueStyleL2;
                                        } else if (EfficiencyStatus.OUTSIDE_L1.equals(item.getUptimeStatus())) {
                                            cellStyleUptime = valueStyleL1;
                                        }
                                    }
                                    int colSubIndex=indexVal*fieldHearList.size();
                                    if(isShotCountExport){
                                        ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, shot);
                                    }
                                    if (isCycleTimeExport)
                                        ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyle, cycleTime);
                                    if (isUptimeExport)
                                        ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyleUptime, uptime);
                                    if (isTempExport)
                                        ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, temp);


                                }
//                                }
                                indexCell[0] += childHear.getMaxItemInHour()*fieldHearList.size();

                            });
                        }

                        indexCell[0] = 0;
                        startRow[0] += numRowPerMold;
                        index[0]++;
                    });
                    if(existWarningAndColsize.getLeft() && existWarningAndColsize.getRight()<6){
//                        int colShow = isCycleTimeExport ? 3 : 2;
                        int colShow = 2;
                        if(isCycleTimeExport) colShow++;
                        if(isUptimeExport) colShow++;
                        if(existWarningAndColsize.getRight()>1) {
                            for (int c = 0; c < existWarningAndColsize.getRight(); c++) {
                                sheetWorking.setColumnWidth(colShow + c, WIDTH_WARNING / existWarningAndColsize.getRight());
                            }
                        } else {
                            sheetWorking.autoSizeColumn(colShow, true);
                        }

                    }
                    //write note
                    int rowNote=startRow[0]+1;
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,headerChildStyleLeft,"Data");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,headerChildStyleLeft,"Unit");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Shot Count");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Shot");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Cycle Time");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Second");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Uptime");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Hour");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Temperature");
                    ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Celsius");
                    log.info("End write data for sheet: {}",groupHeader.getGroupHeader());
                });
            } else {
                Sheet sheetWorking = sheet;
                final int[] startRow = {rowData};
                final int[] index = {1};
                final int[] indexCell = {0};
                List<MoldItem> childHearListAll=new ArrayList<>();
                final int[] totalColDataArr={0};
                mapChildHeader.keySet().stream().map(k->mapChildHeader.get(k)).forEach(childHeaderList->{
                    totalColDataArr[0] += childHeaderList.stream().map(h -> h.getMaxItemInHour()*fieldHearList.size()).reduce(0, (a, b) -> a + b);
                });
                TwoObject<Boolean, Integer> existWarningAndColsize = new TwoObject<>(false, 1);

                moldRowList.stream().forEach(moldRow -> {
                    for (int num = 0; num < numRowPerMold; num++)
                        ExcelCommonUtils.getOrCreateRow(sheetWorking, startRow[0] + num).setHeight((short) -1);//auto resize height

                    ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                            , valueStyle, String.valueOf(index[0]));
                    ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                            , valueStyle, moldRow.getMoldCode());
                    if (isCycleTimeExport)
                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                , valueStyle, moldRow.getApprovedCT());
                    if (isUptimeExport)
                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0]++
                                , valueStyle, moldRow.getUptimeTarget());

                    boolean existsData = moldRow.getMoldItemList().size() > 0;
                    final int totalColData = totalColDataArr[0];
                    //check new counter
                    if (!existsData && StringUtils.isEmpty(moldRow.getCounterCode())) {
//                    if (!moldRow.isNewCounter() && (!existsData || !StringUtils.isEmpty(moldRow.getCounterCode()))) {
                        String message = "No data available because the tooling is not attached to any counter";
//                        if (!StringUtils.isEmpty(moldRow.getCounterCode()) && (!isShotCountExport && !isCycleTimeExport && !isUptimeExport))
//                            message = "No data available because the tooling is attached to old generation counter (Sensor ID: " + moldRow.getCounterCode() + ")";
                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (numRowPerMold - 1), indexCell[0], indexCell[0] + (totalColData > 1 ? totalColData - 1 : 0)
                                , warningStyle
                                , message);
                        existWarningAndColsize.setLeft(true);
                        existWarningAndColsize.setRight(totalColData);
                    } else {
                    groupHeaderList.stream().forEach(groupHeader -> {
                        List<MoldItem> childHearList = mapChildHeader.get(groupHeader.getGroupHeader());
                        childHearList.forEach(childHear -> {
                            int startRowChild = startRow[0];

                            List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                    .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                    .collect(Collectors.toList());//1 item
                            for (int indexVal = 0; indexVal < childHear.getMaxItemInHour(); indexVal++) {//1 item
                                //init col with
                                if (startRowChild == rowData)
                                    sheetWorking.setColumnWidth(indexCell[0] + indexVal, 5000);

                                String cycleTime = "";
                                String shot = "";
                                String uptime = "";
                                String temp = "";
                                CellStyle cellStyle = valueStyle;
                                CellStyle cellStyleUptime = valueStyle;
                                if (itemChildListValue.size() > indexVal) {
                                    MoldItem item = itemChildListValue.get(indexVal);
                                    cycleTime = item.getCt();
                                    shot = item.getShortCount();
                                    uptime=item.getUptimeHour();
                                    temp=item.getTemperature();
                                    if (CycleTimeStatus.OUTSIDE_L2.equals(item.getCycleTimeStatus())) {
                                        cellStyle = valueStyleL2;
                                    } else if (CycleTimeStatus.OUTSIDE_L1.equals(item.getCycleTimeStatus())) {
                                        cellStyle = valueStyleL1;
                                    }
                                    if (EfficiencyStatus.OUTSIDE_L2.equals(item.getUptimeStatus())) {
                                        cellStyleUptime = valueStyleL2;
                                    } else if (EfficiencyStatus.OUTSIDE_L1.equals(item.getUptimeStatus())) {
                                        cellStyleUptime = valueStyleL1;
                                    }
                                }
                                int colSubIndex=indexVal*fieldHearList.size();
                                if(isShotCountExport){
                                    ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, shot);
                                }
                                if (isCycleTimeExport)
                                    ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyle, cycleTime);
                                if (isUptimeExport)
                                    ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyleUptime, uptime);
                                if (isTempExport)
                                    ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, temp);

/*
                                int indexRowChild = 0;
                                if (isCycleTimeExport)
                                    ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild + indexRowChild++, indexCell[0] + indexVal, cellStyle, cycleTime);
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild + indexRowChild, indexCell[0] + indexVal, valueStyle, shot);
*/
                            }
                            indexCell[0] += childHear.getMaxItemInHour()*fieldHearList.size();
                        });
                    });
                    }
                    indexCell[0] = 0;
                    startRow[0] += numRowPerMold;
                    index[0]++;
                });
                if(existWarningAndColsize.getLeft() && existWarningAndColsize.getRight()<4){
//                        int colShow = isCycleTimeExport ? 3 : 2;
                    int colShow = 2;
                    if(isCycleTimeExport) colShow++;
                    if(isUptimeExport) colShow++;
                    if(existWarningAndColsize.getRight()>1) {
                        for (int c = 0; c < existWarningAndColsize.getRight(); c++) {
                            sheetWorking.setColumnWidth(colShow + c, WIDTH_WARNING*2/3/existWarningAndColsize.getRight());
                        }
                    } else {
                        sheetWorking.autoSizeColumn(colShow, true);
                    }

                }
                //write note
                int rowNote=startRow[0]+1;
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,headerChildStyleLeft,"Data");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,headerChildStyleLeft,"Unit");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Shot Count");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Shot");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Cycle Time");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Second");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Uptime");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Hour");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote,1,valueStyleLeft,"Temperature");
                ExcelCommonUtils.writeCellValue(sheetWorking,rowNote++,2,valueStyleLeft,"Celsius");

            }
            log.info("End write data to workbook");
            mapSheet.clear();//GC
            log.info("start write data to file");
            workbook.write(outputStream);
            log.info("flush file");
            outputStream.flush();
            log.info("end write data to file");
            workbook.close();
            log.info("Start exportExcelDynamicMoldNew File");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(outputStream!=null) outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                if(fos!=null) fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

//        return outputStream;
        return tempPathFile;
    }

    public void addMoldItemToHeader(List<MoldItem> moldItemList,final List<MoldItem> groupHeaderListTemp ,final Map<String, List<MoldItem>> mapChildHeader ){
        moldItemList.forEach(moldItem -> {
            Set<String> headerSet = groupHeaderListTemp.stream().map(h -> h.getGroupHeader()).collect(Collectors.toSet());
            if (!headerSet.contains(moldItem.getGroupHeader())) {
                groupHeaderListTemp.add(moldItem);
            }

            List<MoldItem> childHeaderList;
            if (!mapChildHeader.containsKey(moldItem.getGroupHeader())) {
                childHeaderList = new ArrayList<>();
                mapChildHeader.put(moldItem.getGroupHeader(), childHeaderList);
            } else {
                childHeaderList = mapChildHeader.get(moldItem.getGroupHeader());
            }
            Set<String> childHeaderSet = childHeaderList.stream().map(h -> h.getChildHeader()).collect(Collectors.toSet());
            if (childHeaderSet.contains(moldItem.getChildHeader())) {
                MoldItem old = childHeaderList.stream().filter(h -> h.getChildHeader().equalsIgnoreCase(moldItem.getChildHeader())).findFirst().orElse(null);
                if (old.getMaxItemInHour() <= moldItem.getIndexInHour())
                    old.setMaxItemInHour(moldItem.getIndexInHour() + 1);
            } else {
                childHeaderList.add(moldItem);
            }

        });
    }
    public Pair<List<MoldItem>,Map<String, List<MoldItem>>> makerHeaderFull(ExportPayload exportPayload){
        List<MoldItem> groupHeaderListTemp = new ArrayList<>();
        Map<String, List<MoldItem>> mapChildHeader = new HashMap<>();
        List<MoldItem> fullHeader = new ArrayList<>();

        DateViewType dateViewType = exportPayload.getFrequency();
        List<ChartData> chartDataListDefault=makeChartDataListDefault(exportPayload.getFromDate(),exportPayload.getToDate(),exportPayload.getFrequency());
/*

        Instant minDate = DateUtils.getInstant(exportPayload.getFromDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT);
        Instant maxDate = DateUtils.getInstant(exportPayload.getToDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT);
        List<ChartData> chartDataListDefault=new ArrayList<>();
        ChartData cStart=null;
        ChartData cEnd=null;
        if (DateViewType.MONTH.equals(dateViewType)) {
            cStart=new ChartData(exportPayload.getFromDate().substring(0,6), 0, 0);
            cEnd=new ChartData(exportPayload.getToDate().substring(0,6), 0, 0);
        } else if (DateViewType.WEEK.equals(dateViewType)) {
            cStart=new ChartData(DateUtils.getYearWeek(minDate,DateUtils.DEFAULT_DATE_FORMAT), 0, 0);
            cEnd=new ChartData(DateUtils.getYearWeek(maxDate,DateUtils.DEFAULT_DATE_FORMAT), 0, 0);
        } else if (DateViewType.DAY.equals(dateViewType)) {
            cStart=new ChartData(exportPayload.getFromDate().substring(0,8), 0, 0);
            cEnd=new ChartData(exportPayload.getToDate().substring(0,8), 0, 0);
        } else if (DateViewType.HOUR.equals(dateViewType)) {
            cStart=new ChartData(exportPayload.getFromDate().substring(0,8)+"00", 0, 0);
            cEnd=new ChartData(exportPayload.getToDate().substring(0,8)+"23", 0, 0);
        }
        chartDataListDefault.add(cStart);
        if(!exportPayload.getFromDate().equals(exportPayload.getToDate())){
            chartDataListDefault.add(cEnd);
        }
*/
        List<ChartData> chartDataList=MoldRepositoryImpl.getChartDataAfterAddBlankDay(chartDataListDefault,dateViewType,true);
        fullHeader=chartDataList.stream().map(c-> convertChartToMoldItemNew(new Mold(), c, exportPayload)).collect(Collectors.toList());
        addMoldItemToHeader(fullHeader,groupHeaderListTemp,mapChildHeader);
        return Pair.of(groupHeaderListTemp,mapChildHeader);
    }
    public static List<ChartData> makeChartDataListDefault(String fromDate,String toDate,DateViewType dateViewType){
        Instant minDate = DateUtils.getInstant(fromDate, DateUtils.YYYY_MM_DD_DATE_FORMAT);
        Instant maxDate = DateUtils.getInstant(toDate, DateUtils.YYYY_MM_DD_DATE_FORMAT);
        List<ChartData> chartDataListDefault=new ArrayList<>();
        ChartData cStart=null;
        ChartData cEnd=null;
        if (DateViewType.MONTH.equals(dateViewType)) {
            cStart=new ChartData(fromDate.substring(0,6), 0, 0);
            cEnd=new ChartData(toDate.substring(0,6), 0, 0);
        } else if (DateViewType.WEEK.equals(dateViewType)) {
            cStart=new ChartData(DateUtils.getYearWeek(minDate,DateUtils.DEFAULT_DATE_FORMAT), 0, 0);
            cEnd=new ChartData(DateUtils.getYearWeek(maxDate,DateUtils.DEFAULT_DATE_FORMAT), 0, 0);
        } else if (DateViewType.DAY.equals(dateViewType)) {
            cStart=new ChartData(fromDate.substring(0,8), 0, 0);
            cEnd=new ChartData(toDate.substring(0,8), 0, 0);
        } else if (DateViewType.HOUR.equals(dateViewType)) {
            cStart=new ChartData(fromDate.substring(0,8)+"00", 0, 0);
            cEnd=new ChartData(toDate.substring(0,8)+"23", 0, 0);
        }
        chartDataListDefault.add(cStart);
        if(!fromDate.equals(toDate)){
            chartDataListDefault.add(cEnd);
        }
        return chartDataListDefault;
    }
    public void exportExcelDynamicOneToolingPerFile(HttpServletResponse response, ExportPayload payload
            , Pageable pageable) throws IOException {
        List<MoldRow> moldRowList = prepareDataForFile(payload, pageable);
        ByteArrayOutputStream byteArrayOutputStream = null;
        OutputStream outputStream = null;
        try {
            String fileName = moldRowList.size() != 1 ? getExportDynamicFileName(payload).replace(".xlsx", ".zip") :
                    getExportDynamicFileNamePerTooling(payload, moldRowList.get(0).getMoldCode());
            log.info("start make file export for {} tooling ",moldRowList.size());
            if(moldRowList.size()==1){
                Path pathOut = exportExcelDynamicPerMold(moldRowList.get(0), payload);
                if (pathOut != null) {
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    byteArrayOutputStream.write(Files.readAllBytes(pathOut));
                    Files.delete(pathOut);
                }
            }else {
                List<TwoObject<String, Path>> listDataFile=moldRowList.stream().map(moldRow ->
                        TwoObject.of(getExportDynamicFileNamePerTooling(payload, moldRow.getMoldCode()),
                                exportExcelDynamicPerMold(moldRow, payload))).collect(Collectors.toList());
                log.info("Done exportExcelDynamicOneToolingPerFile build list temp excel files");
                log.info("Start convert list temp excel files to byteArray Zip file");
                //convert to valid name
                final List<String> originalNames=listDataFile.stream().map(p->p.getLeft()).collect(Collectors.toList());
                listDataFile.stream().forEach(p->{
                     p.setLeft(FileUtils.getValidFileNameInList(p.getLeft(),originalNames));
                });
                byteArrayOutputStream =  FileUtils.zipMultipleFiles(listDataFile);
                listDataFile.stream().forEach(dataFile-> {
                    try {
                        Files.delete(dataFile.getRight());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            outputStream.write(byteArrayOutputStream.toByteArray());
//        byteArrayOutputStream.close();
//        outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Path exportExcelDynamicPerMold(MoldRow moldRow, ExportPayload exportPayload) {
        InputStream fileTemplate = ExcelUtils.getFileTemplateExcel("dynamic/TemplateExportDynamicPerTooling.xlsx");
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedOutputStream outputStream=null;
        FileOutputStream fos = null;
        Path tempPathFile = null;
        try {
            tempPathFile = Files.createTempFile("dynamic", null);
            fos = new FileOutputStream(tempPathFile.toFile());
            outputStream = new BufferedOutputStream(fos);
            log.info("Start exportExcelDynamicPerMold mold: {}",moldRow.getMoldCode());

            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            final boolean isViewHourly = exportPayload.getFrequency().equals(DateViewType.HOUR);
            if (!isViewHourly)
                workbook.setSheetName(0, getExportDynamicSheetName(exportPayload, null));
            int rowDate = 3;
            int rowNote = 4;

            int rowGroup = 6;
            int rowHeader = 6;
            int rowWarning = 7;
            int rowDataTemp = 7;

//            Default
            int colLabelGeneral = 0;
            int colDataGeneral = 1;
            int indexOfBasicInformation = 0;
            int indexStyleL1 = 5;
            int indexStyleL2 = 7;

            int indexWarning = 1;
            final int[] indexDynamicData = {0};
            final int[] colDynamicDataStart = {0};
            final boolean isShotCountExport = exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT);
            final boolean isCycleTimeExport = exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME);
            final boolean isUptimeExport = !DateViewType.HOUR.equals(exportPayload.getFrequency()) && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME);
            final boolean isTempExport = DateViewType.HOUR.equals(exportPayload.getFrequency()) && exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE);

            //style for lable
            CellStyle styleLabelGeneral = ExcelCommonUtils.cloneCellStyle(sheet, 0, 1);
            CellStyle headerGroupStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowGroup, indexOfBasicInformation);
            CellStyle headerChildStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexOfBasicInformation);
            CellStyle headerChildStyleLeft = ExcelCommonUtils.cloneCellStyle(sheet, rowHeader, indexOfBasicInformation);
            headerChildStyleLeft.setAlignment(HorizontalAlignment.LEFT);
            CellStyle valueStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowDataTemp, indexOfBasicInformation);
            CellStyle valueStyleLeft = ExcelCommonUtils.cloneCellStyle(sheet, rowDataTemp, indexOfBasicInformation);
            valueStyleLeft.setAlignment(HorizontalAlignment.LEFT);
            CellStyle valueStyleL1 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL1);
            CellStyle valueStyleL2 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL2);
            CellStyle styleL1 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL1);
            CellStyle styleL2 = ExcelCommonUtils.cloneCellStyle(sheet, 0, indexStyleL2);
            CellStyle warningStyle = ExcelCommonUtils.cloneCellStyle(sheet, rowWarning, indexWarning);
//            warningStyle.setWrapText(false);
            ExcelCommonUtils.setFullBorderStyle(valueStyleL1);
            ExcelCommonUtils.setFullBorderStyle(valueStyleL2);

            //make data header common
            //increase high header
            rowHeader++;
            rowDataTemp++;

            String timeRange = getTimeRangeLabel(exportPayload, true);
            ExcelCommonUtils.getOrCreateCell(sheet, 0, colDataGeneral).setCellValue(moldRow.getMoldCode());
            ExcelCommonUtils.getOrCreateCell(sheet, 1, colDataGeneral).setCellValue(timeRange);
            ExcelCommonUtils.getOrCreateCell(sheet, 2, colDataGeneral).setCellValue(getDataFrequencyTitle(exportPayload.getFrequency()));
            //add date for hourly
            if (DateViewType.HOUR.equals(exportPayload.getFrequency())) {
                ExcelCommonUtils.insertRow(sheet, rowDate);
                ExcelCommonUtils.writeCellValue(sheet, rowDate, colLabelGeneral, styleLabelGeneral, "Date");
                rowNote++;
                rowGroup++;
                rowHeader++;
                rowDataTemp++;
            }

            if (isCycleTimeExport || isUptimeExport) {
//                ExcelCommonUtils.mergedRegionAndSetVal(sheet, 0, 0, indexStyleL1, indexStyleL1 + 1, styleL1, "Outside L1");
//                ExcelCommonUtils.mergedRegionAndSetVal(sheet, 0, 0, indexStyleL2, indexStyleL2 + 1, styleL2, "Outside L2");
            } else {
                Cell c1 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL1 - 1);
                Cell c2 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL1);
                Cell c3 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL2 - 1);
                Cell c4 = ExcelCommonUtils.getOrCreateCell(sheet, 0, indexStyleL2);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c1);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c2);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c3);
                ExcelCommonUtils.getOrCreateRow(sheet, 0).removeCell(c4);
            }
            List<String> fieldHearList = new ArrayList<>();
            if (isShotCountExport)
                fieldHearList.add("Shot Count");
            if (isCycleTimeExport)
                fieldHearList.add("Cycle Time");
            if (isUptimeExport)
                fieldHearList.add("Uptime");
            if (isTempExport)
                fieldHearList.add("Temperature");

            if (!fieldHearList.isEmpty()) {
//                int increase=fieldHearList.size();
                int increase = 4;
                rowGroup += increase;
                rowHeader += increase;
                rowDataTemp += increase;
                ExcelCommonUtils.insertRow(sheet, rowNote, increase);

                ExcelCommonUtils.writeCellValue(sheet, rowNote, 0, headerChildStyleLeft, "Data");
                ExcelCommonUtils.writeCellValue(sheet, rowNote++, 1, headerChildStyleLeft, "Unit");
                ExcelCommonUtils.writeCellValue(sheet, rowNote, 0, valueStyleLeft, "Shot Count");
                ExcelCommonUtils.writeCellValue(sheet, rowNote++, 1, valueStyleLeft, "Shot");
                ExcelCommonUtils.writeCellValue(sheet, rowNote, 0, valueStyleLeft, "Cycle Time");
                ExcelCommonUtils.writeCellValue(sheet, rowNote++, 1, valueStyleLeft, "Second");
                ExcelCommonUtils.writeCellValue(sheet, rowNote, 0, valueStyleLeft, "Uptime");
                ExcelCommonUtils.writeCellValue(sheet, rowNote++, 1, valueStyleLeft, "Hour");
                ExcelCommonUtils.writeCellValue(sheet, rowNote, 0, valueStyleLeft, "Temperature");
                ExcelCommonUtils.writeCellValue(sheet, rowNote++, 1, valueStyleLeft, "Celsius");
            }

            // header column statistics
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Tooling ID");
            String labelDate = "Date";
            if (DateViewType.MONTH.equals(exportPayload.getFrequency())) {
                labelDate = "Month";
            } else if (DateViewType.WEEK.equals(exportPayload.getFrequency())) {
                labelDate = "Week";
            }

            ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, labelDate);
            if (isViewHourly)
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Hour");

            if (isCycleTimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Approved CT");
            }
            if (isUptimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Uptime Target");
            }
            colDynamicDataStart[0] = indexDynamicData[0];
            if (isCycleTimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Actual CT");
            }
            if (isUptimeExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Actual Uptime");
            }
            if (isTempExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Temperature");
            }
            if (isShotCountExport) {
                ExcelCommonUtils.mergedRegionAndSetVal(sheet, rowGroup, rowHeader, indexDynamicData[0], indexDynamicData[0]++, headerGroupStyle, "Shot");
            }
            for (int indexCol = 0; indexCol < indexDynamicData[0]; indexCol++) {
                sheet.setColumnWidth(indexCol, 5000);
            }
            //build header list
            Pair<List<MoldItem>, Map<String, List<MoldItem>>> makerHeaderFull = makerHeaderFull(exportPayload);
            List<MoldItem> groupHeaderListTemp = makerHeaderFull.getFirst();
            Map<String, List<MoldItem>> mapChildHeader = makerHeaderFull.getSecond();
            if (moldRow.getMoldItemList() != null) {
                addMoldItemToHeader(moldRow.getMoldItemList(), groupHeaderListTemp, mapChildHeader);
            }

            //sort column
            List<MoldItem> groupHeaderList = groupHeaderListTemp.stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < groupHeaderList.size(); i++) {
                MoldItem groupHeader = groupHeaderList.get(i);
                List<MoldItem> childHeaderList = mapChildHeader.get(groupHeader.getGroupHeader());
                childHeaderList = childHeaderList.stream().sorted().collect(Collectors.toList());
                mapChildHeader.put(groupHeader.getGroupHeader(), childHeaderList);
            }

            //write header to file
            ExcelCommonUtils.getOrCreateRow(sheet, rowGroup).setHeight((short) -1);//auto resize
            ExcelCommonUtils.getOrCreateRow(sheet, rowHeader).setHeight((short) -1);//auto resize
            log.info("Done build general file");

            //build sheet for Hourly
            Map<String, Sheet> mapSheet = new HashMap<>();
//            if (isViewHourly) {
            //for empty data
            if (groupHeaderList.isEmpty()) {
                MoldItem groupHeader = new MoldItem();
                if (DateViewType.WEEK.equals(exportPayload.getFrequency())) {
                    groupHeader.setTitle(exportPayload.getTime());
                } else
                    groupHeader.setTitle(exportPayload.getFromDate() + "00");
                groupHeader.setNumColInGroup(1);
                groupHeader.setLastIndexCol(indexDynamicData[0]);
                updateHeaderTable(groupHeader, exportPayload);
                groupHeaderList.add(groupHeader);
                mapChildHeader.put(groupHeader.getGroupHeader(), Arrays.asList(groupHeader));
            }
            if (isViewHourly) {
                log.info("Start write sheet hourly");
                for (int sheetIndex = 0; sheetIndex < groupHeaderList.size(); sheetIndex++) {
                    MoldItem groupHeader = groupHeaderList.get(sheetIndex);
                    Sheet sheetCopy = sheet;
                    String sheetName = getExportDynamicSheetName(exportPayload, groupHeader.getTimeSort());
                    if (sheetIndex == 0) {
                        workbook.setSheetName(0, sheetName);
                    } else {
                        sheetCopy = workbook.cloneSheet(0, sheetName);
                    }
                    if (groupHeader.getTimeSort() != null) {
                        String dateSheet = DateUtils.getDate(groupHeader.getTimeSort(), DateUtils.YYYY_MM_dd);
                        ExcelCommonUtils.writeCellValue(sheetCopy, rowDate, colDataGeneral, styleLabelGeneral, dateSheet);
                    }

                    mapSheet.put(groupHeader.getGroupHeader(), sheetCopy);
                }
                log.info("End write sheet hourly {} sheets",mapSheet.size());

            }
            //write data default to file
            log.info("Start write data column default to per sheet");
            int indexRowDataDefault = rowDataTemp;
            for (int i = 0; i < groupHeaderList.size(); i++) {
                MoldItem groupHeader = groupHeaderList.get(i);
                Sheet sheetWorking = sheet;
                if (isViewHourly) {
                    sheetWorking = mapSheet.get(groupHeader.getGroupHeader());
                    indexRowDataDefault = rowDataTemp;
                }
                List<MoldItem> childHeaderList = mapChildHeader.get(groupHeader.getGroupHeader());
                for (int indexChild = 0; indexChild < childHeaderList.size(); indexChild++) {
                    int colData = 0;
                    MoldItem childHeader = childHeaderList.get(indexChild);

                    ExcelCommonUtils.writeCellValue(sheetWorking, indexRowDataDefault, colData++, valueStyle, moldRow.getMoldCode());
                    if (isViewHourly) {
                        ExcelCommonUtils.writeCellValue(sheetWorking, indexRowDataDefault, colData++, valueStyle, childHeader.getGroupHeader());
                        ExcelCommonUtils.writeCellValue(sheetWorking, indexRowDataDefault, colData++, valueStyle, childHeader.getChildHeader());
                    } else {
                        ExcelCommonUtils.writeCellValue(sheetWorking, indexRowDataDefault, colData++, valueStyle, childHeader.getChildHeader());
                    }

                    if (isCycleTimeExport) {
                        ExcelCommonUtils.writeCellValue(sheetWorking, indexRowDataDefault, colData++, valueStyle, moldRow.getApprovedCT());
                    }
                    if (isUptimeExport)
                        ExcelCommonUtils.writeCellValue(sheetWorking, indexRowDataDefault, colData++, valueStyle, moldRow.getUptimeTarget());
                    ExcelCommonUtils.getOrCreateRow(sheetWorking, indexRowDataDefault).setHeight((short) -1);//auto resize
                    indexRowDataDefault++;
                }
            }
            log.info("End write data column default to per sheet");

            final int rowData = rowDataTemp;
            log.info("Start write data to per sheet");

//            final int numRowPerMold = 1;
            //Export Data
            if (isViewHourly) {
                groupHeaderList.stream().forEach(groupHeader -> {
                    Sheet sheetWorking = mapSheet.get(groupHeader.getGroupHeader());
                    final int[] startRow = {rowData};
                    final int[] index = {1};
                    final int[] indexCell = {0};
                    //for resize
                    TwoObject<Boolean, Integer> existWarningAndColsize = new TwoObject<>(false, 1);
                    ExcelCommonUtils.getOrCreateRow(sheetWorking, startRow[0]).setHeight((short) -1);//auto resize height

                    List<MoldItem> childHearList = mapChildHeader.get(groupHeader.getGroupHeader());

                    boolean existsData = false;
                    if (!moldRow.isNewCounter())
                        existsData = childHearList.stream().filter(childHear -> {
                            List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                    .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                    .collect(Collectors.toList());
                            if (itemChildListValue != null && itemChildListValue.size() > 0) return true;
                            return false;
                        }).findAny().isPresent();

                    //check new counter
                    if (!moldRow.isNewCounter() && (!existsData || !StringUtils.isEmpty(moldRow.getCounterCode()))) {
//                            final int totalColData = childHearList.stream().map(h -> h.getMaxItemInHour()*fieldHearList.size()).reduce(0, (a, b) -> a + b);
                        final int totalColData = indexDynamicData[0] - colDynamicDataStart[0];
                        String message = "No data available because the tooling is not attached to any counter";
                        if (!StringUtils.isEmpty(moldRow.getCounterCode()))
                            message = "No hourly data available because the tooling is attached to old generation counter (Sensor ID: " + moldRow.getCounterCode() + ")";
                        ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + (childHearList.size() > 0 ? (childHearList.size() - 1) : childHearList.size())
                                , colDynamicDataStart[0], totalColData > 0 ? indexDynamicData[0] - 1 : indexDynamicData[0]
                                , warningStyle
                                , message);
                        existWarningAndColsize.setLeft(true);
                        existWarningAndColsize.setRight(totalColData);
                    } else {
                        childHearList.forEach(childHear -> {
                            int startRowChild = startRow[0];
                            indexCell[0] = colDynamicDataStart[0];
                            List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                    .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                    .collect(Collectors.toList());

                            String cycleTime = "";
                            String shot = "";
                            String uptime = "";
                            String temp = "";
                            CellStyle cellStyle = valueStyle;
                            CellStyle cellStyleUptime = valueStyle;
                            if (itemChildListValue.size() > 0) {
                                MoldItem item = itemChildListValue.get(0);
                                cycleTime = item.getCt();
                                shot = item.getShortCount();
                                uptime = item.getUptimeHour();
                                temp = item.getTemperature();
                                if (CycleTimeStatus.OUTSIDE_L2.equals(item.getCycleTimeStatus())) {
                                    cellStyle = valueStyleL2;
                                } else if (CycleTimeStatus.OUTSIDE_L1.equals(item.getCycleTimeStatus())) {
                                    cellStyle = valueStyleL1;
                                }
                                if (EfficiencyStatus.OUTSIDE_L2.equals(item.getUptimeStatus())) {
                                    cellStyleUptime = valueStyleL2;
                                } else if (EfficiencyStatus.OUTSIDE_L1.equals(item.getUptimeStatus())) {
                                    cellStyleUptime = valueStyleL1;
                                }
                            }
                            int colSubIndex = 0 * fieldHearList.size();
                            if (isCycleTimeExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyle, cycleTime);
                            if (isUptimeExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyleUptime, uptime);
                            if (isTempExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, temp);
                            if (isShotCountExport) {
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, shot);
                            }
                            startRow[0]++;
                        });
                    }
                    if (existWarningAndColsize.getLeft() && existWarningAndColsize.getRight()*childHearList.size() <6) {
//                        sheetWorking.setColumnWidth(colDynamicDataStart[0], 10000);
                        if(existWarningAndColsize.getRight()>1) {
                            for (int c = 0; c < existWarningAndColsize.getRight(); c++) {
                                sheetWorking.setColumnWidth(colDynamicDataStart[0] + c, WIDTH_WARNING / (childHearList.size()>0?childHearList.size():1)/existWarningAndColsize.getRight());
                            }
                        } else {
                            sheetWorking.autoSizeColumn(colDynamicDataStart[0], true);
                        }

                    }

                });
            } else {
                Sheet sheetWorking = sheet;
                final int[] startRow = {rowData};
//                final int[] index = {1};
                final int[] indexCell = {0};
//                List<MoldItem> childHearListAll=new ArrayList<>();
                final int[] totalRowDataArr = {0};
                mapChildHeader.keySet().stream().map(k -> mapChildHeader.get(k)).forEach(childHeaderList -> {
                    totalRowDataArr[0] += childHeaderList.stream().map(h -> h.getMaxItemInHour()).reduce(0, (a, b) -> a + b);
                });
                TwoObject<Boolean, Integer> existWarningAndColsize = new TwoObject<>(false, 1);
                ExcelCommonUtils.getOrCreateRow(sheetWorking, startRow[0]).setHeight((short) -1);//auto resize height
                boolean existsData = moldRow.getMoldItemList().size() > 0;
                final int totalRowData = totalRowDataArr[0];
                //check new counter
                if (!existsData && StringUtils.isEmpty(moldRow.getCounterCode())) {
                    String message = "No data available because the tooling is not attached to any counter";
                    final int totalColData = indexDynamicData[0] - colDynamicDataStart[0];
                    ExcelCommonUtils.mergedRegionAndSetVal(sheetWorking, startRow[0], startRow[0] + totalRowData - 1
                            , colDynamicDataStart[0], totalColData > 0 ? indexDynamicData[0] - 1 : indexDynamicData[0]
                            , warningStyle
                            , message);
                    existWarningAndColsize.setLeft(true);
                    existWarningAndColsize.setRight(totalColData);
                } else {
                    groupHeaderList.stream().forEach(groupHeader -> {
                        List<MoldItem> childHearList = mapChildHeader.get(groupHeader.getGroupHeader());
                        childHearList.forEach(childHear -> {
                            int startRowChild = startRow[0];
                            indexCell[0] = colDynamicDataStart[0];

                            List<MoldItem> itemChildListValue = moldRow.getMoldItemList().stream()
                                    .filter(item -> childHear.getKeyHeaderChild().equalsIgnoreCase(item.getKeyHeaderChild()))
                                    .collect(Collectors.toList());//1 item
                            String cycleTime = "";
                            String shot = "";
                            String uptime = "";
                            String temp = "";
                            CellStyle cellStyle = valueStyle;
                            CellStyle cellStyleUptime = valueStyle;
                            if (itemChildListValue.size() > 0) {
                                MoldItem item = itemChildListValue.get(0);
                                cycleTime = item.getCt();
                                shot = item.getShortCount();
                                uptime = item.getUptimeHour();
                                temp = item.getTemperature();
                                if (CycleTimeStatus.OUTSIDE_L2.equals(item.getCycleTimeStatus())) {
                                    cellStyle = valueStyleL2;
                                } else if (CycleTimeStatus.OUTSIDE_L1.equals(item.getCycleTimeStatus())) {
                                    cellStyle = valueStyleL1;
                                }
                                if (EfficiencyStatus.OUTSIDE_L2.equals(item.getUptimeStatus())) {
                                    cellStyleUptime = valueStyleL2;
                                } else if (EfficiencyStatus.OUTSIDE_L1.equals(item.getUptimeStatus())) {
                                    cellStyleUptime = valueStyleL1;
                                }
                            }
                            int colSubIndex = 0 * fieldHearList.size();
                            if (isCycleTimeExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyle, cycleTime);
                            if (isUptimeExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, cellStyleUptime, uptime);
                            if (isTempExport)
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, temp);
                            if (isShotCountExport) {
                                ExcelCommonUtils.writeCellValue(sheetWorking, startRowChild, indexCell[0] + colSubIndex++, valueStyle, shot);
                            }
                            startRow[0]++;
                        });

                    });
                }
                if (existWarningAndColsize.getLeft() && existWarningAndColsize.getRight()*totalRowData <6) {
                    sheetWorking.setColumnWidth(colDynamicDataStart[0], 10000);
                    if(existWarningAndColsize.getRight()>1) {
                        for (int c = 0; c < existWarningAndColsize.getRight(); c++) {
                            sheetWorking.setColumnWidth(colDynamicDataStart[0] + c, WIDTH_WARNING /(totalRowData>0?totalRowData:1) /existWarningAndColsize.getRight());
                        }
                    } else {
                        sheetWorking.autoSizeColumn(colDynamicDataStart[0], true);
                    }
                }

            }
            log.info("End write data to per sheet");
            workbook.write(outputStream);
            workbook.close();
            log.info("End exportExcelDynamicPerMold mold: {}",moldRow.getMoldCode());

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(outputStream!=null) outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                if(fos!=null) fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

//        return outputStream;
        return tempPathFile;
    }

    public void exportDynamicEveryShot(HttpServletResponse response, ExportPayload payload, Pageable pageable) throws IOException {
		BeanUtils.get(TolDatService.class).export(//
				TolDatGetIn.builder().build(), //
				TimeSetting.builder().timeScale(TimeScale.DATE).timeValue(payload.getFromDate()).build(), //
				ObjectUtils.isEmpty(payload.getIds()) ? //
						BatchIn.builder().selectionMode(MasterFilterMode.UNSELECTED).build()//
						: BatchIn.builder().selectionMode(MasterFilterMode.SELECTED).selectedIds(payload.getIds()).build(), //
				response);

//		List<Mold> moldList = getMoldListExport(payload, pageable);
//		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//				ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
//				OutputStream outputStream = response.getOutputStream();) {
//			if (CollectionUtils.isNotEmpty(moldList) && moldList.size() > 1) {
//				for (Mold mold : moldList) {
//					exportDynamicEveryShotSingleTooling(mold, payload.getFromDate(), zipOutputStream, response, false);
//				}
//				zipOutputStream.close();
//				response.setContentType("application/octet-stream");
//				response.setHeader("Content-Disposition",
//						"attachment; filename=" + moldList.get(0).getEquipmentCode() + "+" + (moldList.size() - 1) + "_dynamic_" + payload.getFromDate() + ".zip");
//				outputStream.write(byteArrayOutputStream.toByteArray());
//			} else if (CollectionUtils.isNotEmpty(moldList) && moldList.size() == 1) {
//				exportDynamicEveryShotSingleTooling(moldList.get(0), payload.getFromDate(), zipOutputStream, response, true);
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

    public static String replaceSpecialCharacter(String source) {
        return source.replace("/","_")
                .replace("\\","_")
                .replace("*","_")
                .replace("|","_")
                .replace(":","_")
                .replace("<","_")
                .replace(">","_")
                .replace(",","_")
                .replace("?","_");
    }

    public void exportDynamicEveryShotSingleTooling(Mold mold, String date, ZipOutputStream zipOutputStream, HttpServletResponse response, boolean exportOneTooling) throws IOException {
        try {
            final int COLUMN_WIDTH_LARGE = 4000;
            final int COLUMN_WIDTH_SMALL = 3000;

            String replaceEquipmentCode = replaceSpecialCharacter(mold.getEquipmentCode());
            InputStream fileTemplate = ExcelUtils.getFileTemplateExcel("dynamic/TemplateExportEveryShot.xlsx");
            XSSFWorkbook templateWorkbook = new XSSFWorkbook(fileTemplate);
            Sheet templateSheet = templateWorkbook.getSheetAt(0);
            Workbook wb = new SXSSFWorkbook(1000);
            Sheet sheet = wb.createSheet(replaceEquipmentCode.concat("_").concat(date));

            //prepare style
//            copyCellWidth(templateSheet, sheet, 0, 8);
            CellStyle headerLabelStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 0);
            CellStyle headerValueStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 1);
            CellStyle noteStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 5);
            CellStyle outsideL1Style = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 6);
            CellStyle outsideL1BorderStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 6);
            CellStyle outsideL2Style = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 8);
            CellStyle outsideL2BorderStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 0, 8);
            CellStyle outsideIDLEStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 1, 6);
            CellStyle outsideIDLEBorderStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 1, 6);
            CellStyle headerDataUnitStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 4, 1);
            CellStyle valueDataUnitStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 5, 1);
            CellStyle headerTableStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 8, 0);
            CellStyle dataTableStyle = ExcelCommonUtils.cloneCellStyleFromOtherSheet(templateSheet, sheet, 11, 0);
            ExcelCommonUtils.setFullBorderStyle(headerDataUnitStyle);
            ExcelCommonUtils.setFullBorderStyle(valueDataUnitStyle);
            ExcelCommonUtils.setFullBorderStyle(headerTableStyle);
            ExcelCommonUtils.setFullBorderStyle(dataTableStyle);
            ExcelCommonUtils.setFullBorderStyle(outsideL1BorderStyle);
            ExcelCommonUtils.setFullBorderStyle(outsideL2BorderStyle);
            ExcelCommonUtils.setFullBorderStyle(outsideIDLEBorderStyle);
            sheet.setColumnWidth(0, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(1, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(6, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(7, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(8, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(2, COLUMN_WIDTH_SMALL);
            sheet.setColumnWidth(3, COLUMN_WIDTH_SMALL);
            sheet.setColumnWidth(4, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(5, COLUMN_WIDTH_LARGE);
            sheet.setColumnWidth(10, COLUMN_WIDTH_LARGE);

            ExcelCommonUtils.writeCellValue(sheet, 0, 0, headerLabelStyle, "Tooling ID");
            ExcelCommonUtils.writeCellValue(sheet, 0, 1, headerValueStyle, mold.getEquipmentCode());
            ExcelCommonUtils.writeCellValue(sheet, 1, 0, headerLabelStyle, "Date");
            ExcelCommonUtils.writeCellValue(sheet, 1, 1, headerValueStyle, DateUtils.convertStringDateFormat(date, DateUtils.YYYY_MM_DD_DATE_FORMAT, "MMMM dd yyyy"));
            ExcelCommonUtils.writeCellValue(sheet, 2, 0, headerLabelStyle, "Method");
            ExcelCommonUtils.writeCellValue(sheet, 2, 1, headerValueStyle, "Every Shot");

            ExcelCommonUtils.writeCellValue(sheet, 0, 5, noteStyle, "Note");
            ExcelCommonUtils.writeCellValue(sheet, 0, 6, outsideL1Style, "Outside L1");
            ExcelCommonUtils.writeCellValue(sheet, 0, 8, outsideL2Style, "Outside L2");
            ExcelCommonUtils.writeCellValue(sheet, 0, 10, outsideIDLEStyle, "IDLE");

            ExcelCommonUtils.writeCellValue(sheet, 4, 0, headerDataUnitStyle, "Data");
            ExcelCommonUtils.writeCellValue(sheet, 4, 1, headerDataUnitStyle, "Unit");
            ExcelCommonUtils.writeCellValue(sheet, 5, 0, valueDataUnitStyle, "Cycle Time");
            ExcelCommonUtils.writeCellValue(sheet, 5, 1, valueDataUnitStyle, "Second ");
            ExcelCommonUtils.writeCellValue(sheet, 6, 0, valueDataUnitStyle, "Temperature");
            ExcelCommonUtils.writeCellValue(sheet, 6, 1, valueDataUnitStyle, "Celsius");
            ExcelCommonUtils.writeCellValue(sheet, 7, 0, valueDataUnitStyle, "Injection Time");
            ExcelCommonUtils.writeCellValue(sheet, 7, 1, valueDataUnitStyle, "Second");
            ExcelCommonUtils.writeCellValue(sheet, 8, 0, valueDataUnitStyle, "Packing Time");
            ExcelCommonUtils.writeCellValue(sheet, 8, 1, valueDataUnitStyle, "Second");
            ExcelCommonUtils.writeCellValue(sheet, 9, 0, valueDataUnitStyle, "Cooling Time");
            ExcelCommonUtils.writeCellValue(sheet, 9, 1, valueDataUnitStyle, "Second");


            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11,0, 0,headerTableStyle, "Hour");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11,1,1, headerTableStyle, "Approved CT");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11, 2,2,headerTableStyle, "WACT");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11, 3,3,headerTableStyle, "Actual CT");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11, 4,4,headerTableStyle, "Temperature");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11, 5,5,headerTableStyle, "Injection Time");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11, 6,6,headerTableStyle, "Packing Time");
            ExcelCommonUtils.mergedRegionAndSetVal(sheet, 10, 11, 7,7,headerTableStyle, "Cooling Time");

            DecimalFormat decimalFormat = new DecimalFormat("####0.0");

            int startDataIndex = 12;
            int page = 0;
            boolean isDone = false;
            while (!isDone) {
				TolDatGetOut dataOut = BeanUtils.get(TolDatService.class).get(//
						TolDatGetIn.builder().moldId(mold.getId()).build(), //
						TimeSetting.builder().timeScale(TimeScale.DATE).timeValue(date).build(), //
						PageRequest.of(page, 100));
                isDone = dataOut.isLast();

                for (TolDatGetOut.MldDatItem item : dataOut.getContent()) {
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 0, dataTableStyle, DateUtils.convertStringDateFormat(item.getTime(), DateUtils.DEFAULT_DATE_FORMAT, "H:mm:ss"));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 1, dataTableStyle, item.getApprovedCt() == null ? "" : decimalFormat.format(item.getApprovedCt()));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 2, dataTableStyle, item.getWact() == null ? "" : decimalFormat.format(item.getWact()));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 3, item.isIdle() ? outsideIDLEBorderStyle : item.isOutOfL1() ? outsideL1BorderStyle : item.isOutOfL2() ? outsideL2BorderStyle : dataTableStyle, item.getActualCt() == null ? "" : decimalFormat.format(item.getActualCt()));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 4, dataTableStyle, item.getTemperature() == null ? "" : decimalFormat.format(item.getTemperature()));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 5, dataTableStyle, item.getInjectionTime() == null ? "" : decimalFormat.format(item.getInjectionTime()));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 6, dataTableStyle, item.getPackingTime() == null ? "" : decimalFormat.format(item.getPackingTime()));
                    ExcelCommonUtils.writeCellValue(sheet, startDataIndex, 7, dataTableStyle, item.getCoolingTime() == null ? "" : decimalFormat.format(item.getCoolingTime()));
                    startDataIndex++;
                }
                page++;
            }

            if (exportOneTooling) {
                HttpUtils.respondWorkbook(wb, replaceEquipmentCode+"_"+date+".xlsx", response);
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                wb.write(bos);
                byte[] byteArray = bos.toByteArray();
                ZipEntry zipEntry = new ZipEntry(replaceEquipmentCode+"_"+date+".xlsx");
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(byteArray);
                zipOutputStream.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyCellWidth(Sheet sourceSheet, Sheet targetSheet, int fromColIndex, int toColIndex) {
        for (int i = fromColIndex; i <= toColIndex; i++) {
            double columnWidth = SheetUtil.getColumnWidth(sourceSheet, i, false);
            if (columnWidth > 0) {
                targetSheet.setColumnWidth(i, (int)Math.round(columnWidth * 256));
            }
        }
    }

    private List<Mold> getMoldListExport(final ExportPayload exportPayload, Pageable pageable){
        List<Mold> moldList;
        if (exportPayload.getIds() == null || exportPayload.getIds().size() == 0) {
            moldList = moldRepository.findAllByOrderByIdDesc();
        } else {
            if (pageable != null) {
                Pageable pageableNew =  PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
                pageable.getSort();
                MoldPayload payload = new MoldPayload();
                payload.setIds(exportPayload.getIds());
                QueryUtils.includeDisabled(Q.mold);
                Page<Mold> pageContent = moldService.findAll(payload.getPredicate(), pageableNew, false, null);
                moldList = pageContent.getContent();
            } else
                moldList = moldRepository.findByIdInOrderByIdDesc(exportPayload.getIds());
        }

        return moldList;
    }

    public Boolean isExportOneToolingPerFile() {
        return exportOneToolingPerFile!=null?exportOneToolingPerFile:false;
    }
}
