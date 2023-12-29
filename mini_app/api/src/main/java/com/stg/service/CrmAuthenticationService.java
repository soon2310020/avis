package com.stg.service;

import com.stg.config.security.auth.AccessToken;

public interface CrmAuthenticationService {

	AccessToken verifyToken(String token);

}
