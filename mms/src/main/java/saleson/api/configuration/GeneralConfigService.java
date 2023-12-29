package saleson.api.configuration;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.dto.ChartTypeConfigItem;
import com.emoldino.api.common.resource.base.option.dto.GraphSettingsConfigItem;
import com.emoldino.api.common.resource.base.option.dto.OPConfigItem;
import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.OptionFieldValueRepository;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.QOptionFieldValue;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.dspstp.service.DspStpService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import saleson.api.configuration.payload.AllConfigPayload;
import saleson.api.configuration.payload.EnableConfigPayload;
import saleson.api.configuration.payload.GeneralConfigPayload;
import saleson.api.configuration.payload.OPConfigPayload;
import saleson.api.configuration.payload.RefurbishmentConfigPayload;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.mold.MoldRepository;
import saleson.common.config.Const;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.ChartDataConfigType;
import saleson.common.enumeration.ChartType;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.payload.ApiResponse;
import saleson.common.util.StringUtils;
import saleson.model.config.GeneralConfig;

@Service
public class GeneralConfigService {
	@Autowired
	private GeneralConfigRepository generalConfigRepository;

	@Autowired
	private DataCompletionRateService dataCompletionRateService;

	public List<GeneralConfig> findAll(Predicate predicate) {
		return StreamSupport.stream(generalConfigRepository.findAll(predicate).spliterator(), false).collect(Collectors.toList());
	}

	private GeneralConfig setConfig(GeneralConfigPayload payload) {
		GeneralConfig generalConfig = new GeneralConfig();
		if (payload == null) {
			return null;
		} else if (payload.getId() != null) {
			generalConfig = generalConfigRepository.findById(payload.getId()).orElse(new GeneralConfig());
		} else if (!StringUtils.isEmpty(payload.getFieldName())) {
			generalConfig = generalConfigRepository.findByConfigCategoryAndFieldName(payload.getConfigCategory(), payload.getFieldName()).orElse(new GeneralConfig());
		}
		return payload.getModel(generalConfig);
//		return generalConfigRepository.save(payload.getModel(generalConfig));
	}

	public List<OPConfigItem> findAllOpConfig(OPConfigPayload payload) {
		Map<EquipmentType, Map<OperatingStatus, OPConfigItem>> configs = OptionUtils.getContent("OP_CONFIG",
				new TypeReference<Map<EquipmentType, Map<OperatingStatus, OPConfigItem>>>() {
				});
		List<OPConfigItem> list = new ArrayList<>();
		configs.forEach((equipType, config) -> addItems(list, equipType, config, payload));
		return list;
	}

	private void addItems(List<OPConfigItem> list, EquipmentType equipType, Map<OperatingStatus, OPConfigItem> config, OPConfigPayload payload) {
		if (equipType == null //
				|| ObjectUtils.isEmpty(config) //
				|| (payload.getEquipmentType() != null && !equipType.equals(payload.getEquipmentType()))//
		) {
			return;
		}

		if (payload.getOperatingStatus() != null) {
			OPConfigItem item = config.get(payload.getOperatingStatus());
			addItem(list, item, equipType, payload.getOperatingStatus());
		} else {
			config.forEach((operStatus, item) -> addItem(list, item, equipType, operStatus));
		}
	}

	private void addItem(List<OPConfigItem> list, OPConfigItem item, EquipmentType equipType, OperatingStatus operStatus) {
		if (list == null || item == null || OperatingStatus.NOT_WORKING.equals(operStatus)) {
			return;
		}

		item.setEquipmentType(equipType);
		item.setOperatingStatus(operStatus);
		list.add(item);

		if (OperatingStatus.IDLE.equals(operStatus)) {
			OPConfigItem idleItem = ValueUtils.map(item, OPConfigItem.class);
			idleItem.setOperatingStatus(OperatingStatus.NOT_WORKING);
			list.add(idleItem);
		}
	}

