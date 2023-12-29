package saleson.mms.cycletimereport;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/report/cycle-time")
public class CycleTimeReportController {

    @GetMapping
    public String list() {
        return "front/cycle-time/index";
    }
}
