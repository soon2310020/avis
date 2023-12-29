package saleson.mms.codelist;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/codelist-center")
public class AdminCodelistCenterController {
    @GetMapping
    public String list() {
        return "admin/codelist-center/index";
    }
}
