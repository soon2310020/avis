package saleson.mms.workOrder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/work-order")
public class AdminWorkOrderController {

    @GetMapping
    public String list() {
        return "admin/work-order/index";
    }
}
