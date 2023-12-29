package com.emoldino.api.common.resource.composite.cfgstp.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.object.dto.CustomFieldGetIn;
import com.emoldino.api.common.resource.base.object.service.customfield.CustomFieldService;
import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetOut;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpPostIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpPostOut;
import com.emoldino.framework.util.BeanUtils;

import saleson.api.configuration.GeneralConfigService;
import saleson.api.configuration.payload.GeneralConfigPayload;
import saleson.api.configuration.payload.OPConfigPayload;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;

@Transactional
@Service
public class CfgStpService {
//	@Autowired
//	private ApplicationContext beans;

	@Autowired
	private GeneralConfigService gcService;

	@Autowired
	private CustomFieldService cfService;

	@Autowired
	private OptionService service;

	public CfgStpGetOut get(CfgStpGetIn input) {
		CfgStpGetOut output = new CfgStpGetOut();

		if (input.getConfigCategory().contains(ConfigCategory.ALERT)) {
			Map<String, Object> option = BeanUtils.get(OptionService.class).getFieldValues(ConfigCategory.ALERT);
			output.getOptions().put(ConfigCategory.ALERT, option);
		}

		if (input.getConfigCategory().contains(ConfigCategory.OPTIMAL_CYCLE_TIME)) {
			Map<String, Object> option = service.getFieldValues(ConfigCategory.OPTIMAL_CYCLE_TIME);
			applyDefaultValue(option, "strategy", "WACT");
			applyDefaultValue(option, "periodMonths", 3);
			output.getOptions().put(ConfigCategory.OPTIMAL_CYCLE_TIME, option);
		}

		// TODO Migrate to Option Value (GET Shot Filter Config)
		if (input.getConfigCategory().contains(ConfigCategory.DATA_FILTER)) {
			Map<String, Object> option = BeanUtils.get(OptionService.class).getFieldValues(ConfigCategory.DATA_FILTER);
			output.getOptions().put(ConfigCategory.DATA_FILTER, option);
		}

		// TODO Migrate to Option Value (GET Part Config)
		if (input.getConfigCategory().contains(ConfigCategory.PART)) {
			Map<String, Object> option = new LinkedHashMap<>();
			setEnabled(ConfigCategory.PART, option);
			{
				GeneralConfigPayload payload = new GeneralConfigPayload();
				payload.setConfigCategory(ConfigCategory.PART);
				option.put("fields", gcService.findAll(payload.getPredicate()));
			}
			option.put("customFields", cfService.get(CustomFieldGetIn.builder().objectType(ObjectType.PART).build()));
			output.getOptions().put(ConfigCategory.PART, option);
		}

		// TODO Migrate to Option Value (GET Tooling Config)
		if (input.getConfigCategory().contains(ConfigCategory.TOOLING)) {
			Map<String, Object> option = new LinkedHashMap<>();
			setEnabled(ConfigCategory.TOOLING, option);
			{
				GeneralConfigPayload payload = new GeneralConfigPayload();
				payload.setConfigCategory(ConfigCategory.TOOLING);
				option.put("fields", gcService.findAll(payload.getPredicate()));
			}
			option.put("customFields", cfService.get(CustomFieldGetIn.builder().objectType(ObjectType.TOOLING).build()));
			output.getOptions().put(ConfigCategory.TOOLING, option);
		}

		// TODO Migrate to Option Value (GET OP Config)
		if (input.getConfigCategory().contains(ConfigCategory.OP)) {
			Map<String, Object> option = new LinkedHashMap<>();
			setEnabled(ConfigCategory.OP, option);
			option.put("rules", gcService.findAllOpConfig(new OPConfigPayload()));
			output.getOptions().put(ConfigCategory.OP, option);
		}

		if (input.getConfigCategory().contains(ConfigCategory.REFURBISHMENT)) {
			RefurbishmentConfig config = OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class);
			output.getOptions().put(ConfigCategory.REFURBISHMENT, config);
		}

		if (input.getConfigCategory().contains(ConfigCategory.DASHBOARD_GROUP)) {
			Map<String, Object> option = BeanUtils.get(OptionService.class).getFieldValues(ConfigCategory.DASHBOARD_GROUP);
			output.getOptions().put(ConfigCategory.DASHBOARD_GROUP, option);
		}

		return output;
	}

	private static void applyDefaultValue(Map<String, Object> option, String fieldName, Object defaultValue) {
		if (option.containsKey(fieldName)) {
			return;
		}
		option.put(fieldName, defaultValue);
	}

	private void setEnabled(ConfigCategory configCategory, Map<String, Object> option) {
		option.put("enabled", OptionUtils.isEnabled(configCategory));
	}

	public CfgStpPostOut post(CfgStpPostIn input) {
		Map<ConfigCategory, Map<String, Object>> options = input.getOptions();
		if (ObjectUtils.isEmpty(options)) {
			return new CfgStpPostOut();
		}

		options.forEach((name, option) -> {
			if (ObjectUtils.isEmpty(option)) {
				return;
			}
			service.saveFieldValues(name, option);
		});

		return new CfgStpPostOut();
	}

}
