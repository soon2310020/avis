package saleson.mms.machine;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/machines")
public class AdminMachineController {
    
    @GetMapping
    public String list() {
        return "admin/machines/list";
    }
    
    @GetMapping("/new")
	public String create() {
		return "admin/machines/form";
	}
	
	@GetMapping("/{id}")
	public String edit(@PathVariable(value = "id") Integer id) {
		return "admin/machines/form";
	}
	@GetMapping("/tooling/{id}")
	public String tooling(@PathVariable(value = "id") Integer id) {
		return "admin/machines/matchTooling";
	}
}
