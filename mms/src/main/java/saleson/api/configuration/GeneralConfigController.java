package saleson.api.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.option.dto.GraphSettingsConfigItem;
import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;

import saleson.api.configuration.payload.AlertConfigPayload;
import saleson.api.configuration.payload.AllConfigPayload;
import saleson.api.configuration.payload.ColumnTableConfigPayload;
import saleson.api.configuration.payload.EnableConfigPayload;
import saleson.api.configuration.payload.GeneralConfigPayload;
import saleson.api.configuration.payload.OPConfigPayload;
import saleson.common.enumeration.ChartDataConfigType;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.PageType;
import saleson.common.payload.ApiResponse;

@RestController
@RequestMapping("/api/config")
public class GeneralConfigController {
	@Autowired
	private GeneralConfigService generalConfigService;
	@Autowired
	private ColumnTableConfigService columnTableConfigService;

	@GetMapping
	public ResponseEntity<?> getConfigs(GeneralConfigPayload payload) {
		return ResponseEntity.ok(generalConfigService.findAll(payload.getPredicate()));
	}

	@PostMapping("/update")
	public ResponseEntity<?> setConfig(@RequestBody GeneralConfigPayload payload) {
		AllConfigPayload allConfigPayload = AllConfigPayload.builder().generalConfigPayloads(Arrays.asList(payload)).build();
		return ResponseEntity.ok(generalConfigService.updateConfig(allConfigPayload));
	}

	@PostMapping("/delete-fixed-property")
	public ResponseEntity<?> deleteFixedProperty(@RequestBody GeneralConfigPayload payload) {
		ApiResponse valid = generalConfigService.validDeleteFixedProperty(payload);
		if (valid != null)
			return ResponseEntity.ok(valid);

		return ResponseEntity.ok(generalConfigService.deleteFixedProperty(payload));
	}

	@GetMapping("/op-config")
	public ResponseEntity<?> getOPConfigs(OPConfigPayload payload) {
		return ResponseEntity.ok(generalConfigService.findAllOpConfig(payload));
	}

	@GetMapping("/alert-config")
	public ResponseEntity<?> getAlertConfigs(AlertConfigPayload payload) {
		return ResponseEntity.ok(generalConfigService.findAllAlertConfig());
	}

	@GetMapping("/refurbishment-config")
	public ResponseEntity<?> getRefurbishmentConfigs() {
		return ResponseEntity.ok(OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class));
	}

	@PostMapping("/update/all")
	public ResponseEntity<?> updateConfig(@RequestBody AllConfigPayload payload) {
		return ResponseEntity.ok(generalConfigService.updateConfig(payload));
	}

	@Deprecated
	@GetMapping("/config-enable")
	public ResponseEntity<?> getEnableConfig(@RequestParam(required = false) ConfigCategory configCategory) {
		return ResponseEntity.ok(generalConfigService.findEnableConfig(configCategory));
	}

	@Deprecated
	@PostMapping("/update-config-enable")
	public ResponseEntity<?> updateEnableConfig(@RequestBody EnableConfigPayload payload) {
		return ResponseEntity.ok(generalConfigService.updateEnableConfig(payload));
	}

	@GetMapping("/column-config")
	public ResponseEntity<?> getColumnConfig(@RequestParam(name = "pageType", required = true) PageType pageType) {
		return ResponseEntity.ok(columnTableConfigService.getColumnConfig(pageType));
	}

	@PostMapping("/update-column-config")
	public ResponseEntity<?> updateColumnConfig(@RequestBody ColumnTableConfigPayload payload) {
		return ResponseEntity.ok(columnTableConfigService.updateColumnConfig(payload));
	}

	@GetMapping("/graph-settings-config")
	public ResponseEntity<?> getGraphSettingsConfig(@RequestParam(required = false) ChartDataConfigType chartDataType) {
		return ResponseEntity.ok(generalConfigService.getGraphSettingsConfig(chartDataType));
	}

	@PostMapping("/graph-settings-config")
	public ResponseEntity<?> updateGraphSettingsConfig(@RequestBody List<GraphSettingsConfigItem> userGraphSettingsConfigPayLoads) {
		return ResponseEntity.ok(generalConfigService.updateGraphSettingsConfig(userGraphSettingsConfigPayLoads));
	}

	@GetMapping("/chart-type-config")
	public ResponseEntity<?> getChartTypeConfig(@RequestParam(required = false) Long moldId) {
		return ResponseEntity.ok(generalConfigService.getChartTypeConfig(moldId));
	}

}
