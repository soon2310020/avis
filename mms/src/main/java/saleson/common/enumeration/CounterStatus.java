package saleson.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CounterStatus {
    INSTALLED("Installed"),
    NOT_INSTALLED("Not Installed"),
    DETACHED("Detached");

    private final String title;
}
