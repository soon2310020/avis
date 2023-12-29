package unused.resource.dashboard;

//@Deprecated
//public class ChartController {

//	@Deprecated
//	@PostMapping("dashboard-group")
//	public ResponseEntity updateDashboardGroup(@RequestBody DashboardGroupPayload payload) {
//		List<DashboardGroup> dashboardGroupList = BeanUtils.get(DashboardGroupService.class).saveGraphProfile(payload);
//		return ResponseEntity.ok(ApiResponse.success(Const.SUCCESS, dashboardGroupList));
//	}
//
//	@Deprecated
//	@GetMapping("dashboard-group")
//	public ResponseEntity getDashboardGroup(@RequestParam(required = false) DashboardGroupType dashboardGroupType) {
//		List<DashboardGroup> dashboardGroupList = BeanUtils.get(DashboardGroupService.class).getGraphProfile(dashboardGroupType);
//		return ResponseEntity.ok(ApiResponse.success(Const.SUCCESS, dashboardGroupList));
//	}
//
//	@Deprecated
//	@GetMapping("dashboard-group/reset")
//	public ApiResponse resetDefaultConfig() {
//		return BeanUtils.get(DashboardGroupService.class).resetDefaultConfig();
//	}
//
//	@Deprecated
//	@GetMapping("make-default-value-dashboard-group")
//	public ResponseEntity makeDefaultDashboardGroup(@RequestParam(required = false) DashboardGroupType dashboardGroupType) {
//		BeanUtils.get(DashboardGroupService.class).makeDefaultConfig();
//		return ResponseEntity.ok(ApiResponse.success(Const.SUCCESS));
//	}
//
//}
