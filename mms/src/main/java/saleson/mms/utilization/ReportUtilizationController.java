package saleson.mms.utilization;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/report/utilization")
public class ReportUtilizationController {

    @GetMapping
    public String list() {
        return "front/utilization/index";
    }
}
