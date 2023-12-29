package vn.com.twendie.avis.authen.controller;

import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.authen.model.payload.LoginRequest;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.service.TokenFirebaseService;
import vn.com.twendie.avis.security.core.filter.TokenProvider;
import vn.com.twendie.avis.security.core.model.UserPrincipal;
import vn.com.twendie.avis.security.core.payload.UserToken;
import vn.com.twendie.avis.security.jdbc.repository.MemberCustomerAuthenRepo;

@RestController
@Api("login")
@RequestMapping("/login")
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final TokenProvider tokenProvider;

    private final TokenFirebaseService tokenFirebaseService;

    private final MemberCustomerAuthenRepo memberCustomerRepo;

    public LoginController(AuthenticationManager authenticationManager,
                           TokenProvider tokenProvider,
                           TokenFirebaseService tokenFirebaseService,
                           MemberCustomerAuthenRepo memberCustomerRepo) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenFirebaseService = tokenFirebaseService;
        this.memberCustomerRepo = memberCustomerRepo;
    }

    @PostMapping("")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        if (StringUtils.isNotBlank(loginRequest.getPlayerId())) {
            tokenFirebaseService.saveIfNotExist(user.getId(), loginRequest.getPlayerId(), true);
        }

        UserToken userToken = tokenProvider.generateToken(authentication);
        if(userToken.getDepartmentName() == null){

        }

        return ResponseEntity.ok(ApiResponse.success(userToken));
    }

}
