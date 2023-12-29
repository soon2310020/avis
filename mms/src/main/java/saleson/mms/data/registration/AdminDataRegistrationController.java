package saleson.mms.data.registration;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/data-registration")
public class AdminDataRegistrationController {

    @GetMapping
    public String list() {
        return "admin/data-registration/list";
    }

    @GetMapping("/new")
    public String create() {
        return "admin/data-registration/form";
    }

    @GetMapping("/{id}")
    public String edit(@PathVariable(value = "id") Integer id) {
        return "admin/data-registration/form";
    }
}
