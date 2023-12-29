package saleson.mms.data.requests;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/data-requests")
public class AdminDataRequestsController {

    @GetMapping
    public String list() {
        return "admin/data-requests/list";
    }
}
