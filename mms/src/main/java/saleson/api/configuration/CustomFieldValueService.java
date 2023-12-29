package saleson.api.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.object.repository.customfield.CustomFieldRepository;
import com.emoldino.api.common.resource.base.object.repository.customfieldvalue.CustomFieldValueRepository;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.DataUtils;
import saleson.common.util.StringUtils;
import saleson.dto.CustomField.CustomFieldDTO;
import saleson.dto.CustomField.CustomFieldListDTO;
import saleson.dto.CustomField.CustomFieldValueDTO;
import saleson.model.customField.CustomField;
import saleson.model.customField.CustomFieldValue;

@Service
public class CustomFieldValueService {
	@Autowired
	private CustomFieldRepository customFieldRepository;
	@Autowired
	private CustomFieldValueRepository repo;

	public List<CustomFieldDTO> get(Long objectId, ObjectType objectType) {
		List<CustomFieldDTO> list = new ArrayList<>();
		customFieldRepository.findAll(new BooleanBuilder()//
				.and(Qs.customField.objectType.eq(objectType))//
				.and(Qs.customField.defaultField.eq(false))//
				.and(Qs.customField.deleted.eq(false))//
				.and(Qs.customField.hidden.eq(false)), //
				Sort.by("propertyGroup", "position", "createdAt"))//
				.forEach(customField -> {
					CustomFieldDTO field = ValueUtils.map(customField, CustomFieldDTO.class);
					List<CustomFieldValueDTO> values = new ArrayList<>();
					BeanUtils.get(CustomFieldValueRepository.class)//
							.findAll(Qs.customFieldValue.customField.eq(customField).and(Qs.customFieldValue.objectId.eq(objectId)), Sort.by("id"))//
							.forEach(customFieldValue -> {
								CustomFieldValueDTO customFieldValueDTO = ValueUtils.map(customFieldValue, CustomFieldValueDTO.class);
								values.add(customFieldValueDTO);
							});
					if (values.isEmpty()) {
						CustomFieldValueDTO value = new CustomFieldValueDTO();
						value.setObjectId(objectId);
						values.add(value);
					}
					field.setCustomFieldValueDTOList(values);
					list.add(field);
				});
		return list;
	}

	public CustomFieldListDTO editListCustomField(Long objectId, CustomFieldListDTO dto) {
		if (dto.getCustomFieldDTOList() == null || dto.getCustomFieldDTOList().isEmpty()) {
			return dto;
		}

		validate(dto);

		CustomFieldListDTO result = new CustomFieldListDTO();
		for (CustomFieldDTO customFieldDTO : dto.getCustomFieldDTOList()) {
			CustomFieldDTO customFieldDTO1 = addOrEdit(objectId, customFieldDTO);
			if (customFieldDTO1 == null) {
				continue;
			}
			result.getCustomFieldDTOList().add(customFieldDTO1);
		}

		return result;
	}

	private void validate(CustomFieldListDTO input) {
		CustomField customField = input.getCustomFieldDTOList().stream().map(customFieldDTO -> customFieldDTO.getId()).filter(id -> customFieldRepository.existsById(id))
				.findFirst().map(id -> customFieldRepository.findById(id)).orElse(Optional.empty()).orElse(null);
		if (customField != null) {
			ConfigCategory configCategory = null;
			if (ObjectType.PART.equals(customField.getObjectType())) {
				configCategory = ConfigCategory.PART;
			} else if (ObjectType.TOOLING.equals(customField.getObjectType())) {
				configCategory = ConfigCategory.TOOLING;
			}
			if (configCategory != null && OptionUtils.isEnabled(configCategory)) {
				input.getCustomFieldDTOList().stream().forEach(customFieldDTO -> {
					CustomField cf = customFieldRepository.findById(customFieldDTO.getId()).orElse(null);
					if (cf != null && cf.getRequired()) {
						String value = !CollectionUtils.isEmpty(customFieldDTO.getCustomFieldValueDTOList()) ? customFieldDTO.getCustomFieldValueDTOList().get(0).getValue() : null;
						ValueUtils.assertNotEmpty(value, cf.getFieldName());
					}
				});
			}
		}
	}

	public CustomFieldDTO addOrEdit(Long objectId, CustomFieldDTO customFieldDTO) {
		CustomField customField = customFieldRepository.findById(customFieldDTO.getId()).orElse(null);
		if (customField == null) {
			return null;
		}
		List<CustomFieldValueDTO> fieldValue = customFieldDTO.getCustomFieldValueDTOList();
		List<CustomFieldValue> customFieldValueList = new ArrayList<>();
		List<CustomFieldValue> currentCustomFieldValueList = repo.findByCustomFieldAndObjectId(customField, objectId);

		repo.deleteInBatch(currentCustomFieldValueList);

		if (fieldValue != null && fieldValue.size() > 0) {
			CustomFieldValueDTO customFieldValueDTO = fieldValue.get(0);
			CustomFieldValue customFieldValue = null;
			if (customFieldValueDTO.getId() != null) {
				customFieldValue = repo.findById(customFieldValueDTO.getId()).orElse(null);
			}
			if (customFieldValue == null) {
				customFieldValue = new CustomFieldValue();
			}
			customFieldValue.setCustomField(customField);
			customFieldValue.setObjectId(objectId);
			customFieldValue.setValue(StringUtils.trimWhitespace(customFieldValueDTO.getValue()));
			customFieldValueList.add(customFieldValue);

			fieldValue.clear();
			customFieldValueDTO.setId(customFieldValue.getId());
			fieldValue.add(customFieldValueDTO);
		}
		repo.saveAll(customFieldValueList);
		fieldValue = customFieldValueList.stream().map(cf -> DataUtils.mapper.map(cf, CustomFieldValueDTO.class)).collect(Collectors.toList());
		customFieldDTO.setCustomFieldValueDTOList(fieldValue);
		return customFieldDTO;
	}

	public void deleteCustomFieldValueByObjectId(Long objectId) {
		if (objectId != null) {
			repo.deleteAllByObjectId(objectId);
		}
	}

	public List<CustomFieldValue> getByObjectTypeAndObjectIdList(ObjectType objectType, List<Long> objectIds) {
		List<CustomField> customFieldList = customFieldRepository.findByObjectTypeOrderByFieldNameAsc(objectType);
		List<CustomFieldValue> customFieldValueList = repo.findByCustomFieldInAndObjectIdIn(customFieldList, objectIds);
		return customFieldValueList;
	}

	public Map<Long, Map<Long, List<CustomFieldValue>>> mapValueCustomField(ObjectType objectType, List<Long> objectIds) {
		List<CustomFieldValue> customFieldValueList = getByObjectTypeAndObjectIdList(objectType, objectIds);
		Map<Long, Map<Long, List<CustomFieldValue>>> mapResult = new HashMap<>();
		customFieldValueList.forEach(c -> {
			Map<Long, List<CustomFieldValue>> customFieldOneObject = new HashMap<>();
			if (mapResult.containsKey(c.getObjectId()))
				customFieldOneObject = mapResult.get(c.getObjectId());
			else {
				mapResult.put(c.getObjectId(), customFieldOneObject);
			}

			List<CustomFieldValue> lstVal = new ArrayList<>();
			if (customFieldOneObject.containsKey(c.getCustomField().getId())) {
				lstVal = customFieldOneObject.get(c.getCustomField().getId());
			} else {
				customFieldOneObject.put(c.getCustomField().getId(), lstVal);
			}
			lstVal.add(c);
		});
		return mapResult;
	}
}
