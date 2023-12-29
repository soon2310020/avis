package vn.com.twendie.avis.security.core.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Builder
@Data
public class UserToken {
	@JsonProperty("access_token")
	// TODO: uncomment below code to stop logging user token
	// @ToString.Exclude
	private String accessToken;

	@JsonProperty("refresh_token")
	// TODO: uncomment below code to stop logging user token
	// @ToString.Exclude
	private String refreshToken;

	@JsonProperty("roles")
	private List<String> listRole;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("username")
	private String username;

	@JsonProperty("name")
	private String name;

	@JsonProperty("email")
	private String email;

	@JsonProperty("expires_in")
	private Integer expiresIn;

	@JsonProperty("avatar")
	private String avatar;

	@JsonProperty("first_time_login")
	private boolean firstTimeLogin;

	@JsonProperty("department_name")
	private String departmentName;

}
