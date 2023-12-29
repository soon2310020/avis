package saleson.api.tabbedFilter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.constant.CommonMessage;
import saleson.common.payload.ApiResponse;

@RestController
@RequestMapping("/api/tabbed-filters")
public class TabbedOverviewGeneralFilterController {
    @Autowired
    private TabbedOverviewGeneralFilterService tabbedOverviewGeneralFilterService;

    @GetMapping("/general-filter")
    public ApiResponse get() {
        return ApiResponse.success(CommonMessage.OK, tabbedOverviewGeneralFilterService.get());
    }

    @PostMapping("/general-filter/update")
    public ApiResponse save(@RequestBody TabbedOverviewGeneralFilterPayload payload) {
        return ApiResponse.success(CommonMessage.OK, tabbedOverviewGeneralFilterService.save(payload));
    }

/*    @GetMapping("/individual-filter")
    public ApiResponse getIndividualFilter() {
        return tabbedOverviewGeneralFilterService.getToolingAndPartByGeneralFilter();
    }
    */
}
