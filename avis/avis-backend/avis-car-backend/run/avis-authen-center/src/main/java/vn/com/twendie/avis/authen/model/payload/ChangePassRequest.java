package vn.com.twendie.avis.authen.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.twendie.avis.api.core.constraint.IgnoreTrimReflect;
import vn.com.twendie.avis.api.core.constraint.Password;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChangePassRequest {

	@JsonProperty("username")
	private String username;

	@JsonProperty("old_password")
	@IgnoreTrimReflect
	private String oldPassword;

	@JsonProperty("new_password")
	@Password(message = "user.error.password_is_invalid")
	@IgnoreTrimReflect
	private String newPassword;

}
