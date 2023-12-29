package saleson.mms.time_language;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/time-language")
public class AdminTimeAndLanguageController {

    @GetMapping
    public String index(){
        return "admin/time-language/index";
    }
}
