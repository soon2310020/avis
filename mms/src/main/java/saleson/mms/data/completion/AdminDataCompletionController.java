package saleson.mms.data.completion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/data-completion")
public class AdminDataCompletionController {

    @GetMapping
    public String list() {
        return "admin/data-completion/list";
    }

    @GetMapping("/new")
    public String create() {
        return "admin/data-completion/form";
    }

    @GetMapping("/{id}")
    public String edit(@PathVariable(value = "id") Integer id) {
        return "admin/data-completion/form";
    }
}
