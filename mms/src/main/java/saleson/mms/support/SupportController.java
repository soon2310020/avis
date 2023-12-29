package saleson.mms.support;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/support")
public class SupportController {

    @GetMapping("/customer-support")
    public String list() {
        return "front/support/list";
    }

    @GetMapping("/customer-support/new")
    public String createTopic(){
        return "front/support/form";
    }

    @GetMapping("/customer-support/detail/{id}")
    public String detail(){
        return "front/support/detail";
    }

    @GetMapping("/tutorial")
    public String tutorial(){
        return "front/support/tutorial";
    }
    @GetMapping("/version-history")
    public String version(){
        return "front/support/version-history";
    }
}
