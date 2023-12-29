package vn.com.twendie.avis.api.core.util;

import lombok.extern.slf4j.Slf4j;
import vn.com.twendie.avis.api.core.constraint.IgnoreTrimReflect;

import java.lang.reflect.Field;
import java.text.Normalizer;

@Slf4j
public class ValidObjectUtil {

    public static Object trimReflectAndNormalizeString(Object object) {
        Class<?> c = object.getClass();
        try {
            for (Field field : c.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value instanceof String) {
                    String str = (String) value;
                    if (field.isAnnotationPresent(IgnoreTrimReflect.class)) {
                        field.set(object, Normalizer.normalize(str, Normalizer.Form.NFC));
                    } else {
                        field.set(object, Normalizer.normalize(str.trim(), Normalizer.Form.NFC));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot trim string because ", e);
            return object;
        }

        return object;
    }
}
