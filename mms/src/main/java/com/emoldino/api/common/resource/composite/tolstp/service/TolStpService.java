package com.emoldino.api.common.resource.composite.tolstp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.xspec.B;
import com.emoldino.api.common.resource.composite.tolstp.dto.*;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.api.analysis.resource.composite.toldat.service.TolDatService;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.base.object.util.EmObjectUtils;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.api.common.resource.composite.tolstp.enumeration.TolStpDataFrequency;
import com.emoldino.api.common.resource.composite.tolstp.repository.TolStpRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.accessHierachy.AccessHierarchyRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.mold.DynamicExportService;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.ExportPayload;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.api.versioning.service.VersioningService;
import saleson.common.config.Const;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.RangeType;
import saleson.common.util.ExcelUtils;
import saleson.common.util.FileUtils;
import saleson.dto.common.TwoObject;
import saleson.dto.exports.dynamic.MoldRow;
import saleson.model.*;
import saleson.model.data.CompanyLiteData;
import saleson.service.util.NumberUtils;

@Slf4j
@Service
public class TolStpService {

	@Transactional
	public TolStpGetOut get(TolStpGetIn input, Pageable pageable) {
		Page<TolStpItem> page = get(input, null, pageable);
		List<Tab> tabs = QueryUtils.findTabs(ObjectType.TOOLING, input, page, //
				countin -> BeanUtils.get(TolStpRepository.class).count(countin));
		return new TolStpGetOut(page, tabs);
	}

	private Page<TolStpItem> get(TolStpGetIn input, BatchIn batchin, Pageable pageable) {
		Page<TolStpItem> page = BeanUtils.get(TolStpRepository.class).findAll(input, batchin, pageable);
		List<TolStpItem> tolStpItems = page.getContent();
		loadCustomFieldValues(page);
//		loadAccumulatedShot(tolStpItems, input.getAccumulatedShotFilter());//need to calculator because accumulatedShot is incorrect due to join with moldPart(one-to-many)
		loadUpDepreciation(tolStpItems);
		// loadCustomFieldForPart remove this
		loadUpperTierCompanies(tolStpItems);
		loadEngineersInfo(tolStpItems);
		return page;
	}

