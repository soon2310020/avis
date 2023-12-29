package com.stg.service3rd.common.utils;

import com.stg.config.jackson.Jackson;
import com.stg.service3rd.common.dto.ExceptionDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.Nullable;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogUtil {

    public static String stacksToJsonString(Exception ex) {
        return Jackson.get().toJson(new ExceptionDto(ex));
    }

    public static String stacksToString(Exception ex) {
        return new ExceptionDto(ex).toString();
    }

    public static String arrayToString(Object[] a) {
        if (a == null) return "null";

        int iMax = a.length - 1;
        if (iMax == -1) return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax) return b.append(']').toString();

            b.append(",\n");
        }
    }


    /***/
    public static String errorDescToString(@Nullable List<?> errorDesc) {
        return StringUtils.join(errorDesc, ", ");
    }

}
