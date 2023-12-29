package vn.com.twendie.avis.api.util;

import java.util.Objects;

public class PhoneNumberUtil {

    public static String toMobileFullPattern(String countryCode, String mobile) {
        if (Objects.isNull(countryCode) || Objects.isNull(mobile)) {
            return "";
        }
        return "+" + countryCode + mobile;
    }
}
