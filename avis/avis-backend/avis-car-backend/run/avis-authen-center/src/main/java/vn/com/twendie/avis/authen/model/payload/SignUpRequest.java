package vn.com.twendie.avis.authen.model.payload;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SignUpRequest {

	@NotBlank
	@Size(min = 3, max = 255)
	private String username;

	@Size(max = 255)
	@Email
	private String email;

	@NotBlank
	@Size(min = 6, max = 20)
	// TODO: uncomment below code to stop logging user password
	// @ToString.Exclude
	private String password;

	@Size(max = 255)
	private String code;

	@Size(max = 255)
	private String name;

	@Size(max = 255)
	private String idCard;

	@Size(max = 255)
	private String mobile;

	@Size(max = 255)
	private String address;

	@Size(max = 255)
	private String department;

	private Integer userRoleId;

	private Integer userTypeId;

}
