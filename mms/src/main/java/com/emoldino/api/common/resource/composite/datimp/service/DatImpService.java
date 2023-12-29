package com.emoldino.api.common.resource.composite.datimp.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.file.dto.FileDto;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.service.FileService;
import com.emoldino.api.common.resource.base.file.util.FileUtils;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.object.repository.customfield.CustomFieldRepository;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpJxlsEm;
import com.emoldino.api.common.resource.composite.datimp.dto.DatImpFileResult;
import com.emoldino.api.common.resource.composite.datimp.dto.DatImpPostIn;
import com.emoldino.api.common.resource.composite.datimp.dto.DatImpPostOut;
import com.emoldino.api.common.resource.composite.datimp.dto.DatImpSheetResult;
import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpCategoryOption;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpCompanyOption;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpMachineOption;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpPartOption;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpPlantOption;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpToolingOption;
import com.emoldino.api.common.resource.composite.datimp.service.resoption.DatImpUserOption;
import com.emoldino.api.common.resource.composite.datimp.util.PoiCopySheet;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1Param;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ResourceUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import saleson.api.category.payload.CategoryRequest;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.payload.PartPayload;
import saleson.api.user.payload.UserPayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.ExcelCommonUtils;
import saleson.dto.CustomField.CustomFieldDTO;
import saleson.dto.CustomField.CustomFieldListDTO;
import saleson.dto.CustomField.CustomFieldValueDTO;
import saleson.model.customField.CustomField;

@Service
@Slf4j
public class DatImpService {

	@Data
	@AllArgsConstructor
	public static class ResOption<V> {
		private List<String> sheetName;
		private ObjectType objectType;
		private Class<V> itemClass;
		private List<String> codeField;
		private Closure1ParamNoReturn<V> populte;
		private Closure1ParamNoReturn<V> before;
		private Closure1Param<V, Boolean> exists;
		private Closure1ParamNoReturn<V> post;
		private Closure1ParamNoReturn<V> put;
	}

	public static interface ResOptionGetter<T> {

		DatImpResourceType getResourceType();

		ResOption<T> getResOption();

	}

	private static final Map<DatImpResourceType, ResOption<?>> RESOURCE_TYPE;
	static {
		RESOURCE_TYPE = new LinkedHashMap<>();

		ResOptionGetter<CompanyPayload> companyOption = new DatImpCompanyOption();
		RESOURCE_TYPE.put(companyOption.getResourceType(), companyOption.getResOption());

		ResOptionGetter<CategoryRequest> categoryOption = new DatImpCategoryOption();
		RESOURCE_TYPE.put(categoryOption.getResourceType(), categoryOption.getResOption());

		ResOptionGetter<LocationPayload> plantOption = new DatImpPlantOption();
		RESOURCE_TYPE.put(plantOption.getResourceType(), plantOption.getResOption());

		ResOptionGetter<PartPayload> partOption = new DatImpPartOption();
		RESOURCE_TYPE.put(partOption.getResourceType(), partOption.getResOption());

		ResOptionGetter<UserPayload> userOption = new DatImpUserOption();
		RESOURCE_TYPE.put(userOption.getResourceType(), userOption.getResOption());

		ResOptionGetter<MachinePayload> machineOption = new DatImpMachineOption();
		RESOURCE_TYPE.put(machineOption.getResourceType(), machineOption.getResOption());

		ResOptionGetter<MoldPayload> toolingOption = new DatImpToolingOption();
		RESOURCE_TYPE.put(toolingOption.getResourceType(), toolingOption.getResOption());
	}

