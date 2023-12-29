package saleson.api.dashboardChartDisplaySetting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saleson.api.dashboardChartDisplaySetting.payload.DashboardChartDisplaySettingLite;
import saleson.common.enumeration.DashboardChartType;
import saleson.common.payload.ApiResponse;

@Deprecated
@RestController
@RequestMapping("/api/dashboard-chart-display-settings")
public class DashboardChartDisplaySettingController {
	@Autowired
	DashboardChartDisplaySettingService dashboardChartDisplaySettingService;

	@GetMapping
	public ResponseEntity<ApiResponse> getDashboardChartDisplaySetting() {
		return ResponseEntity.ok(dashboardChartDisplaySettingService.getDashboardChartDisplaySetting());
	}

	@PostMapping
	public ResponseEntity<ApiResponse> saveDashboardChartDisplaySetting(@RequestBody DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite) {
		return ResponseEntity.ok(dashboardChartDisplaySettingService.save(dashboardChartDisplaySettingLite));
	}

	@GetMapping(value = "/get-by-chart-type")
	public ResponseEntity<ApiResponse> getDashboardChartDisplaySettingByChartType(@RequestParam(value = "chartType") DashboardChartType chartType) {
		return ResponseEntity.ok(dashboardChartDisplaySettingService.getDashboardChartDisplaySettingByChartType(chartType));
	}
}
