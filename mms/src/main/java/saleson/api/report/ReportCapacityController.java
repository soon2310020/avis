package saleson.api.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.common.util.DateUtils;
import saleson.dto.RestDataList;
import saleson.model.data.cycleTime.ToolingCycleTimeData;
import saleson.model.data.productivity.ToolingProductivityData;
import saleson.model.data.supplierReport.SupplierProductionData;

import java.util.HashMap;

@RestController
@RequestMapping("/api/reports")
public class ReportCapacityController {
    @Autowired
    ReportService reportService;

    @GetMapping("/productivity/tooling")
    public ResponseEntity<?> getOverviewProductivity(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd"));
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()));
        }

        return ResponseEntity.ok(reportService.getOverviewProductivity(payload, pageable));
    }

    @GetMapping("/productivity/tooling-top5")
    public ResponseEntity<?> getTop5ToolingProductivity(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd"));
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()));
        }
        RestDataList<ToolingProductivityData> restDataList =reportService.getTop5ToolingProductivity(null, payload, pageable,false);
        return ResponseEntity.ok(restDataList.getDataList());
    }

    @GetMapping("/productivity/tooling-list")
    public ResponseEntity<?> getToolingProductivityDetailsList(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd"));
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()));
        }

        return ResponseEntity.ok(reportService.getToolingProductivityDetailsList(payload, pageable));
    }

    @GetMapping("/cycle-time/tooling")
    public ResponseEntity<?> getOverviewCycleTime(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd"));
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()));
        }
        return ResponseEntity.ok(reportService.getOverviewCycleTime(payload, pageable));
    }

    @GetMapping("/cycle-time/tooling-top5")
    public ResponseEntity<?> getTop5ToolingCycleTime(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd"));
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()));
        }

        RestDataList<ToolingCycleTimeData> restDataList =reportService.getTop5ToolingCycleTime(null, payload, pageable,null,new HashMap<>() ,false);
        return ResponseEntity.ok(restDataList.getDataList());
    }

    @GetMapping("/supplier/production")
    public ResponseEntity<?> getOverviewSupplierProduction(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd") + "000000");
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()) + "000000");
        }

        return ResponseEntity.ok(reportService.getOverviewSupplierProduction(payload, pageable));
    }

    @GetMapping("/supplier/supplier-top5")
    public ResponseEntity<?> getTop5SupplierProduction(ProductivitySearchPayload payload, Pageable pageable){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd") + "000000");
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()) + "000000");
        }

        RestDataList<SupplierProductionData> restDataList =reportService.getTop5SupplierProduction(null, payload, pageable);

        return ResponseEntity.ok(restDataList.getDataList());
    }
    @GetMapping("/supplier/supplier-list")
    public ResponseEntity<?> getSupplierProductionDetailsList(ProductivitySearchPayload payload){
        payload.setEndDate(DateUtils.getYesterday("yyyyMMdd") + "000000");
        if(payload.getDuration() != null){
            payload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()) + "000000");
        }

        return ResponseEntity.ok(reportService.getSupplierProductionList(null,payload));
    }

}
