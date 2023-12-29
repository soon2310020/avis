package saleson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Item {
    private String code;
    private String name;
    private String value;

    public Item(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Item(String code, String name, String value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }
}
