package saleson.model.data.errorData;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErrorIndexData {
    private Integer index;
    private List<String> data;
}
