package com.emoldino.api.common.resource.composite.datimp.service.deprecated;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatImpServiceDeprecated {

//	@Deprecated
//	private static final String PART_SIZE_FORMAT_REGEX = "^(\\d+\\s*x\\s*){2}\\d*$";
//
//	@Deprecated
//	@Autowired
//	private ModelMapper modelMapper;
//
//	@Deprecated
//	private DatImpSheetResult importPartData(Sheet sheet, boolean overwrite) {
//		DatImpSheetResult result = new DatImpSheetResult();
//		result.setSheetName(sheet.getSheetName());
//
//		final int fieldRowIndex = 0;
//		final int headerRowIndex = 1;
//		final int dataStartRowIndex = 2;
//		final int dataStartColIndex = 0;
//
//		int maxRowValue = sheet.getLastRowNum();
//		if (maxRowValue <= dataStartRowIndex) {
//			return result;
//		}
//		if (!ExcelCommonUtils.existData(sheet.getRow(fieldRowIndex), 6)) {
//			result.addError(new DatImpError(fieldRowIndex, "The define field data row is not exist"));
//			return result;
//		}
//		int maxCol = sheet.getRow(fieldRowIndex).getLastCellNum();
//		Map<Integer, String> fieldMap = ExcelCommonUtils.getRowValueAsMap(sheet.getRow(fieldRowIndex));
//		Map<Integer, String> headerMap = ExcelCommonUtils.getRowValueAsMap(sheet.getRow(headerRowIndex));
//		headerMap.keySet().stream().forEach(k -> {
//			if (StringUtils.isNotBlank(headerMap.get(k)))
//				headerMap.put(k, headerMap.get(k).replace("*", ""));
//		});
//		List<Pair<Integer, Map<Integer, String>>> sheetValue = ExcelCommonUtils.readSheetValue(sheet, dataStartRowIndex, dataStartColIndex, maxCol, maxRowValue);
//
//		List<DatImpRawDatPart> rawDatParts = new ArrayList<>();
//		//load raw value
//		sheetValue.stream().forEach(rowVal -> {
//			Map<Integer, String> rowValMap = rowVal.getSecond();
//			DatImpRawDatPart item = new DatImpRawDatPart(rowVal.getFirst());
//			Map<String, String> customFields = new HashMap<>();
//			item.setCustomFields(customFields);
//			rawDatParts.add(item);
//			for (int i = dataStartColIndex; i < maxCol; i++) {
//				if (!fieldMap.containsKey(i)) {
//					continue;
//				}
//				String fieldName = fieldMap.get(i);
//				if (ObjectUtils.isEmpty(fieldName)) {
//					continue;
//				}
//
//				Method setter = ReflectionUtils.getSetter(DatImpRawDatPart.class, fieldName);
//				Method getter = ReflectionUtils.getGetter(DatImpRawDatPart.class, fieldName);
//				try {
//					if (setter != null && getter != null && getter.invoke(item) == null) {
//						try {
//							setter.invoke(item, rowValMap.get(i));
//						} catch (IllegalAccessException | InvocationTargetException e) {
//							throw new RuntimeException(e);
//						}
//					} else {// custom field
//						customFields.put(fieldMap.get(i), rowValMap.get(i));
//					}
//				} catch (IllegalAccessException | InvocationTargetException e) {
//					throw new RuntimeException(e);
//				}
//			}
//
//		});
//
//		//convert and valid
//		List<Category> productList = BeanUtils.get(CategoryService.class).getAllProductActive();
//		//load column config
//		Pair<List<HeaderDTO>, List<String>> headerConfig = BeanUtils.get(ExcelUtils.class).getPartHearAndDeletedFields(true);
//		List<HeaderDTO> basicPartHeaders = headerConfig.getFirst();
//		List<String> deleteProperties = headerConfig.getSecond();
//
//		List<PartPayload> payloadList = new ArrayList<>();
//		int newRecord = 0;
//		int updateRecord = 0;
//		for (int i = 0; i < rawDatParts.size(); i++) {
//			DatImpRawDatPart rawDatPart = rawDatParts.get(i);
//			PartPayload partPayload = modelMapper.map(rawDatPart, PartPayload.class);
//			List<DatImpError> cellErrors = new ArrayList<>();
//			//check edit able
//			if (StringUtils.isBlank(partPayload.getPartCode())) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.partCode, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isNotBlank(partPayload.getPartCode()) && partPayload.getPartCode().length() > 200) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.partCode, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " Invalid length"));
//			}
//			//check duplicate
//			Part part = BeanUtils.get(PartService.class).findByPartCode(partPayload.getPartCode());
//			if (part != null) {
//				if (overwrite) {
//					partPayload.setId(part.getId());
//				} else {
//					Integer col = getColIndexByFieldName(Const.ColumnCode.part.partCode, fieldMap);
//					cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " was duplicated"));
//					result.addErrors(cellErrors);
//					continue;
//				}
//			}
//			if (StringUtils.isNotBlank(rawDatPart.getProductName())) {
//				Category product = productList.stream().filter(p -> p.getName() != null && p.getName().trim().equalsIgnoreCase(rawDatPart.getProductName())).findFirst()
//						.orElse(null);
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.IMPORT.productName, fieldMap);
//				if (product == null) {
//					cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " not exists"));
//				} else {
//					partPayload.setCategoryId(product.getId());
//				}
//			}
//			//config check product by "category" text
//			if (partPayload.getCategoryId() == null && isRequiredField(Const.ColumnCode.part.category, basicPartHeaders)) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.IMPORT.productName, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isBlank(partPayload.getName())) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.name, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isNotBlank(partPayload.getName()) && partPayload.getName().length() > 200) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.name, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " Invalid length"));
//			}
//
//			if (StringUtils.isBlank(partPayload.getResinCode()) && isRequiredField(Const.ColumnCode.part.resinCode, basicPartHeaders)) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.resinCode, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isBlank(partPayload.getResinGrade()) && isRequiredField(Const.ColumnCode.part.resinGrade, basicPartHeaders)) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.resinGrade, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isNotBlank(partPayload.getResinGrade()) && partPayload.getResinGrade().length() > 200) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.resinGrade, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " Invalid length"));
//			}
//
//			if (isRequiredField(Const.ColumnCode.part.size, basicPartHeaders)) {
//				if (StringUtils.isBlank(partPayload.getSize())) {
//					Integer col = getColIndexByFieldName(Const.ColumnCode.part.size, fieldMap);
//					cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//				}
//			}
//
//			if (StringUtils.isNotBlank(partPayload.getSize()) && !partPayload.getSize().trim().toLowerCase().matches(PART_SIZE_FORMAT_REGEX)) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.IMPORT.sizeUnit, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, " Invalid format, requested format W x D x H"));
//			}
//			if (partPayload.getSizeUnit() == null && StringUtils.isNotBlank(partPayload.getSize())) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.IMPORT.sizeUnit, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//
//			if (isRequiredField(Const.ColumnCode.part.weight, basicPartHeaders)) {
//				if (StringUtils.isBlank(partPayload.getWeight())) {
//					Integer col = getColIndexByFieldName(Const.ColumnCode.part.weight, fieldMap);
//					cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//				}
//			}
//			if (StringUtils.isNotBlank(partPayload.getWeight()) && !partPayload.getWeight().trim().toLowerCase().matches("^\\d*$")) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.weight, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, "Invalid format, Weight must be numberic"));
//			}
//			if (partPayload.getWeightUnit() == null && StringUtils.isNotBlank(partPayload.getWeight())) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.IMPORT.weightUnit, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isBlank(rawDatPart.getWeeklyDemand()) && isRequiredField(Const.ColumnCode.part.weeklyDemand, basicPartHeaders)) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.weeklyDemand, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isNotBlank(rawDatPart.getWeeklyDemand()) && !rawDatPart.getWeeklyDemand().trim().toLowerCase().matches("^\\d*$")) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.weeklyDemand, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, "Invalid format, Weekly Demand must be numberic"));
//			}
//			if (StringUtils.isBlank(rawDatPart.getQuantityRequired()) && isRequiredField(Const.ColumnCode.part.quantityRequired, basicPartHeaders)) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.quantityRequired, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, headerMap.get(col) + " required field"));
//			}
//			if (StringUtils.isNotBlank(rawDatPart.getQuantityRequired()) && !rawDatPart.getQuantityRequired().trim().toLowerCase().matches("^\\d*$")) {
//				Integer col = getColIndexByFieldName(Const.ColumnCode.part.quantityRequired, fieldMap);
//				cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), col, "Invalid format, Weekly Demand must be numberic"));
//			}
//			//custom field
//			if (rawDatPart.getCustomFields() != null) {
//				List<CustomFieldDTO> customFieldDTOList = new ArrayList<>();
//				rawDatPart.getCustomFields().keySet().stream().forEach(field -> {
//					HeaderDTO customField = basicPartHeaders.stream().filter(headerDTO -> headerDTO.isCustomField() && headerDTO.getName().equalsIgnoreCase(field)).findFirst()
//							.orElse(null);
//					String val = rawDatPart.getCustomFields().get(field);
//					if (customField != null && customField.getCustomFieldId() != null) {
//						if (customField.isRequired() && StringUtils.isBlank(val)) {
//							cellErrors.add(new DatImpError(rawDatPart.getRowIndex(), getColIndexByFieldName(field, fieldMap), field + " is required field"));
//						}
//						CustomFieldDTO customFieldDTO = new CustomFieldDTO();
//						customFieldDTO.setId(customField.getCustomFieldId());
//						CustomFieldValueDTO valueDTO = new CustomFieldValueDTO(val);
//						customFieldDTO.setCustomFieldValueDTOList(new ArrayList<>(Arrays.asList(valueDTO)));
//						customFieldDTOList.add(customFieldDTO);
//					}
//				});
//				partPayload.setCustomFieldDTOList(customFieldDTOList);
//			}
//
//			if (cellErrors.isEmpty()) {
//				try {
//					BeanUtils.get(PartService.class).savePartByImport(partPayload);
//					if (partPayload.getId() != null)
//						updateRecord++;
//					payloadList.add(partPayload);
//				} catch (Exception e) {
//					log.error(e.getMessage());
//					e.printStackTrace();
//					result.addError(new DatImpError(rawDatPart.getRowIndex(), null, e.getMessage()));
//				}
//			} else
//				result.addErrors(cellErrors);
//		}
//		newRecord = payloadList.size() - updateRecord;
//		result.setPostCount(newRecord);
//		result.setPutCount(updateRecord);
//		return result;
//	}
//
//	@Deprecated
//	private static boolean isRequiredField(String field, List<HeaderDTO> headers) {
//		return headers.stream().filter(h -> h.getCode().equalsIgnoreCase(field) && h.isRequired()).map(h -> h.isRequired()).findFirst().orElse(false);
//	}
//
//	@Deprecated
//	private Integer getColIndexByFieldName(String fieldName, Map<Integer, String> fieldMap) {
//		return getColIndexByFieldName(fieldName, fieldMap, false);
//	}
//
//	@Deprecated
//	private Integer getColIndexByFieldName(String fieldName, Map<Integer, String> fieldMap, boolean isCustomField) {
//		Integer col = null;
//		if (StringUtils.isNotBlank(fieldName)) {
//			for (int i = 0; i < fieldMap.keySet().stream().mapToInt(v -> v).max().orElse(0); i++) {
//				if (fieldName.equalsIgnoreCase(fieldMap.get(i))) {
//					col = i;
//					if (!isCustomField)
//						return col;// return fixed field at the first, custom field at the last
//				}
//			}
//		}
//		return col;
//	}

}
