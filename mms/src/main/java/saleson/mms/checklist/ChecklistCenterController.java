package saleson.mms.checklist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/checklist-center")
public class ChecklistCenterController {
    @GetMapping
    public String list() {
        return "admin/checklist-center/index";
    }


    @GetMapping("/maintenance/new")
    public String maintenanceForm() {
        return "admin/maintenance-checklist/form";
    }
    @GetMapping("/maintenance/{id}/edit")
    public String maintenanceEdit() {
        return "admin/maintenance-checklist/form";
    }


    @GetMapping("/reject-rate/new")
    public String form() {
        return "admin/reject-rate-checklist/form";
    }
    @GetMapping("/reject-rate/{id}/edit")
    public String edit() {
        return "admin/reject-rate-checklist/form";
    }

}
