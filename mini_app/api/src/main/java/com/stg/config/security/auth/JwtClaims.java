package com.stg.config.security.auth;

import io.jsonwebtoken.impl.DefaultClaims;

public class JwtClaims extends DefaultClaims {

	public static final String RM_CODE = "rmCode";
	public static final String IC_CODE = "icCode";
	public static final String BRANCH_CODE = "branchCode";
	public static final String BRANCH_NAME = "branchName";
	public static final String FULL_NAME = "fullName";
	public static final String PHONE_NUMBER = "phoneNumber";
	public static final String EMAIL = "email";

	public JwtClaims() {
		super();
	}

	public JwtClaims setRmCode(String rmCode) {
		setValue(RM_CODE, rmCode);
		return this;
	}
	
	public JwtClaims setIcCode(String icCode) {
		setValue(IC_CODE, icCode);
		return this;
	}
	
	public JwtClaims setBranchCode(String branchCode) {
	    setValue(BRANCH_CODE, branchCode);
	    return this;
	}
	
	public JwtClaims setBranchName(String branchName) {
		setValue(BRANCH_NAME, branchName);
		return this;
	}
	
	public JwtClaims setFullName(String icCode) {
	    setValue(FULL_NAME, icCode);
	    return this;
	}
	
	public JwtClaims setPhoneNumber(String phoneNumber) {
	    setValue(PHONE_NUMBER, phoneNumber);
	    return this;
	}

	public JwtClaims setEmail(String email) {
        setValue(EMAIL, email);
        return this;
    }
	
	public String getRmCode() {
		return (String) get(RM_CODE);
	}

	public String getIcCode() {
		return (String) get(IC_CODE);
	}
	
	public String getBranchCode() {
		return (String) get(BRANCH_CODE);
	}
	
	public String getBranchName() {
        return (String) get(BRANCH_NAME);
    }
	
	public String getFullName(String icCode) {
	    return (String) get(FULL_NAME);
    }
    
    public String getPhoneNumber(String phoneNumber) {
        return (String) get(PHONE_NUMBER);
    }

    public String getEmail(String email) {
        return (String) get(EMAIL);
    }
	
}
