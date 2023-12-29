package saleson.api.accessHierachy.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessMoldPayload {
    private Long companyId;
    private List<Long> moldIdList;

}