	public void saveOpConfigs(List<OPConfigItem> list) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}
		Map<EquipmentType, Map<OperatingStatus, OPConfigItem>> configs = OptionUtils.getContent("OP_CONFIG",
				new TypeReference<Map<EquipmentType, Map<OperatingStatus, OPConfigItem>>>() {
				});
		list.forEach(item -> {
			EquipmentType equipType = item.getEquipmentType();
			OperatingStatus operStatus = item.getOperatingStatus();
			if (equipType == null || operStatus == null) {
				return;
			} else if (OperatingStatus.NOT_WORKING.equals(operStatus)) {
				return;
			}

			Map<OperatingStatus, OPConfigItem> config;
			if (configs.containsKey(equipType)) {
				config = configs.get(equipType);
			} else {
				config = new LinkedHashMap<>();
				configs.put(equipType, config);
			}
			item.setEquipmentType(null);
			item.setOperatingStatus(null);
			config.put(operStatus, item);
		});
		BeanUtils.get(OptionService.class).saveContent("OP_CONFIG", configs);
	}

	public Map<String, Object> updateConfig(AllConfigPayload payload) {
		if (CollectionUtils.isNotEmpty(payload.getGeneralConfigPayloads())) {
			List<GeneralConfig> savedGeneralConfigs = new ArrayList<>();
			payload.getGeneralConfigPayloads().forEach(generalConfigPayload -> savedGeneralConfigs.add(setConfig(generalConfigPayload)));
			generalConfigRepository.saveAll(savedGeneralConfigs);
		}

		saveOpConfigs(payload.getOpConfigPayloads());

//		if (payload.getAlertConfigPayloads() != null) {
//			double value = ValueUtils.toDouble(payload.getAlertConfigPayloads().getPercentageThreshold(), 1d);
//			BeanUtils.get(OptionService.class).saveFieldValue(ConfigCategory.ALERT, "percentageThreshold", value);
//		}

		if (payload.getRefurbishmentConfigPayload() != null && payload.getRefurbishmentConfigPayload().getLm() != null && payload.getRefurbishmentConfigPayload().getMh() != null) {
			RefurbishmentConfigPayload configPayload = payload.getRefurbishmentConfigPayload();
			BeanUtils.get(OptionService.class).saveFieldValue(ConfigCategory.REFURBISHMENT, "mh", configPayload.getMh());
			BeanUtils.get(OptionService.class).saveFieldValue(ConfigCategory.REFURBISHMENT, "lm", configPayload.getLm());
			BeanUtils.get(OptionService.class).saveFieldValue(ConfigCategory.REFURBISHMENT, "hmMonths", configPayload.getHmMonths());
			BeanUtils.get(OptionService.class).saveFieldValue(ConfigCategory.REFURBISHMENT, "mlMonths", configPayload.getMlMonths());
		}

		Map<String, Object> result = new HashMap<>();
		result.put("general_config", generalConfigRepository.findAll());
		result.put("op_config", findAllOpConfig(new OPConfigPayload()));
		result.put("alert_config", findAllAlertConfig());
		result.put("refurbishment_config", OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class));
		return result;
	}

	@Deprecated
	@Transactional
	public List<Map<String, Object>> findEnableConfig(ConfigCategory configCategory) {
		List<Map<String, Object>> list = new ArrayList<>();
		if (configCategory != null) {
			Map<String, Object> config = new LinkedHashMap<>();
			config.put("configCategory", configCategory);
			config.put("enabled", OptionUtils.isEnabled(configCategory));
			list.add(config);
		} else {
			QOptionFieldValue table = QOptionFieldValue.optionFieldValue;
			BeanUtils.get(OptionFieldValueRepository.class).findAll(new BooleanBuilder()//
					.and(table.userId.isNull())//
					.and(table.fieldName.eq("enabled"))//
			).forEach(item -> {
				Map<String, Object> config = new LinkedHashMap<>();
				config.put("configCategory", item.getConfigCategory());
				config.put("enabled", ValueUtils.toBoolean(item.getValue(), false));
				list.add(config);
			});
		}
		return list;
	}

	@Deprecated
	@Transactional
	public Map<String, Object> updateEnableConfig(EnableConfigPayload payload) {
		if (payload == null || payload.getConfigCategory() == null) {
			return null;
		}
		Map<String, Object> config = BeanUtils.get(OptionService.class).getFieldValues(payload.getConfigCategory());
		config.put("enabled", ValueUtils.toBoolean(payload.getEnabled(), false));
		if (payload.getConfigOption() != null) {
			config.put("configOption", payload.getConfigOption().name());
		} else if (config.containsKey("configOption")) {
			config.put("configOption", null);
		}
		if (payload.getValue() != null) {
			config.put("value", payload.getValue());
		} else if (config.containsKey("value")) {
			config.put("value", null);
		}
		BeanUtils.get(OptionService.class).saveFieldValues(payload.getConfigCategory(), config);
		config.put("id", DateUtils2.newInstant().toEpochMilli());
		return config;
	}

	@Deprecated
	public Map<String, Object> findAllAlertConfig() {
		return new LinkedHashMap<>();
	}

	public Instant getDisconnectedChecktime() {
		OPConfigItem disconConfig = null;
		{
			List<OPConfigItem> configs = findAllOpConfig(OPConfigPayload.builder()//
					.equipmentType(EquipmentType.MOLD)//
					.operatingStatus(OperatingStatus.DISCONNECTED)//
					.build());
			if (!ObjectUtils.isEmpty(disconConfig)) {
				disconConfig = configs.get(0);
			}
		}

		if (disconConfig == null || disconConfig.getTime() == null) {
			return DateUtils2.newInstant().minus(Duration.ofHours(6));
		}

		if (ChronoUnit.DAYS.equals(disconConfig.getTimeUnit())) {
			return DateUtils2.newInstant().minus(Duration.ofMinutes((long) (disconConfig.getTime() * 24 * 60)));
		}

		return DateUtils2.newInstant().minus(Duration.ofHours(ValueUtils.toLong(disconConfig.getTime(), 0L)));
	}

	public ApiResponse deleteFixedProperty(GeneralConfigPayload payload) {
		GeneralConfig generalConfig = setConfig(payload);
		generalConfig.setDeletedField(true);
		generalConfigRepository.save(generalConfig);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> dataCompletionRateService.updateAllCompletionRate());
		executor.shutdown();

		return ApiResponse.success(Const.SUCCESS, generalConfig);
	}

	public ApiResponse validDeleteFixedProperty(GeneralConfigPayload payload) {
		if (payload.getConfigCategory() == null) {
			return ApiResponse.error("Config Category is required");
		}
		if (StringUtils.isEmpty(payload.getFieldName())) {
			return ApiResponse.error("Field Name is required");
		}
		if (payload.getConfigCategory().equals(ConfigCategory.TOOLING)) {
			List<String> fixedProperties = Arrays.asList("equipmentCode", "designedShot", "locationId"

					, "toolMakerCompanyName", "supplierCompanyName"

					, "approvedCycleTime", "cycleTimeLimit1", "cycleTimeLimit2", "uptimeTarget", "uptimeLimitL1", "uptimeLimitL2", "preventCycle", "preventUpcoming",
					"preventOverdue"

					, "partId", "cavity", "totalCavities");
			if (fixedProperties.contains(payload.getFieldName())) {
				return ApiResponse.error("Cannot delete this field.");

			}
		}
		if (payload.getConfigCategory().equals(ConfigCategory.PART)) {
			List<String> fixedProperties = Arrays.asList("category", "partCode", "name");
			if (fixedProperties.contains(payload.getFieldName())) {
				return ApiResponse.error("Cannot delete this field.");
			}
		}
		return null;
	}

	public ApiResponse getGraphSettingsConfig(ChartDataConfigType chartDataType) {
		Map<ChartDataConfigType, List<GraphSettingsConfigItem>> config = OptionUtils.getUserContent(//
				"GRAPH_SETTINGS", //
				new TypeReference<Map<ChartDataConfigType, List<GraphSettingsConfigItem>>>() {
				}//
		);

		List<GraphSettingsConfigItem> output = new ArrayList<>();
		if (chartDataType == null) {
			config.forEach((key, list) -> {
				list.forEach(item -> {
					item.setChartDataType(key);
					output.add(item);
				});
			});
		} else if (config.containsKey(chartDataType)) {
			List<GraphSettingsConfigItem> list = config.get(chartDataType);
			if (!ObjectUtils.isEmpty(list)) {
				list.forEach(item -> {
					item.setChartDataType(chartDataType);
					output.add(item);
				});
			}
		}

		return ApiResponse.success(CommonMessage.OK, output);
	}

	public ApiResponse updateGraphSettingsConfig(List<GraphSettingsConfigItem> payload) {
		List<GraphSettingsConfigItem> output = new ArrayList<>();
		Map<ChartDataConfigType, List<GraphSettingsConfigItem>> map = new LinkedHashMap<>();
		for (GraphSettingsConfigItem item : payload) {
			ChartDataConfigType key = item.getChartDataType();
			if (key == null) {
				continue;
			}
			output.add(ValueUtils.map(item, GraphSettingsConfigItem.class));

			item.setChartDataType(null);
			List<GraphSettingsConfigItem> list;
			if (map.containsKey(key)) {
				list = map.get(key);
			} else {
				list = new ArrayList<>();
				map.put(key, list);
			}
			list.add(item);
		}

		Map<ChartDataConfigType, List<GraphSettingsConfigItem>> config = OptionUtils.getUserContent(//
				"GRAPH_SETTINGS", //
				new TypeReference<Map<ChartDataConfigType, List<GraphSettingsConfigItem>>>() {
				}//
		);
		config.putAll(map);
		BeanUtils.get(OptionService.class).saveUserContent("GRAPH_SETTINGS", config);

		return ApiResponse.success(CommonMessage.OK, output);
	}

	public ApiResponse getChartTypeConfig(Long moldId) {
		List<ChartTypeConfigItem> config = BeanUtils.get(DspStpService.class).get().getChartTypeConfig();
		if (moldId != null && moldId > 0L) {
			if (BeanUtils.get(MoldRepository.class).count3rdGenerationDeviceById(moldId) > 0) {
				ChartTypeConfigItem item = new ChartTypeConfigItem();
				item.setChartDataType(ChartDataConfigType.ACCELERATION);
				item.setChartType(ChartType.BUBBLE);
				config.add(item);
			}
		}
		return ApiResponse.success(CommonMessage.OK, config);
	}

	//valid
	private static final Map<ConfigCategory, Map<String, String>> RESOURCE_COLL_DIFFERENT_NAME;

	static {
		RESOURCE_COLL_DIFFERENT_NAME = new LinkedHashMap<>();
		Map<String, String> toolingDiffMapper = new HashMap<String, String>() {
			{
				put(Const.ColumnCode.tooling.BASIC.engineers, "engineerIds");
				put(Const.ColumnCode.tooling.PRODUCTION.approvedCycleTime, "");
				put(Const.ColumnCode.tooling.MAINTENANCE.preventOverdue, "");
				put(Const.ColumnCode.tooling.PART.partId, "");//skip check
				put("accumulatedShots", "");//skip the check as it is a special case for the sensor
				put("instructionVideo", "");//skip the check as it is a special case for the sensor
				put("supplierCompanyName", "");//skip the check as it is a special case for the sensor
				put("toolMakerCompanyName", "");//skip the check as it is a special case for the sensor
				put("preventCycle", "");//change to PM planning
				put("preventUpcoming", "");//change to PM planning
			}
		};
		RESOURCE_COLL_DIFFERENT_NAME.put(ConfigCategory.TOOLING, toolingDiffMapper);
		Map<String, String> partDiffMapper = new HashMap<String, String>() {
			{
				put(Const.ColumnCode.part.category, MessageUtils.get("product", null));
			}
		};
		RESOURCE_COLL_DIFFERENT_NAME.put(ConfigCategory.PART, partDiffMapper);
	}

	public void validRequiredField(Object item, ConfigCategory configCategory) {
		if (!OptionUtils.isEnabled(configCategory)) {
			return;
		}
		GeneralConfigPayload payload = new GeneralConfigPayload();
		payload.setConfigCategory(configCategory);
		payload.setDeletedField(false);
		payload.setRequired(true);
		Map<String, String> diffFieldMapper = RESOURCE_COLL_DIFFERENT_NAME.containsKey(configCategory) ? RESOURCE_COLL_DIFFERENT_NAME.get(configCategory) : new HashMap<>();
		List<GeneralConfig> generalConfigList = findAll(payload.getPredicate());
		List<String> requiredFields = generalConfigList.stream().map(g -> g.getFieldName()).collect(Collectors.toList());
		requiredFields.forEach(field -> {
			String colName = diffFieldMapper.containsKey(field) ? diffFieldMapper.get(field) : field; // default field is filed in the item object
			if (!StringUtils.isEmpty(colName) && ReflectionUtils.getGetter(item, colName) != null)
				ValueUtils.assertNotEmpty(ValueUtils.get(item, colName), colName);
		});

	}
}
