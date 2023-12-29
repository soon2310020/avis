package saleson.mms.appDownload;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/download-app")
public class AppDownloadController {

    @GetMapping
    public String list() {
        return "front/download-app/index";
    }
}
