package saleson.mms.productivity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/report/capacity-utilization")
public class ReportProductivityController {

    @GetMapping
    public String list() {
        return "front/capacity-utilization/index";
    }
}
