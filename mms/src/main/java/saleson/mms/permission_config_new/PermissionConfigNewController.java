package saleson.mms.permission_config_new;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.api.versioning.service.VersioningService;
import saleson.common.payload.ApiResponse;
import saleson.model.User;


@Controller
@RequestMapping("/admin/permission-config-new")
public class PermissionConfigNewController {

    @GetMapping
    public String list() {
        return "admin/permission-config-new/list";
    }



}
