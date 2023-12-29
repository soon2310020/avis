package vn.com.twendie.avis.api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${account.default-password.customer}")
    private String customerDefaultPass;

    @Value("${account.default-password.driver}")
    private String driverDefaultPass;

    @Value("${account.default-password.admin}")
    private String adminUserDefaultPass;

    @Value("${account.default-password.signature}")
    private String signatureDefaultPassword;

    public String getCustomerDefaultPass() {
        return passwordEncoder.encode(customerDefaultPass);
    }

    public String getDriverDefaultPass() {
        return passwordEncoder.encode(driverDefaultPass);
    }

    public String getAdminUserDefaultPass() {
        return passwordEncoder.encode(adminUserDefaultPass);
    }

    public String getSignatureDefaultPassword(){
        return passwordEncoder.encode(signatureDefaultPassword);
    }

    public String encryptPassword(String input) {
        return passwordEncoder.encode(input);
    }
}
