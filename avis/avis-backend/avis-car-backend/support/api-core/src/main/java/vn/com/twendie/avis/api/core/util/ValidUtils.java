package vn.com.twendie.avis.api.core.util;

import java.text.Normalizer;

public class ValidUtils {

    public static String normalizeString(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFC);
    }
}
