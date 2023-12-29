package saleson.mms.nothis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/not-his")
public class NotHisController {
  @GetMapping
  public String list() {
    return "admin/not-his/index";
  }
}