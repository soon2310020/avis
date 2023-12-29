package saleson.api.dataRequest.payload;

import lombok.Data;
import saleson.common.enumeration.DataCompletionRequestStatus;

@Data
public class UpdateStatusDTO {
    private Long id;
    private DataCompletionRequestStatus dataRequestStatus;
    private String reason;
}
