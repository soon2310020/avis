package saleson.mms.machine;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/machine-availability")
public class AdminMachineAvailabilityController {

    @GetMapping
    public String list() {
        return "admin/machine-availability/list";
    }
}