	public void getTemplates(HttpServletResponse response) {
		InputStream is = null;
		Workbook wb = null;
		Map<DatImpResourceType, File> files = null;
		try {
			// 1. Get Default Data Import Template
			is = ResourceUtils.getInputStream("/file/dataimport/data_import_template.xlsx");

			// 4. Replace Default Template -> Custom Template
			{
				wb = WorkbookFactory.create(is);
				ValueUtils.closeQuietly(is);

				for (DatImpResourceType resourceType : RESOURCE_TYPE.keySet()) {
					ResOption<?> options = RESOURCE_TYPE.get(resourceType);
					if (ObjectUtils.isEmpty(options.getSheetName())) {
						continue;
					}
					String groupCode = resourceType.name();
					File file;

					file = FileUtils.getReleasedByGroupTypeAndCode(FileGroupType.DATA_IMPORT_TEMPLATE, groupCode);
					if (file != null && file.exists()) {
						replace(wb, file, options);
						continue;
					}

					file = FileUtils.getReleasedByGroupTypeAndCode(FileGroupType.DATA_EXPORT_TEMPLATE, groupCode);
					if (file != null && file.exists()) {
						replace(wb, file, options);
						continue;
					}

					InputStream eis = ResourceUtils.getInputStream("/file/dataexport/" + groupCode + ".xlsx");
					if (eis != null) {
						replace(wb, eis, options);
						continue;
					}
				}

//				for (DatImpResourceType resourceType : files.keySet()) {
//					File file = files.get(resourceType);
//					ResOption<?> options = RESOURCE_TYPE.get(resourceType);
//					replace(wb, file, options);
//				}

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				wb.write(bos);
				is = new ByteArrayInputStream(bos.toByteArray());
			}

			// 5. Respond Custom Template
			respond(is, response);

		} catch (Exception e) {
			AbstractException ae = ValueUtils.toAe(e, "FILE_DOWN_FAIL");
			throw ae;
		} finally {
			ValueUtils.closeQuietly(is);
			ValueUtils.closeQuietly(wb);
//			if (!ObjectUtils.isEmpty(files)) {
//				try {
//					files.forEach((key, file) -> file.deleteOnExit());
//				} catch (Exception e) {
//					// Do Nothing
//				}
//			}
		}

	}

	private void replace(Workbook wb, File file, ResOption<?> options) throws Exception {
		Workbook wbCustom = WorkbookFactory.create(file);
		replace(wb, wbCustom, options);
	}

	private void replace(Workbook wb, InputStream is, ResOption<?> options) throws Exception {
		Workbook wbCustom = WorkbookFactory.create(is);
		replace(wb, wbCustom, options);
	}

	private void replace(Workbook wb, Workbook wbCustom, ResOption<?> options) throws Exception {
		try {
			for (String sheetName : options.getSheetName()) {
				int index = wbCustom.getSheetIndex(sheetName);
				if (index < 0) {
					continue;
				}

				Sheet sourceSheet = wbCustom.getSheetAt(index);
				int rindex = wb.getSheetIndex(sheetName);
				Sheet destinationSheet;
				if (rindex < 0) {
					destinationSheet = wb.createSheet(sheetName);
				} else {
					wb.removeSheetAt(rindex);
					destinationSheet = wb.createSheet(sheetName);
					if (wb.getSheetIndex(sheetName) != rindex) {
						wb.setSheetOrder(sheetName, rindex);
					}
				}
				PoiCopySheet.copySheet(sourceSheet, destinationSheet);
				break;
			}
		} finally {
			ValueUtils.closeQuietly(wbCustom);
		}
	}

	private void respond(InputStream is, HttpServletResponse response) throws Exception {
		Context context = new Context();
		context.putVar("output", Collections.emptyMap());
		context.putVar("content", Arrays.asList(new LinkedHashMap<>()));
		context.putVar("em", new DatExpJxlsEm());
		try (OutputStream os = response.getOutputStream()) {
			response.setContentType("application/msexcel");
			response.setHeader("Content-Disposition", "attachment; filename=Data Import Template.xlsx");
			JxlsHelper.getInstance()//
					.processTemplate(is, os, context);
		}
//		HttpUtils.respondFile(is, "Data Import Template.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response);
	}

	public DatImpPostOut post(DatImpPostIn input) {
		if (ObjectUtils.isEmpty(input.getFileRefs())) {
			return new DatImpPostOut();
		}

		DatImpPostOut output = new DatImpPostOut();
		for (FileDto fileRef : input.getFileRefs()) {
			if (ObjectUtils.isEmpty(fileRef.getId())) {
				continue;
			}
			DatImpFileResult result = importByFileId(fileRef.getId(), input.getResourceType(), input.isOverwriteExistingData());
			output.addFileResult(result);
		}

		return output;
	}

