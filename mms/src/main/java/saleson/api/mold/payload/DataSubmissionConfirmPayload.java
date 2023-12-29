package saleson.api.mold.payload;

import lombok.*;
import saleson.common.enumeration.DataSubmissionAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataSubmissionConfirmPayload {
    private Long moldId;
    private DataSubmissionAction action;
    private String message;
}
