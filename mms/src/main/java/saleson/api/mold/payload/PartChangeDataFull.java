package saleson.api.mold.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartChangeDataFull {
    private Integer numberOfPart;
    private String singlePartName;
    private List<PartChangeData> list;
}
