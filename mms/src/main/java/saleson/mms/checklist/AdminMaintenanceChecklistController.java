package saleson.mms.checklist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/maintenance-checklist")
public class AdminMaintenanceChecklistController {
    @GetMapping
    public String list() {
        return "admin/maintenance-checklist/list";
    }

    @GetMapping("/new")
    public String form() {
        return "admin/maintenance-checklist/form";
    }
    @GetMapping("/{id}/edit")
    public String edit() {
        return "admin/maintenance-checklist/form";
    }
}
