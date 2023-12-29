package saleson.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DataCompletionRequestStatus {
    OVERDUE("Overdue"),
    IN_PROGRESS("In Progress"),
    REQUESTED("Requested"),
    COMPLETED("Completed"),
    DECLINED("Declined"),
    CANCELLED("Cancelled");

    private final String title;

}
