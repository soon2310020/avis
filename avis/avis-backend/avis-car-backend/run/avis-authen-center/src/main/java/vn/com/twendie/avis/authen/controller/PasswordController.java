package vn.com.twendie.avis.authen.controller;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.authen.model.payload.ChangePassRequest;
import vn.com.twendie.avis.authen.service.PasswordService;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@Api("password")
@RequestMapping("/password")
public class PasswordController {

	private final PasswordService passwordService;

	public PasswordController(PasswordService passwordService) {
		this.passwordService = passwordService;
	}

	@PostMapping("/change")
	public ResponseEntity<?> changePass(@Valid @RequestBody ChangePassRequest request) {
		passwordService.changePassword(request);
		return ResponseEntity.ok(ApiResponse.success(Collections.EMPTY_MAP));
	}

	@PostMapping("/reset")
	public ResponseEntity<?> resetPass() {
		return null;
	}
}
