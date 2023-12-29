package saleson.mms.insight;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/insight")
public class InsightController {

    @GetMapping
    public String list() {
        return "front/insight/list";
    }
}
