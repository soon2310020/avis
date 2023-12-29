package vn.com.twendie.avis.authen.service;

import vn.com.twendie.avis.authen.model.payload.ChangePassRequest;

/**
 * @author trungnt
 *
 */
public interface PasswordService {

	void changePassword(ChangePassRequest request);

	void resetPassword(String username);

}
