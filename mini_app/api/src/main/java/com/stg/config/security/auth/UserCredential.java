package com.stg.config.security.auth;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCredential implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String username;
	private String rmCode;
	private String icCode;
	private String branchCode;
	private String branchName;
	private String fullName;
	private String phoneNumber;
    private String email;
	
}
