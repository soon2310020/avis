package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPartDetailsData {
    private Long partId;
    private String partCode;
    private Integer moldCount;
    private List<String> moldCode;
}
