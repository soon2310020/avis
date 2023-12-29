package saleson.api.machine.payload;

import lombok.Data;

@Data
public class CustomBoolean {
    boolean value;

    public CustomBoolean(boolean value) {
        this.value = value;
    }
}
