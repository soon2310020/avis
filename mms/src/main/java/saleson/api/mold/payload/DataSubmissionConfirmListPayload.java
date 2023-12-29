package saleson.api.mold.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.DataSubmissionAction;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataSubmissionConfirmListPayload {
    private List<Long> moldIds;
    private DataSubmissionAction action;
    private String message;
}