	private DatImpFileResult importByFileId(String fileId, List<DatImpResourceType> resTypes, boolean overwrite) {
		DatImpFileResult fileResult = new DatImpFileResult();
		fileResult.setFileId(fileId);

		if (ObjectUtils.isEmpty(resTypes)) {
			return fileResult;
		}

		// 1. Get Tmp File
		File file = null;
		try {
			file = BeanUtils.get(FileService.class).getTmp(fileId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (file == null) {
				fileResult.setErrorMessage("File does not exists");
				return fileResult;
			}
		}

		// 2. Process Workbook
		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(file);
			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

			Workbook _wb = wb;
			for (DatImpResourceType resType : resTypes) {
				if (!RESOURCE_TYPE.containsKey(resType)) {
					continue;
				}

				ResOption<?> options = RESOURCE_TYPE.get(resType);
				options.getSheetName().forEach(sheetName -> {
					int index = _wb.getSheetIndex(sheetName);
					if (index < 0) {
						return;
					}

					Sheet sheet = _wb.getSheetAt(index);
					importData(fileResult, sheet, evaluator, options, overwrite);
				});
			}

		} catch (Exception e) {
			AbstractException ae = ValueUtils.toAe(e, null);
			fileResult.setErrorMessage(ae.getCodeMessage());
		} finally {
			ValueUtils.closeQuietly(wb);
		}

		// 3. Delete Tmp File
		BeanUtils.get(FileService.class).deleteTmp(fileId);

		return fileResult;
	}

	private static final long MAX_ERROR_COUNT = 20;

