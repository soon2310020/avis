package vn.com.twendie.avis.api.core.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Component
public class ObjUtils {

    public boolean isNull(Object... objects) {
        return Arrays.stream(objects).allMatch(Objects::isNull);
    }

    public boolean nonNull(Object... objects) {
        return Arrays.stream(objects).allMatch(Objects::nonNull);
    }

}