	private void loadCustomFieldValues(Page<TolStpItem> page) {
		Map<Long, TolStpItem> map = page.getContent().stream().collect(Collectors.toMap(TolStpItem::getId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
		EmObjectUtils.loadCustomFieldValues(ObjectType.TOOLING, new ArrayList<>(map.keySet()), (objectId, fields) -> map.get(objectId).setCustomFields(fields));
	}

	/*
	private void loadAccumulatedShot(List<TolStpItem> tolStpItems, String year) {
	    List<Long> ids = tolStpItems.stream().map(TolStpItem::getId).collect(Collectors.toList());
	    List<TolStpItem.AccumulatedShot> moldAccumulatedData = StringUtils.isEmpty(year) ? null : BeanUtils.get(TolStpRepository.class).findMoldAccumulatedShot(year, ids);
	    tolStpItems.forEach(stpItem -> {
	        if (moldAccumulatedData == null) stpItem.setAccumulatedShot(stpItem.getLastShot());
	        else {
	            moldAccumulatedData.forEach(mData -> {
	                if (mData.getMoldId().equals(stpItem.getId())) {
	                    stpItem.setAccumulatedShot(mData.getAccumulatedShot());
	                }
	            });
	        }
	    });
	}
	*/

	private void loadUpDepreciation(List<TolStpItem> tolStpItems) {
		tolStpItems.forEach(tolStpItem -> {
			String latestDepreciationPoint = DateUtils2.format(tolStpItem.getUpLatestDepreciationPoint(), DateUtils2.DatePattern.yyyyMMddHHmmss, DateUtils2.Zone.SYS);
			TolStpItem.AccumulatedShot data = BeanUtils.get(TolStpRepository.class).findMoldAccumulatedShotByLstLessThan(latestDepreciationPoint, tolStpItem.getId());
			if (tolStpItem.getLastShot() != null && tolStpItem.getLastShot() >= tolStpItem.getDesignedShot()) {
				tolStpItem.setUpDepreciation(tolStpItem.getLastShot() == null ? 0D : tolStpItem.getLastShot().doubleValue());
			} else {
				Double upDepreciation = data == null ? 0D : (data.getAccumulatedShot() == null ? 0D : data.getAccumulatedShot().doubleValue());
				tolStpItem.setUpDepreciation(tolStpItem.getLastShot() == null ? 0D : (upDepreciation > tolStpItem.getLastShot() ? tolStpItem.getLastShot() : upDepreciation));
			}

			if (tolStpItem.getLastShot() != null && tolStpItem.getLastShot() >= tolStpItem.getDesignedShot()) {
				tolStpItem.setUpCurrentBookValue(tolStpItem.getSalvageValue());
			} else {
				double assetCost = tolStpItem.getCost() != null ? tolStpItem.getCost() : 0;
				tolStpItem.setUpCurrentBookValue(
						ValueUtils.max(NumberUtils.roundOffNumber(assetCost - (tolStpItem.getUpDepreciationPerShotRaw() * tolStpItem.getUpDepreciation())), 0D));
			}
		});
	}

	private void loadUpperTierCompanies(List<TolStpItem> tolStpItems) {
		tolStpItems.forEach(tolStpItem -> {
			Optional<AccessHierarchy> optional = BeanUtils.get(AccessHierarchyRepository.class).findFirstByCompanyId(tolStpItem.getCompanyIdByLocation());
			if (optional.isPresent()) {
				List<Long> parentIds = optional.get().getAccessCompanyParentRelations().stream().map(AccessCompanyRelation::getCompanyParentId).collect(Collectors.toList());
				tolStpItem.setUpperTierCompanies(BeanUtils.get(CompanyRepository.class).findByIdIn(parentIds).stream().map(CompanyLiteData::new).collect(Collectors.toList()));
			}
		});
	}

	private void loadEngineersInfo(List<TolStpItem> tolStpItems) {
		UserParam param = new UserParam();
		param.setStatus("active");
		List<Long> allowedEngineerIds = BeanUtils.get(UserService.class).getAllIds(param);
		tolStpItems.forEach(m -> {
			m.setEngineers(BeanUtils.get(TolStpRepository.class).getEngineersInCharge(m.getId(), allowedEngineerIds).stream().map(UserLiteDTO::new).collect(Collectors.toList()));
			m.setPlantEngineers(
					BeanUtils.get(TolStpRepository.class).getPlantEngineersInCharge(m.getId(), allowedEngineerIds).stream().map(UserLiteDTO::new).collect(Collectors.toList()));
		});
	}

	@Transactional
	public void disable(List<Long> ids) {
		saveEnabled(ids, false);
	}

	@Transactional
	public void enable(List<Long> ids) {
		saveEnabled(ids, true);
	}

	private void saveEnabled(List<Long> ids, boolean enabled) {
		if (ObjectUtils.isEmpty(ids) || ids.size() > 100) {
			return;
		}
		for (Long id : ids) {
			Mold data = findById(id);
			data.setEnabled(enabled);
			save(data);
		}
	}
	/*
	private Mold2 findById2(Long id) {
		return BeanUtils.get(Mold2Repository.class).findById(id).orElseThrow(
				() -> DataUtils.newDataNotFoundException(Mold.class, "id", id));
	}
	
	private void save2(Mold2 data) {
		BeanUtils.get(Mold2Repository.class).save(data);
	}
	 */

	public void disableBatch(TolStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(TolStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	private void saveEnabledBatch(TolStpGetIn input, BatchIn batchin, Boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Mold mold = findById(item.getId());
			mold.setEnabled(enabled);
			save(mold);
		}, false);
	}

	public void postNoteBatch(TolStpGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.TOOLING_SETTING, item.getId(), body), true);
	}

