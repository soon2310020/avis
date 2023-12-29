package vn.com.twendie.avis.mobile.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.mobile.api.model.response.DriverCommonInfo;
import vn.com.twendie.avis.mobile.api.service.UserService;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

@RestController
@RequestMapping("/driver")
public class DriverController {

    @Autowired
    private UserService userService;

    @GetMapping("/common-info")
    public ResponseEntity<?> getUserInfo(@CurrentUser UserDetails userDetails) {
        User user = ((UserPrincipal) userDetails).getUser();
        DriverCommonInfo driverCommonInfo = userService.getDriverCommonInfo(user);
        return ResponseEntity.ok(ApiResponse.success(driverCommonInfo));
    }
}
