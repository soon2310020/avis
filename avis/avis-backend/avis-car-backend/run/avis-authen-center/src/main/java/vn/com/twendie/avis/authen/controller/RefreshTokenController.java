package vn.com.twendie.avis.authen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.security.core.filter.TokenProvider;
import vn.com.twendie.avis.security.core.payload.Token;

@RestController
@Api("refresh")
@RequestMapping("/refresh-token")
public class RefreshTokenController {

    private final TokenProvider tokenProvider;

    public RefreshTokenController(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("")
    public ResponseEntity<?> refreshToken(@RequestParam("refresh_token") String refreshToken,
                                          @RequestParam(value = "access_token", required = false) String accessToken) {
        Token token = tokenProvider.refreshToken(refreshToken, accessToken);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

}
