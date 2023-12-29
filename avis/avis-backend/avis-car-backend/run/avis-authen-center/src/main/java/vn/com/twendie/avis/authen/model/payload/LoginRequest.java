package vn.com.twendie.avis.authen.model.payload;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
	@NotBlank
	private String usernameOrEmail;

	@NotBlank
	// TODO: uncomment below code to stop logging user password
	// @ToString.Exclude
	private String password;

	private String playerId;
}
