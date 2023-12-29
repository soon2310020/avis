package saleson.api.dashboardSetting;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saleson.api.dashboardSetting.payload.DashboardSettingsLite;
import saleson.common.payload.ApiResponse;

@Deprecated
@RestController
@RequestMapping("/api/dashboard-settings")
public class DashboardSettingController {
	@Autowired
	DashboardSettingService dashboardSettingService;

	@GetMapping
	public ResponseEntity<List<DashboardSettingsLite>> getAllDashboardSettings() {
		return ResponseEntity.ok(dashboardSettingService.getAllDashboardSetting());
	}

	@PutMapping("/change-checked")
	public ResponseEntity<ApiResponse> changeChecked(@RequestBody List<DashboardSettingsLite> updateCheckedPayloadList) {
		return ResponseEntity.ok(dashboardSettingService.changeChecked(updateCheckedPayloadList));
	}

}
