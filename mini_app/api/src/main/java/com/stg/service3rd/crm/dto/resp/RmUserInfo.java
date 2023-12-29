package com.stg.service3rd.crm.dto.resp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RmUserInfo {
	private String sub;
	private Boolean email_verified;
	private String preferred_username;
	private String email;
}
