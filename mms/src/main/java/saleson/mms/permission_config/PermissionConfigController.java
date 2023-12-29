package saleson.mms.permission_config;

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
@RequestMapping("/admin/permission-config")
public class PermissionConfigController {
//    UserService userService;

//    @Autowired
//    VersioningService versioningService;

    @GetMapping
    public String list() {
        return "admin/permission-config/list";
    }

//    @GetMapping("/new")
//    public String create(Model model) {
//
//        return "admin/role-config/form";
//    }
//
//
//    @GetMapping("/{id}")
//    public String edit(@PathVariable("id") Long id, Model model) {
//
//
//        model.addAttribute("id", id);
//        return "admin/role-config/form";
//    }
//
//
//
//    @PostMapping("/edit")
//    public ResponseEntity<?> edit(UserParam param) {
//
//        User user = userService.findById(param.getId());
//
//        if (user == null) {
//            return ResponseEntity.ok(new ApiResponse(false, "User does not exists"));
//        }
//        versioningService.writeHistory(user);
//        user.setEnabled(param.isEnabled());
//
//        userService.save(user);
//
//        return ResponseEntity.ok(new ApiResponse(true, "User update success." + param.getId()));
//    }

}
