package saleson.api.filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import saleson.api.filter.payload.DashboardGeneralFilterPayload;
import saleson.common.constant.CommonMessage;
import saleson.common.payload.ApiResponse;
import saleson.model.DashboardGeneralFilter;

@RestController
@RequestMapping("/api/filters")
public class DashboardGeneralFilterController {
    @Autowired
    private DashboardGeneralFilterService dashboardGeneralFilterService;

    @GetMapping("/dashboard-general-filter")
    public ApiResponse get() {
        return ApiResponse.success(CommonMessage.OK, dashboardGeneralFilterService.get());
    }

    @PostMapping("/dashboard-general-filter/update")
    public ApiResponse save(@RequestBody DashboardGeneralFilterPayload payload) {
        return dashboardGeneralFilterService.save(payload);
    }

    @GetMapping("/dashboard-individual-filter")
    public ApiResponse getIndividualFilter() {
        return dashboardGeneralFilterService.getToolingAndPartByGeneralFilter();
    }
}
