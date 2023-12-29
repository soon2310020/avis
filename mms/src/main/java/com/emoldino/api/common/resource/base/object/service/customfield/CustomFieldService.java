package com.emoldino.api.common.resource.base.object.service.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.object.dto.CustomFieldGetIn;
import com.emoldino.api.common.resource.base.object.dto.CustomFieldItemIn;
import com.emoldino.api.common.resource.base.object.dto.CustomFieldItemOut;
import com.emoldino.api.common.resource.base.object.repository.customfield.CustomFieldRepository;
import com.emoldino.api.common.resource.base.object.repository.customfieldvalue.CustomFieldValueRepository;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.machine.MachineRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.api.resource.ResourceHandler;
import saleson.common.config.Const;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.customField.PropertyGroup;
import saleson.common.service.ContextWrapper;
import saleson.model.Machine;
import saleson.model.Mold;
import saleson.model.Part;
import saleson.model.customField.CustomField;
import saleson.model.customField.CustomFieldValue;

@Service
public class CustomFieldService {
	@Autowired
	private CustomFieldRepository repo;

	@Transactional
	public List<CustomFieldItemOut> get(CustomFieldGetIn input) {
		List<CustomFieldItemOut> output = new ArrayList<>();
		PropertyGroup prevGroup = null;
		int position = 0;
		for (CustomField customField : findAll(input)) {
			if (prevGroup != customField.getPropertyGroup()) {
				position = 0;
			}
			if (++position != ValueUtils.toInteger(customField.getPosition(), 0)) {
				customField.setPosition(position);
				repo.save(customField);
			}
			output.add(ValueUtils.map(customField, CustomFieldItemOut.class));
		}
		return output;
	}

