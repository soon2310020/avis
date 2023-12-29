package saleson.mms.rejectrate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/reject-rates")
public class AdminRejectRateController {
    @GetMapping
    public String list() {
        return "admin/reject-rates/list";
    }
    @GetMapping("edit")
	public String edit(@RequestParam(value = "id", required = true) Integer id) {
		return "admin/reject-rates/edit-reject-rate";
	}
    @GetMapping("multiple-edit")
	public String edit(@RequestParam(value = "ids", required = true) String id) {
		return "admin/reject-rates/multiple-edit-reject-rate";
	}

    @GetMapping("/chart/{chart-param}")
    public String partDetailChart(@PathVariable("chart-param") String chartParam ) {
        return "admin/reject-rates/chart";
    }

}
