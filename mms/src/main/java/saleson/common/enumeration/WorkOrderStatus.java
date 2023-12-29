package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum WorkOrderStatus implements CodeMapperType {

    ACCEPTED("Accepted"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue"),
    REQUESTED("Requested"),
    DECLINED("Declined"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    PENDING_APPROVAL("Pending Approval"),
    REQUESTED_NOT_FINISHED("Requested Not Finished"),
    APPROVAL_REQUESTED("Approval Requested"),
    CHANGE_REQUESTED("Change requested")
    ;


    private String title;

    WorkOrderStatus(String title) {
        this.title = title;
    }


    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return "";
    }
    @Override
    public Boolean isEnabled() {
        return true;
    }
}