	private List<CustomField> findAll(CustomFieldGetIn input) {
		LogicUtils.assertNotNull(input.getObjectType(), "objectType");
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Qs.customField.objectType.eq(input.getObjectType()))//
				.and(Qs.customField.hidden.eq(false))//
				.and(Qs.customField.deleted.eq(false))//
				.and(Qs.customField.defaultField.eq(false));
		QueryUtils.eq(filter, Qs.customField.propertyGroup, input.getPropertyGroup());
		QueryUtils.eq(filter, Qs.customField.fieldName, input.getFieldName());
		List<CustomField> output = new ArrayList<>();
		int position = 0;
		for (CustomField customField : repo.findAll(filter, Sort.by("propertyGroup", "position", "createdAt"))) {
			if (input.getPropertyGroup() == null && ++position != ValueUtils.toInteger(customField.getPosition(), 0)) {
				customField.setPosition(position);
				repo.save(customField);
			}
			output.add(customField);
		}
		return output;
	}

	@Transactional
	public CustomFieldItemOut post(CustomFieldItemIn payload) {
		validateCustomField(null, payload);

		List<CustomField> list = findAll(CustomFieldGetIn.builder().objectType(payload.getObjectType()).propertyGroup(payload.getPropertyGroup()).build());
		CustomField data = findByName(payload.getObjectType(), payload.getFieldName());
		if (data != null) {
			data.setDeleted(false);
		} else {
			data = ValueUtils.map(payload, CustomField.class);
		}

		if (payload.getPosition() == null || payload.getPosition() <= 0) {
			if (list.isEmpty()) {
				data.setPosition(1);
			} else {
				data.setPosition(list.get(list.size() - 1).getPosition() + 1);
			}
		} else {
			int position = data.getPosition();
			for (CustomField customField : list) {
				if (customField.getPosition() >= data.getPosition() && ++position != customField.getPosition()) {
					customField.setPosition(position);
					repo.save(customField);
				}
			}
		}
		repo.save(data);
		return ValueUtils.map(data, CustomFieldItemOut.class);
	}

	@Transactional
	public CustomFieldItemOut put(Long id, CustomFieldItemIn payload) {
		LogicUtils.assertNotNull(id, "id");
		validateCustomField(id, payload);
		CustomField data = findById(id);
		payload.setPosition(data.getPosition());
		ValueUtils.map(payload, data);
		repo.save(data);
		return ValueUtils.map(data, CustomFieldItemOut.class);
	}

	@Deprecated
	private CustomField createDefaultValue(CustomField customField) {
		//create default data custom field value for all objects
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.initialize();
		executor.execute(() -> {
			switch (customField.getObjectType()) {
			case TOOLING:
				List<Mold> molds = BeanUtils.get(MoldRepository.class).findAll();
				List<CustomFieldValue> customFieldValuesForMold = new ArrayList<>();
				molds.forEach(mold -> {
					CustomFieldValue cfv = new CustomFieldValue();
					cfv.setCustomField(customField);
					cfv.setObjectId(mold.getId());
					cfv.setValue(null);
					customFieldValuesForMold.add(cfv);
				});
				BeanUtils.get(CustomFieldValueRepository.class).saveAll(customFieldValuesForMold);
			case PART:
				List<Part> parts = BeanUtils.get(PartRepository.class).findAll();
				List<CustomFieldValue> customFieldValuesForPart = new ArrayList<>();
				parts.forEach(part -> {
					CustomFieldValue cfv = new CustomFieldValue();
					cfv.setCustomField(customField);
					cfv.setObjectId(part.getId());
					cfv.setValue(null);
					customFieldValuesForPart.add(cfv);
				});
				BeanUtils.get(CustomFieldValueRepository.class).saveAll(customFieldValuesForPart);
			case MACHINE:
				List<Machine> machines = BeanUtils.get(MachineRepository.class).findAll();
				List<CustomFieldValue> customFieldValuesForMachines = new ArrayList<>();
				machines.forEach(part -> {
					CustomFieldValue cfv = new CustomFieldValue();
					cfv.setCustomField(customField);
					cfv.setObjectId(part.getId());
					cfv.setValue(null);
					customFieldValuesForMachines.add(cfv);
				});
				BeanUtils.get(CustomFieldValueRepository.class).saveAll(customFieldValuesForMachines);
			default:
			}
		});
		executor.shutdown();

		return customField;
	}

	@Transactional
	public void delete(Long id) {
		CustomField customField = findById(id);
		if (!BeanUtils.get(CustomFieldValueRepository.class).exists(Qs.customFieldValue.customField.id.eq(id))) {
			repo.delete(customField);
		} else {
			customField.setDeleted(true);
			repo.save(customField);
		}
	}

	private CustomField findById(Long id) {
		return repo.findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(CustomField.class, "id", id));
	}

	private CustomField findByName(ObjectType objectType, String fieldName) {
		LogicUtils.assertNotNull(objectType, "objectType");
		LogicUtils.assertNotEmpty(fieldName, "fieldName");
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Qs.customField.objectType.eq(objectType))//
				.and(Qs.customField.fieldName.eq(fieldName));
		return repo.findOne(filter).orElse(null);
	}

	private void validateCustomField(Long id, CustomFieldItemIn payload) {
		LogicUtils.assertNotNull(payload, "payload");
		LogicUtils.assertNotNull(payload.getObjectType(), "objectType");
		LogicUtils.assertNotEmpty(payload.getFieldName(), "fieldName");
		if (ObjectType.TOOLING.equals(payload.getObjectType())) {
			LogicUtils.assertNotNull(payload.getPropertyGroup(), "propertyGroup");
		}
		if (existsOriginField(payload.getFieldName().trim(), payload.getObjectType())) {
			throw new BizException("REGISTERED_FIELD_NAME", "Property Name is already registered in the system.");
		}

		BooleanBuilder fileter = new BooleanBuilder()//
				.and(Qs.customField.objectType.eq(payload.getObjectType()))//
				.and(Qs.customField.fieldName.eq(payload.getFieldName()))//
				.and(Qs.customField.deleted.ne(true));
		QueryUtils.ne(fileter, Qs.customField.id, id);
		if (repo.exists(fileter)) {
			throw new BizException("REGISTERED_FIELD_NAME", "Property Name is already registered in the system.");
		}
	}

	private boolean existsOriginField(String fieldName, ObjectType objectType) {
		ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);
		String currentLang = resourceHandler.getCurrentLang();
		List<String> toolingListName = genToolingListName(currentLang);
		List<String> partListName = genPartListName(currentLang);
		List<String> machineListName = genMachineListName(currentLang);
		if (!Const.LANGUAGE.en.equalsIgnoreCase(currentLang)) {
			toolingListName.addAll(genToolingListName(Const.LANGUAGE.en));
			partListName.addAll(genPartListName(Const.LANGUAGE.en));
			machineListName.addAll(genMachineListName(Const.LANGUAGE.en));
		}
		if (ObjectType.TOOLING.equals(objectType)) {
			return toolingListName.stream().anyMatch(field -> field.equalsIgnoreCase(fieldName));
		}
		if (ObjectType.PART.equals(objectType)) {
			return partListName.stream().anyMatch(field -> field.equalsIgnoreCase(fieldName));
		}
		if (ObjectType.MACHINE.equals(objectType)) {
			return machineListName.stream().anyMatch(field -> field.equalsIgnoreCase(fieldName));
		}

		return false;
	}

	private List<String> genToolingListName(String lang) {
		ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);
		List<String> toolingListName = Arrays.asList(//
				resourceHandler.getMessageByKey("tooling_id", lang), //
				resourceHandler.getMessageByKey("part", lang), //
				resourceHandler.getMessageByKey("company", lang), //
				resourceHandler.getMessageByKey("last_shot", lang), //
				resourceHandler.getMessageByKey("cycle_time", lang), //
				resourceHandler.getMessageByKey("op", lang), //
				resourceHandler.getMessageByKey("status", lang), //
				resourceHandler.getMessageByKey("weight_of_runner_system_o", lang), //
				resourceHandler.getMessageByKey("approved_cycle_time", lang), //
				resourceHandler.getMessageByKey("counter_id", lang), //
				resourceHandler.getMessageByKey("reset.forecastedMaxShots", lang), //
				resourceHandler.getMessageByKey("front_injection", lang), //
				resourceHandler.getMessageByKey("last_date_of_shots", lang), //
				resourceHandler.getMessageByKey("tooling_letter", lang), //
				resourceHandler.getMessageByKey("tooling_type", lang), //
				resourceHandler.getMessageByKey("tooling_complexity", lang), //
				resourceHandler.getMessageByKey("forecasted_max_shots", lang), //
				resourceHandler.getMessageByKey("forecasted_tool_life", lang), //
				resourceHandler.getMessageByKey("year_of_tool_made", lang), //
				resourceHandler.getMessageByKey("location", lang), //
				resourceHandler.getMessageByKey("engineer_in_charge", lang), //
				resourceHandler.getMessageByKey("tool_description", lang),

				resourceHandler.getMessageByKey("tool_size", lang), //
				resourceHandler.getMessageByKey("tool_weight", lang), //
				resourceHandler.getMessageByKey("shot_weight", lang) + "(gram)", //
				resourceHandler.getMessageByKey("toolmaker", lang), //
				resourceHandler.getMessageByKey("injection_molding_machine_id", lang), //
				resourceHandler.getMessageByKey("machine_tonnage", lang), //
				resourceHandler.getMessageByKey("machine_ton", lang),

				resourceHandler.getMessageByKey("type_of_runner_system", lang), //
				resourceHandler.getMessageByKey("maker_of_runner_system", lang), //
				resourceHandler.getMessageByKey("weight_of_runner_system_g", lang), //
				resourceHandler.getMessageByKey("hot_runner_number_of_drop", lang), //
				resourceHandler.getMessageByKey("hot_runner_zone", lang), //
				resourceHandler.getMessageByKey("cost_tooling", lang), //
				resourceHandler.getMessageByKey("memo", lang),

				resourceHandler.getMessageByKey("supplier", lang), //
				resourceHandler.getMessageByKey("required_labor", lang), //
				resourceHandler.getMessageByKey("production_hour_per_day", lang), //
				resourceHandler.getMessageByKey("production_day_per_week", lang), //
				resourceHandler.getMessageByKey("maximum_capacity_per_week", lang),

				resourceHandler.getMessageByKey("approved_cycle_time_form_second", lang), //
				resourceHandler.getMessageByKey("cycle_time_tolerance_l1", lang), //
				resourceHandler.getMessageByKey("cycle_time_tolerance_l2", lang), //
				resourceHandler.getMessageByKey("uptime_target", lang) + " (%)", //
				resourceHandler.getMessageByKey("uptime_tolerance_l1", lang) + " (%)", //
				resourceHandler.getMessageByKey("uptime_tolerance_l2", lang) + " (%)",

				resourceHandler.getMessageByKey("maintenance_interval", lang), //
				resourceHandler.getMessageByKey("upcoming_maintenance_tolerance", lang), //
				resourceHandler.getMessageByKey("overdue_maintenance_tolerance", lang), //
				resourceHandler.getMessageByKey("maintenance_document", lang), //
				resourceHandler.getMessageByKey("instruction_video", lang),

				resourceHandler.getMessageByKey("part_id", lang), //
				resourceHandler.getMessageByKey("front_tool_size", lang), //
				resourceHandler.getMessageByKey("utilization_rate", lang), //
				resourceHandler.getMessageByKey("inactive_period", lang), //
				resourceHandler.getMessageByKey("no_of_cavity", lang)//
		);
		return toolingListName;
	}

	public List<String> genPartListName(String lang) {
		ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);
		List<String> partListName = Arrays.asList(//
				resourceHandler.getMessageByKey("category", lang), //
				resourceHandler.getMessageByKey("part_id", lang), //
				resourceHandler.getMessageByKey("part_name", lang), //
				resourceHandler.getMessageByKey("part_resin_code", lang), //
				resourceHandler.getMessageByKey("part_resin_grade", lang), //
				resourceHandler.getMessageByKey("design_revision_level", lang), //
				resourceHandler.getMessageByKey("part_volume", lang) + " (W x D x H)", //
				resourceHandler.getMessageByKey("part_weight", lang), //
				resourceHandler.getMessageByKey("weekly_demand", lang)//
		);
		return partListName;
	}

	public List<String> genMachineListName(String lang) {
		ResourceHandler resourceHandler = ContextWrapper.getContext().getBean("resourceHandler", ResourceHandler.class);
		List<String> listName = Arrays.asList(resourceHandler.getMessageByKey("machine", lang), //
				resourceHandler.getMessageByKey("machine_id", lang), //
				resourceHandler.getMessageByKey("line", lang), //
				resourceHandler.getMessageByKey("machine_maker", lang), //
				resourceHandler.getMessageByKey("machine_type", lang), //
				resourceHandler.getMessageByKey("machine_model", lang), //
				resourceHandler.getMessageByKey("company", lang), //
				resourceHandler.getMessageByKey("locations", lang), //
				resourceHandler.getMessageByKey("status", lang), //
				resourceHandler.getMessageByKey("action", lang));
		return listName;
	}
}
