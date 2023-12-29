package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum NotificationType implements CodeMapperType {
    SYSTEM_NOTE("System Note"),
    INVITE_USER("Invite User"),
    WORK_ORDER("Work Order"),
    DATA_REQUEST("Data Request"),
    ALERT("Alert"),
    WORK_ORDER_COMPLETE_REQUEST_APPROVAL("Work Order Complete Request Approval"),
    WORK_ORDER_APPROVAL_APPROVED("Work Order Approval Approved"),
    WORK_ORDER_APPROVAL_REJECTED("Work Order Approval Rejected"),
    WORK_ORDER_APPROVAL_CANCELLED("Work Order Approval Cancelled"),
    WORK_ORDER_CM_REQUEST_APPROVAL("Work Order CM Request Approval"),
    WORK_ORDER_CM_REQUEST_APPROVAL_APPROVED("Work Order CM Request Approval Approved"),
    WORK_ORDER_CM_REQUEST_APPROVAL_DECLINED("Work Order CM Request Approval Declined"),
    WORK_ORDER_REQUEST_CHANGE("Work Order Request Change"),
    DATA_COMPLETION("Data Completion"),

    WORK_ORDER_DECLINED("Work Order Declined"),
    WORK_ORDER_APPROVED_CHANGE("Work Order Approved"),
    WORK_ORDER_REJECTED_CHANGE("Work Order Rejected"),
    WORK_ORDER_CANCELLED("Work Order Cancelled"),

    DATA_REQUEST_ASSIGN("Data request assign"),
    DATA_COMPLETION_ASSIGN("Data completion assign"),
    DATA_REQUEST_3DAY_BEFORE_DUE_DATE("3 days before the deadline"),
    DATA_REQUEST_OVERDUE_REQUESTEE("data request overdue"),
    DATA_REQUEST_CANCEL("data request cancel"),
    DATA_COMPLETION_CANCEL("data completion cancel"),
    DATA_REQUEST_DECLINE("data request decline"),
    DATA_REQUEST_COMPLETED("data request complete"),
    DATA_REQUEST_OVERDUE_REQUESTER("data request overdue requester"),
    DATA_REQUEST_REOPEN("data request reopen"),
    DATA_COMPLETION_REOPEN("data completion reopen"),
    USER_ACCESS_REQUESTED("user access requested"),
    ;

    private String title;

    NotificationType(String title) {
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
