package saleson.mms.currency;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/currency")
public class AdminCurrencyController {

    @GetMapping
    public String index(){
        return "admin/currency/index";
    }
}
