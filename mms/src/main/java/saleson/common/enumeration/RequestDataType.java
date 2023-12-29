package saleson.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestDataType {
    DATA_REGISTRATION("Data Registration"),
    DATA_COMPLETION("Data completion");

    private final String title;

}