	private void runBatch(TolStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<TolStpItem> closure, boolean pageUpRequired) {
		DataUtils.runBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(TolStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	private Mold findById(Long id) {
		return BeanUtils.get(MoldRepository.class).findById(id)//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Mold.class, "id", id));
	}

	@Transactional
	public void save(Mold data) {
		BeanUtils.get(MoldService.class).save(data);
		Mold dataGet = BeanUtils.get(MoldService.class).findById(data.getId());
		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
	}

	@Transactional
	public void export(TolStpGetIn input, BatchIn batchin, Sort sort, HttpServletResponse response) {
		DatExpUtils.exportByJxls("TOOLING", //
				pageable2 -> BeanUtils.get(TolStpService.class).get(input, batchin, pageable2), //
				100, sort == null || sort.isUnsorted() ? Sort.by("equipmentCode") : sort, //
				"Tooling", response//
		);
	}

	@Deprecated
	// TODO removed after it is replaced with export(TolStpGetIn input, BatchIn batchin, Sort sort, HttpServletResponse response) method
	public ByteArrayOutputStream exportStatic(TolStpExportDataIn input, Integer timezoneOffsetClient, BatchIn batchin, Pageable pageable) throws IOException {
		populate(input, batchin);
		List<Mold> moldList = getAllMoldListForExport(input, batchin, pageable);
		//todo: improve export excel
		return BeanUtils.get(ExcelUtils.class).exportExcelToolingDetailNew(moldList, timezoneOffsetClient);
	}

	public void exportDynamicData(TolStpExportDataIn input, TimeSetting timeSetting, BatchIn batchin, Pageable pageable, HttpServletResponse response) {
		populate(input, batchin);
		if (TolStpDataFrequency.EVERY_SHOT == input.getDataFrequency()) {
			BeanUtils.get(TolDatService.class).export(//
					TolDatGetIn.builder().build(), //
					timeSetting, //
					batchin, //
					response);
		} else {
			BeanUtils.get(TolStpService.class).exportExcelDynamicOneToolingPerFile(input, timeSetting, batchin, pageable, response);
		}
	}

	@Deprecated
	// TODO Remove after mode -> selectionMode applied
	private void populate(TolStpExportDataIn input, BatchIn batchin) {
		if (batchin.getSelectionMode() == null) {
			batchin.setSelectionMode(input.getMode());
		}
	}

//	@Deprecated
//	private void exportDynamicEveryShot(TolStpExportDataIn input, TimeSetting timeSetting, BatchIn batchin, Pageable pageable, HttpServletResponse response) {
//		List<Mold> moldList = getAllMoldListForExport(input, batchin, pageable);
//
//		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//				ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
//				OutputStream outputStream = response.getOutputStream();) {
//			if (CollectionUtils.isNotEmpty(moldList) && moldList.size() > 1) {
//				for (Mold mold : moldList) {
//					BeanUtils.get(DynamicExportService.class).exportDynamicEveryShotSingleTooling(mold, timeSetting.getFromDate(), zipOutputStream, response, false);
//				}
//				zipOutputStream.close();
//				response.setContentType("application/octet-stream");
//				response.setHeader("Content-Disposition",
//						"attachment; filename=" + moldList.get(0).getEquipmentCode() + "+" + (moldList.size() - 1) + "_dynamic_" + timeSetting.getFromDate() + ".zip");
//				outputStream.write(byteArrayOutputStream.toByteArray());
//			} else if (CollectionUtils.isNotEmpty(moldList) && moldList.size() == 1) {
//				BeanUtils.get(DynamicExportService.class).exportDynamicEveryShotSingleTooling(moldList.get(0), timeSetting.getFromDate(), zipOutputStream, response, true);
//			}
//
//		} catch (IOException e) {
//			throw ValueUtils.toAe(e, "EXPORT_DYNAMIC_FAIL");
//		}
//	}

	@Deprecated
	// TODO improve later
	private void exportExcelDynamicOneToolingPerFile(TolStpExportDataIn input, TimeSetting timeSetting, BatchIn batchin, Pageable pageable, HttpServletResponse response) {

		validExportExcelDynamic(input, timeSetting);

		ExportPayload payload = converToExportPayload(null, input, timeSetting);//temporary
		List<MoldRow> moldRowList = prepareDataForFile(payload, input, timeSetting, batchin, pageable);
		ByteArrayOutputStream byteArrayOutputStream = null;
		OutputStream outputStream = null;
		try {
			String fileName = moldRowList.size() != 1 ? BeanUtils.get(DynamicExportService.class).getExportDynamicFileName(payload).replace(".xlsx", ".zip")
					: BeanUtils.get(DynamicExportService.class).getExportDynamicFileNamePerTooling(payload, moldRowList.get(0).getMoldCode());
			log.info("start make file export for {} tooling ", moldRowList.size());
			if (moldRowList.size() == 1) {
				Path pathOut = BeanUtils.get(DynamicExportService.class).exportExcelDynamicPerMold(moldRowList.get(0), payload);
				if (pathOut != null) {
					byteArrayOutputStream = new ByteArrayOutputStream();
					byteArrayOutputStream.write(Files.readAllBytes(pathOut));
					Files.delete(pathOut);
				}
			} else {
				List<TwoObject<String, Path>> listDataFile = moldRowList.stream()
						.map(moldRow -> TwoObject.of(BeanUtils.get(DynamicExportService.class).getExportDynamicFileNamePerTooling(payload, moldRow.getMoldCode()),
								BeanUtils.get(DynamicExportService.class).exportExcelDynamicPerMold(moldRow, payload)))
						.collect(Collectors.toList());
				log.info("Done exportExcelDynamicOneToolingPerFile build list temp excel files");
				log.info("Start convert list temp excel files to byteArray Zip file");
				//convert to valid name
				final List<String> originalNames = listDataFile.stream().map(p -> p.getLeft()).collect(Collectors.toList());
				listDataFile.stream().forEach(p -> {
					p.setLeft(FileUtils.getValidFileNameInList(p.getLeft(), originalNames));
				});
				byteArrayOutputStream = FileUtils.zipMultipleFiles(listDataFile);
				listDataFile.stream().forEach(dataFile -> {
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
		} catch (Exception e) {
			throw ValueUtils.toAe(e, "EXPORT_DYNAMIC_FAIL");
		} finally {
			ValueUtils.closeQuietly(byteArrayOutputStream);
			ValueUtils.closeQuietly(outputStream);
		}
	}

	@Deprecated
	private void validExportExcelDynamic(TolStpExportDataIn input, TimeSetting timeSetting) {
		if (Arrays.asList(TimeScale.CUSTOM).contains(timeSetting.getTimeScale())) {
			ValueUtils.assertNotEmpty(timeSetting.getFromDate(), "From Date");
			ValueUtils.assertNotEmpty(timeSetting.getToDate(), "To Date");
		} else {
			ValueUtils.assertNotEmpty(timeSetting.getTimeValue(), "Time");
		}
		int dataTypeAbleForHourly = 0;
		if (input.isShotCountEnabled()) {
			dataTypeAbleForHourly++;
		}
		if (input.isCycleTimeEnabled()) {
			dataTypeAbleForHourly++;
		}
		if (TimeScale.HOUR.equals(timeSetting.getTimeScale()) && input.isUptimeEnabled() && dataTypeAbleForHourly == 0) {
			throw new BizException("Uptime type not exist Hourly frequency data.");
		}
		if (!TimeScale.HOUR.equals(timeSetting.getTimeScale()) && input.isTemperatureEnabled() && dataTypeAbleForHourly == 0) {
			throw new BizException("Temperature type exists only Hourly frequency data.");
		}
	}

	private List<Mold> getAllMoldListForExport(TolStpExportDataIn input, BatchIn batchin, Pageable pageable) {
		List<Mold> moldList = new ArrayList<>();
//		if (input.getMode() == null) {
//			moldList = BeanUtils.get(MoldRepository.class).findAllByOrderByIdDesc();
//		} else {
		List<Long> ids;
		Pageable pageableNew = Pageable.unpaged();
		if (pageable != null && pageable.getSort() != null) {
			if (pageable.getSort() != null) {
				pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
			}
		} else {
			pageableNew = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.ASC, "id");
		}
		if (batchin.getSelectionMode() == null || batchin.getSelectionMode().equals(MasterFilterMode.SELECTED)) {
			ids = batchin.getSelectedIds();
		} else {
			Page<TolStpItem> page = BeanUtils.get(TolStpRepository.class).findAll(input, null, pageableNew);
			ids = page.getContent().stream()//
					.map(item -> item.getId())//
					.filter(id -> batchin.getUnselectedIds() == null || !batchin.getUnselectedIds().contains(id))//
					.collect(Collectors.toList());
		}
		if (ids != null) {
			moldList = BeanUtils.get(MoldRepository.class).findAllByIdIn(ids, pageableNew);
		}
//		}
		return moldList;
	}

	//todo: temporary implement
	private ExportPayload converToExportPayload(List<Long> ids, TolStpExportDataIn input, TimeSetting timeSetting) {
		ExportPayload payload = new ExportPayload();
		payload.setIds(ids);
		payload.setTime(timeSetting.getTimeValue());
		payload.setFromDate(timeSetting.getFromDate());
		payload.setToDate(timeSetting.getToDate());
		payload.setTimezoneOffsetClient(input.getTimezoneOffsetClient());

		DateViewType frequency = DateViewType.DAY;
		if (TolStpDataFrequency.HOURLY.equals(input.getDataFrequency())) {
			frequency = DateViewType.HOUR;
		} else if (TolStpDataFrequency.WEEKLY.equals(input.getDataFrequency())) {
			frequency = DateViewType.WEEK;
		} else if (TolStpDataFrequency.MONTHLY.equals(input.getDataFrequency())) {
			frequency = DateViewType.MONTH;
		} else if (TolStpDataFrequency.EVERY_SHOT.equals(input.getDataFrequency())) {
			frequency = DateViewType.EVERY_SHOT;
		}

		payload.setFrequency(frequency);
		RangeType rangeType = RangeType.CUSTOM_RANGE;
		if (TimeScale.WEEK.equals(timeSetting.getTimeScale())) {
			rangeType = RangeType.WEEKLY;
		} else if (TimeScale.MONTH.equals(timeSetting.getTimeScale())) {
			rangeType = RangeType.MONTHLY;
		} else if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
			rangeType = RangeType.YEARLY;
		}
		payload.setRangeType(rangeType);

		List<String> dataTypes = new ArrayList<>();
		if (input.isTemperatureEnabled()) {
			dataTypes.add(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE);
		}
		if (input.isShotCountEnabled()) {
			dataTypes.add(Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT);
		}
		if (input.isCycleTimeEnabled()) {
			dataTypes.add(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME);
		}
		if (input.isUptimeEnabled()) {
			dataTypes.add(Const.DYNAMIC_DATA.TOOLING.UPTIME);
		}
		payload.setDataTypes(dataTypes);

		return payload;
	}

	//todo: improve later
	private List<MoldRow> prepareDataForFile(final ExportPayload exportPayload, TolStpExportDataIn input, TimeSetting timeSetting, BatchIn batchin, Pageable pageable) {
		log.info("Start prepareDataForFile");
		List<Mold> moldList = getAllMoldListForExport(input, batchin, pageable);
		exportPayload.setIds(moldList.stream().map(m -> m.getId()).collect(Collectors.toList()));
		DynamicExportService.generateRageTimeFromTime(exportPayload);
		List<MoldRow> moldRowList = BeanUtils.get(DynamicExportService.class).getMoldRowDataNew(exportPayload, moldList);
		log.info("End prepareDataForFile {} molds", moldRowList.size());
		return moldRowList;
	}

	public void moveTabItemsBatch(TolStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.TOOLING, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.TOOLING, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}

	public void deleteTabItemsBatch(TolStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.TOOLING, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(TolStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(TolStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

}
