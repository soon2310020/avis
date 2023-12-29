package saleson.mms.supplier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/report/production-efficiency")
public class SupplierController {

    @GetMapping
    public String list() {
        return "front/production-efficiency/index";
    }
}
