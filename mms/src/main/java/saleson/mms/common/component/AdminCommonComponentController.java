package saleson.mms.common.component;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/common-component")
public class AdminCommonComponentController {

    @GetMapping
    public String list() {
        return "admin/common-component/list";
    }

}
