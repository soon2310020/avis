package saleson.api.mold.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartLiteData {
    private Long id;
    private String code;
    private String name;
}