	private <V> DatImpSheetResult importData(DatImpFileResult fileResult, Sheet sheet, FormulaEvaluator evaluator, ResOption<V> options, boolean overwrite) {
		DatImpSheetResult sheetResult = new DatImpSheetResult();
		sheetResult.setSheetName(sheet.getSheetName());

		try {
			Class<V> itemClass = options.getItemClass();

			// 1. Summarize Meta-info
			int headerRowNo;
			int firstDataRowNo;
			int lastDataRowNo;
			{
				SheetSummary summary = getSummary(sheet, evaluator);
				headerRowNo = summary.getHeaderRowNo();
				firstDataRowNo = summary.getFirstDataRowNo();
				lastDataRowNo = summary.getLastDataRowNo();
			}

			// 2. Check Error Exceeded
			if (fileResult.getErrorCount() >= MAX_ERROR_COUNT) {
				sheetResult.setSkippedCount(lastDataRowNo - firstDataRowNo);
				return sheetResult;
			}

			// 3. Extract Field Mapping
			Map<String, String> fieldByXlsFieldMap;
			Map<String, String> xlsFieldByFieldMap;
			{
				Row row = sheet.getRow(headerRowNo);
				fieldByXlsFieldMap = extractFieldMap(itemClass, row, evaluator, options, false);
				if (ObjectUtils.isEmpty(fieldByXlsFieldMap)) {
					return sheetResult;
				}
				xlsFieldByFieldMap = new HashMap<>();
				fieldByXlsFieldMap.forEach((key, value) -> xlsFieldByFieldMap.put(value, key));
			}

			Map<String, String> xlsTitleByFieldNameMap;
			{
				Map<String, String> titleByXlsFieldMap = new HashMap<>();
				for (int r = headerRowNo + 1; r < firstDataRowNo; r++) {
					Row row = sheet.getRow(r);
					Map<String, String> titleByXlsFieldMapOneRow = extractFieldMap(itemClass, row, evaluator, options, true);
					titleByXlsFieldMapOneRow.forEach((key, value) -> {
						if (!StringUtils.isEmpty(value)) {
							titleByXlsFieldMap.put(key, value.replace("*", ""));//ignore special characters
						}
					});
				}

				xlsTitleByFieldNameMap = new HashMap<>();
				titleByXlsFieldMap.forEach((key, value) -> {
					if (fieldByXlsFieldMap.containsKey(key)) {
						xlsTitleByFieldNameMap.put(fieldByXlsFieldMap.get(key), value);
					}
				});
				ThreadUtils.setProp("DatImpService.xlsTitleByFieldNameMap", xlsTitleByFieldNameMap);
			}

			// 4. Process Rows
			sheetResult.setFirstDataRowNo(firstDataRowNo + 1);
			Set<String> xlsFieldNameSet = fieldByXlsFieldMap.keySet();
			Method cfSetter = ReflectionUtils.getSetter(itemClass, "customFieldDTOList");
			for (int i = firstDataRowNo; i < lastDataRowNo + 1; i++) {
				//fix the problem of using the same object for different items
				Map<Long, CustomFieldDTO> cfMap = cfSetter == null ? null : new LinkedHashMap<>();
				// 4.1 Get Row -> XlsData
				final Row row = sheet.getRow(i);
				final XlsData xlsData = toXlsData(row, evaluator, xlsFieldNameSet);
				if (xlsData == null) {
					continue;
				}

				try {
					// 4.2 XlsData -> Item
					V item;
					try {
						item = itemClass.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						AbstractException ae = ValueUtils.toAe(e, null);
						throw ae;
					}

					if (!ObjectUtils.isEmpty(options.getCodeField())) {
						for (String fieldName : options.getCodeField()) {
							if (!xlsFieldByFieldMap.containsKey(fieldName)) {
								continue;
							}
							String xlsFieldName = xlsFieldByFieldMap.get(fieldName);
							String value = ValueUtils.getString(xlsData, xlsFieldName);

							String titleColumn = xlsTitleByFieldNameMap.containsKey(fieldName) ? xlsTitleByFieldNameMap.get(fieldName) : fieldName;
							ValueUtils.assertNotEmpty(value, titleColumn);

							ValueUtils.set(item, fieldName, value);
						}
					}

					if (options.getPopulte() != null) {
						options.getPopulte().execute(item);
					}

					Map<String, Integer> counters = new HashMap<>();
					for (String xlsFieldName : xlsFieldNameSet) {
						String fieldName = fieldByXlsFieldMap.get(xlsFieldName);
						// Child
						if (fieldName.contains(".")) {
							String value = ValueUtils.getString(xlsData, xlsFieldName);
							if (value != null) {
								value = value.trim();
							}

							int index = counters.containsKey(fieldName) ? counters.get(fieldName) : 0;
							counters.put(fieldName, index + 1);
							try {
								setChildValue(item, fieldName, value, index);
							} catch (Exception e) {
								LogUtils.saveErrorQuietly(e);
								String titleColumn = xlsTitleByFieldNameMap.containsKey(fieldName) ? xlsTitleByFieldNameMap.get(fieldName) : fieldName;
								throw DataUtils.newDataValueInvalidException(sheet.getSheetName(), titleColumn, value);
							}
						}
						// Java Field
						else if (!fieldName.startsWith("cf:")) {
							String value = ValueUtils.getString(xlsData, xlsFieldName);
							// If the new value is null but the old value is not null and the type is number, skip it.
							if (value == null) {
								String oldValue = ValueUtils.getString(item, fieldName);
								if (oldValue != null) {
									Method getter = ReflectionUtils.getGetter(item, fieldName);
									if (getter != null) {
										Class<?> type = getter.getReturnType();
										if (Integer.class.equals(type) || int.class.equals(type) //
												|| Long.class.equals(type) || long.class.equals(type) //
												|| Float.class.equals(type) || float.class.equals(type) //
												|| Double.class.equals(type) || double.class.equals(type)) {
											continue;
										}
									}
								}
							}
							try {
								ValueUtils.set(item, fieldName, value);

								Class<?> type = ReflectionUtils.getGetter(item, fieldName) != null ? ReflectionUtils.getGetter(item, fieldName).getReturnType() : null;
								if (saleson.common.util.DataUtils.isInterger(type)
										&& !saleson.common.util.DataUtils.compareExactlyNumberValue(value, ValueUtils.get(item, fieldName)))
									throw new LogicException("VALUE_NOT_EXACTLY", "Format is invalid of " + ReflectionUtils.toClass(item).getName());

							} catch (Exception e) {
								LogUtils.saveErrorQuietly(e);
								String titleColumn = xlsTitleByFieldNameMap.containsKey(fieldName) ? xlsTitleByFieldNameMap.get(fieldName) : fieldName;
								throw DataUtils.newDataValueInvalidException(itemClass, titleColumn, value);
							}
						}
						// Custom Field
						else {
							if (cfSetter == null || cfMap == null) {
								continue;
							}
							Long cfId = ValueUtils.toLong(fieldName.substring(3), null);
							if (cfId == null) {
								continue;
							}
							CustomFieldValueDTO cfValue = new CustomFieldValueDTO();
							String value = ValueUtils.getString(xlsData, xlsFieldName);
							cfValue.setValue(value);
							CustomFieldDTO cf;
							if (cfMap.containsKey(cfId)) {
								cf = cfMap.get(cfId);
							} else {
								cf = new CustomFieldDTO();
								cf.setId(cfId);
								cfMap.put(cfId, cf);
							}
							cf.getCustomFieldValueDTOList().add(cfValue);
						}
					}

					// 4.3 Save Item
					TranUtils.doNewTran(() -> {
						boolean successPost = false;
						boolean successPut = false;
						// Before
						if (options.getBefore() != null) {
							options.getBefore().execute(item);
						}

						// Overwrite Check
						boolean exists = options.getExists() != null && options.getExists().execute(item);
						if (exists && !overwrite) {
							sheetResult.skip();
							return;
						}

						// POST
						if (options.getPost() != null && !exists) {
							options.getPost().execute(item);
//							sheetResult.post();
							successPost = true;
						}
						// PUT
						else if (options.getPut() != null) {
							options.getPut().execute(item);
//							sheetResult.put();
							successPut = true;
						}

						try {
							// save custom field
							Method itemIdGetter = ReflectionUtils.getGetter(itemClass, "id");
							if (cfSetter != null && itemIdGetter != null && itemIdGetter.invoke(item) != null && !cfMap.isEmpty()) {
								BeanUtils.get(CustomFieldValueService.class).editListCustomField((Long) itemIdGetter.invoke(item),
										new CustomFieldListDTO(cfMap.values().stream().collect(Collectors.toList())));
							}
							//add success id
							if (itemIdGetter != null && itemIdGetter.invoke(item) != null) {
								if (successPost)
									sheetResult.addPostId((Long) itemIdGetter.invoke(item));
								else if (successPut)
									sheetResult.addPutId((Long) itemIdGetter.invoke(item));
							}
						} catch (IllegalAccessException | InvocationTargetException e) {
							throw new LogicException("REFLECTION_BUG", e);
						}
						if (successPost) {
							sheetResult.post();
						} else if (successPut) {
							sheetResult.put();
						}
					});
				} catch (Exception e) {
					LogUtils.saveErrorQuietly(e);
					// ERROR
					sheetResult.error(i + 1, null, e);

					if (fileResult.getErrorCount() + sheetResult.getErrorCount() >= MAX_ERROR_COUNT) {
						sheetResult.setSkippedCount(sheetResult.getSkippedCount() + (lastDataRowNo - (i + 1)));
						break;
					}
				} finally {
					sheetResult.setLastRowDataRead(i + 1);
				}
			}

		} finally {
			fileResult.addSheetResult(sheetResult);
		}
		return sheetResult;
	}

