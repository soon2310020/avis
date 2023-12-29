package vn.com.twendie.avis.authen.controller;


import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.service.TokenFirebaseService;
import vn.com.twendie.avis.security.core.filter.TokenProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Collections;

@RestController
@Api("log-out")
@RequestMapping("/log-out")
public class LogoutController {

    private final TokenProvider tokenProvider;

    private final TokenFirebaseService tokenFirebaseService;

    public LogoutController(TokenProvider tokenProvider,
                            TokenFirebaseService tokenFirebaseService) {
        this.tokenProvider = tokenProvider;
        this.tokenFirebaseService = tokenFirebaseService;
    }

    @GetMapping("")
    @Transactional
    public ResponseEntity<?> signOut(@RequestParam(value = "player_id", required = false) String playerId,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            tokenProvider.clearTokenOnSystem(token);
        }
        if (StringUtils.isNotBlank(playerId)) {
        	tokenFirebaseService.deleteAllByToken(playerId);
		}
        return ResponseEntity.ok(ApiResponse.success(Collections.EMPTY_MAP));
    }

}
