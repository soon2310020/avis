package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum ActionTypeEnum {

    CONTRACT(1L, "CONTRACT", "/contracts"),
    CUSTOMER(2L, "CUSTOMER", new String[]{"/customers", "/member-customers"}),
    DIARY(3L, "DIARY", "/journey_diary_dailies"),
    APP_DIARY(4L, "APP_DIARY", "/journey-diary"),
    VEHICLE(5L, "VEHICLE", "/vehicles"),
    DRIVER(6L, "DRIVER", "/drivers"),
    USER(7L, "USER", "/users"),
    ;

    private final Long id;

    private final String code;

    private final String[] patterns;

    ActionTypeEnum(Long id, String code, String pattern) {
        this(id, code, new String[]{pattern});
    }

    public static ActionTypeEnum getActionTypeFromUri(String uri) {
        if (Objects.nonNull(uri)) {
            for (ActionTypeEnum actionType : values()) {
                for (String pattern : actionType.getPatterns()) {
                    if (uri.startsWith(pattern)) {
                        return actionType;
                    }
                }
            }
        }
        return null;
    }

}