	private static void setChildValue(Object item, String fieldName, Object value, Integer index)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
		String[] fieldPath = fieldName.split("\\.");
		String fn1 = fieldPath[0];
		String fn2 = fieldPath[1];
		Method getter = ReflectionUtils.getGetter(item, fn1);
		Class<?> type = getter.getReturnType();
		Object childItem = null;

		if (type.isArray()) {
			Object[] array = (Object[]) getter.invoke(item);
//			Object childItem;
			if (array != null && array.length > index) {
				childItem = array[index];
			} else {
				Object[] newArray = new Object[index + 1];
				if (array != null) {
					for (int j = 0; j < array.length; j++) {
						newArray[j] = array[j];
					}
				}
				Class<?> dataType = type.getComponentType();
				childItem = dataType.getConstructor().newInstance();
				newArray[index] = childItem;
				ValueUtils.set(item, fn1, newArray);
			}
			ValueUtils.set(childItem, fn2, value);
		} else if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) getter.invoke(item);
			if (list == null) {

			}
//			Object childItem;
			if (list != null && list.size() > index) {
				childItem = list.get(index);
			} else {
				if (list == null) {
					list = new ArrayList<>();
				}
				Class<?> dataType = type.getComponentType();
				childItem = dataType.getConstructor().newInstance();
				list.add(childItem);
				ValueUtils.set(item, fn1, list);
			}
			ValueUtils.set(childItem, fn2, value);
		} else {
			setChildValue(item, fieldName, value);
		}

		//add validate
		if (childItem != null) {
			Class<?> typeChild = ReflectionUtils.getGetter(childItem, fn2) != null ? ReflectionUtils.getGetter(childItem, fn2).getReturnType() : null;
			if (saleson.common.util.DataUtils.isInterger(typeChild) && !saleson.common.util.DataUtils.compareExactlyNumberValue(value, ValueUtils.get(childItem, fn2)))
				throw new LogicException("VALUE_NOT_EXACTLY", "Format is invalid of " + ReflectionUtils.toClass(item).getName());
		}

	}

	private static void setChildValue(Object data, String fieldName, Object value) {
		if (!fieldName.contains(".")) {
			ValueUtils.set(data, fieldName, value);
			return;
		}

		String[] fieldPath = fieldName.split("\\.");
		Method setter = ReflectionUtils.getSetter(data, fieldPath[0]);

		if (setter == null) {
			throw new LogicException("METHOD_NOT_FOUND", "Cannot find setter of " + ReflectionUtils.toClass(data).getName());
		}
		Class<?> type = setter.getParameterTypes()[0];
		Object childVal = ValueUtils.get(data, fieldPath[0]);
		try {
			if (childVal == null) {
				childVal = type.getConstructor().newInstance();
				ValueUtils.set(data, fieldPath[0], childVal);

				Class<?> typeCheck = ReflectionUtils.getGetter(data, fieldPath[0]) != null ? ReflectionUtils.getGetter(data, fieldPath[0]).getReturnType() : null;
				if (saleson.common.util.DataUtils.isInterger(typeCheck) && !saleson.common.util.DataUtils.compareExactlyNumberValue(value, ValueUtils.get(data, fieldPath[0])))
					throw new LogicException("VALUE_NOT_EXACTLY", "Format is invalid of " + ReflectionUtils.toClass(data).getName());

			}
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
			AbstractException ae = ValueUtils.toAe(e, null);
			throw ae;
		}
		setChildValue(childVal, fieldName.substring(fieldName.indexOf(".")), value);
	}

	private static SheetSummary getSummary(Sheet sheet, FormulaEvaluator evaluator) {
		int headerRowNo = sheet.getFirstRowNum();
		int lastDataRowNo = sheet.getLastRowNum();
		for (int i = headerRowNo; i < Math.min(30, lastDataRowNo + 1); i++) {
			if (getMergedRegionsColCountInRow(sheet, i) > 0 || !ExcelCommonUtils.existData(sheet.getRow(i), Integer.valueOf(sheet.getRow(i).getLastCellNum()))) {
				headerRowNo++;
			} else {
				break;
			}
		}

		int firstDataRowNo = headerRowNo + 2;
		int mergedHeaderCount = 0;
		for (int i = headerRowNo; i < Math.min(30, lastDataRowNo + 1); i++) {
			final Row row = sheet.getRow(i);
			final XlsData data = toXlsData(row, evaluator, null);
			if (data == null) {
				continue;
			}

			if (i > headerRowNo && getMergedRegionsColCountInRow(sheet, i) > 0) {
				mergedHeaderCount++;
				continue;
			}

			for (int j = 1; j < 10; j++) {
				String str = ValueUtils.getString(data, "data" + j);
				if (str == null) {
					continue;
				}
				str = str.trim();
				if (ObjectUtils.isEmpty(str)) {
					continue;
				}
//				if (!StringUtils.equalsIgnoreCase(str, "TABLE") && !StringUtils.equalsIgnoreCase(str, "RESOURCE")) {
//					break;
//				}
//				headerRowNo = i + 1;
//				firstDataRowNo = headerRowNo + 2;
//				if (lastDataRowNo < firstDataRowNo) {
//					return new SheetSummary();
//				}
				break;
			}
		}

		if (mergedHeaderCount > 0) {
			firstDataRowNo = headerRowNo + 2 + mergedHeaderCount;
		}
		SheetSummary data = new SheetSummary();
		data.setHeaderRowNo(headerRowNo);
		data.setFirstDataRowNo(firstDataRowNo);
		data.setLastDataRowNo(lastDataRowNo);
		return data;
	}

	private static int getMergedRegionsColCountInRow(Sheet sheet, int row) {
		int count = 0;
		for (int i = 0; i < sheet.getNumMergedRegions(); ++i) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			if (range.getFirstRow() <= row && range.getLastRow() >= row && range.getFirstColumn() < range.getLastColumn()) {
				count++;
			}
		}
		return count;
	}

	@Data
	private static class SheetSummary {
		private int headerRowNo;
		private int firstDataRowNo;
		private int lastDataRowNo;
	}

	private static Object getValue(Cell cell, FormulaEvaluator evaluator) {
		if (cell == null) {
			return null;
		}

		CellType cellType = cell.getCellType();

		if (evaluator != null && CellType.FORMULA.equals(cellType)) {
			cellType = evaluator.evaluateFormulaCell(cell);
		}

		Object value = null;
		if (CellType.BLANK.equals(cellType)) {
			return null;
		} else if (CellType.STRING.equals(cellType)) {
			value = cell.getStringCellValue();
		} else if (CellType.NUMERIC.equals(cellType)) {
			String str = cell.toString();
			int i1 = str.indexOf('-');
			int i2 = str.lastIndexOf('-');
			if (i1 != -1 && i1 != i2) {
				Date date = cell.getDateCellValue();
				return date;
			} else {
				value = NumberToTextConverter.toText(cell.getNumericCellValue());
			}
		} else if (CellType.BOOLEAN.equals(cellType)) {
			value = cell.getBooleanCellValue();
		} else if (CellType.ERROR.equals(cellType)) {
			value = cell.getErrorCellValue();
		} else if (CellType.FORMULA.equals(cellType)) {
			value = cell.getCellFormula();
		} else {
			value = cell.getStringCellValue();
		}
		return value;
	}

	/**
	 * Extract Field Mapping between xlsFieldName and fieldName
	 * @param <V>
	 * @param clazz
	 * @param row
	 * @param evaluator
	 * @param options
	 * @return
	 */
	private static <V> Map<String, String> extractFieldMap(Class<?> clazz, Row row, FormulaEvaluator evaluator, ResOption<V> options, boolean isGetTitle) {
		if (row == null) {
			return null;
		}

		Map<String, String> map = new LinkedHashMap<>();
		int firstCellNum = row.getFirstCellNum();
		int lastCellNum = row.getLastCellNum() + 1;
		for (int i = firstCellNum; i < lastCellNum; i++) {
			Cell cell = row.getCell(i);

			// 1. Get Cell Value
			Object value;
			value = getValue(cell, evaluator);
			if (value == null) {
				continue;
			}

			// 2. Map xlsFieldName and fieldName
			if (value instanceof String) {
				// 2.1 fieldName
				String fieldName = (String) value;
				fieldName = fieldName.trim();

				if (!isGetTitle && (fieldName.contains(" ") || fieldName.contains("/n") || (ReflectionUtils.getSetter(clazz, fieldName) == null
						&& (!fieldName.contains(".") || ReflectionUtils.getSetter(clazz, StringUtils.tokenizeToStringArray(fieldName, ".")[0]) == null)))) {
					// Check Custom Field Case
					if (ObjectUtils.isEmpty(options.getObjectType())) {
						continue;
					}
					List<CustomField> list = BeanUtils.get(CustomFieldRepository.class).findByObjectTypeAndFieldNameEquals(options.getObjectType(), fieldName,
							PageRequest.of(0, 1));
					if (ObjectUtils.isEmpty(list)) {
						continue;
					}
					fieldName = "cf:" + list.get(0).getId();
				}

				// 2.2 xlsFieldName
				String xlsFieldName = "data" + (i + 1);
				if (ReflectionUtils.getField(XlsData.class, xlsFieldName) == null) {
					break;
				}

				// 2.3 Mapping
				map.put(xlsFieldName, fieldName);
			}
		}

		return map;
	}

	private static XlsData toXlsData(Row row, FormulaEvaluator evaluator, Collection<String> targetFields) {
		if (row == null || row.getFirstCellNum() < 0) {
			return null;
		}

		XlsData data = new XlsData();
		boolean found = false;
		int firstCellNum = row.getFirstCellNum();
		int lastCellNum = row.getLastCellNum() + 1;
		for (int i = firstCellNum; i < lastCellNum; i++) {
			String fieldName = "data" + (i + 1);
			if (targetFields != null && !targetFields.contains(fieldName)) {
				continue;
			}

			Cell cell = row.getCell(i);
			Object value = getValue(cell, evaluator);
			if (value == null) {
				continue;
			}

			if (value instanceof String) {
				String str = (String) value;
				value = str.trim();
			}
			if (ObjectUtils.isEmpty(value)) {
				continue;
			}
			found = true;

			ValueUtils.set(data, fieldName, value);
			String str = StringUtils.trimWhitespace(ValueUtils.getString(data, fieldName));
			if (str != null) {
				if (str.endsWith(".0") && ValueUtils.isNumber(str)) {
					ValueUtils.set(data, fieldName, str.substring(0, str.lastIndexOf(".0")));
				}
			}
		}

		return found ? data : null;
	}

	@Data
	public static class XlsData {
		private String data1;
		private String data2;
		private String data3;
		private String data4;
		private String data5;
		private String data6;
		private String data7;
		private String data8;
		private String data9;
		private String data10;
		private String data11;
		private String data12;
		private String data13;
		private String data14;
		private String data15;
		private String data16;
		private String data17;
		private String data18;
		private String data19;
		private String data20;
		private String data21;
		private String data22;
		private String data23;
		private String data24;
		private String data25;
		private String data26;
		private String data27;
		private String data28;
		private String data29;
		private String data30;
		private String data31;
		private String data32;
		private String data33;
		private String data34;
		private String data35;
		private String data36;
		private String data37;
		private String data38;
		private String data39;
		private String data40;
		private String data41;
		private String data42;
		private String data43;
		private String data44;
		private String data45;
		private String data46;
		private String data47;
		private String data48;
		private String data49;
		private String data50;
		private String data51;
		private String data52;
		private String data53;
		private String data54;
		private String data55;
		private String data56;
		private String data57;
		private String data58;
		private String data59;
		private String data60;
		private String data61;
		private String data62;
		private String data63;
		private String data64;
		private String data65;
		private String data66;
		private String data67;
		private String data68;
		private String data69;
		private String data70;
		private String data71;
		private String data72;
		private String data73;
		private String data74;
		private String data75;
		private String data76;
		private String data77;
		private String data78;
		private String data79;
		private String data80;
		private String data81;
		private String data82;
		private String data83;
		private String data84;
		private String data85;
		private String data86;
		private String data87;
		private String data88;
		private String data89;
		private String data90;
		private String data91;
		private String data92;
		private String data93;
		private String data94;
		private String data95;
		private String data96;
		private String data97;
		private String data98;
		private String data99;
		private String data100;
	}
}
