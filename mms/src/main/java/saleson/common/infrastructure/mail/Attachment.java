package saleson.common.infrastructure.mail;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    private byte[] data;
    private String name;

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }
}
