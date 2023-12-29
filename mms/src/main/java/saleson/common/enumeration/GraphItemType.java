package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum GraphItemType implements CodeMapperType {
    APPROVED_CYCLE_TIME("Approved CT"),
    CYCLE_TIME("Cycle Time"),
    MAX_CYCLE_TIME("Max CT"),
    MIN_CYCLE_TIME("Min CT"),
    QUANTITY("Shot"),
    UPTIME("Uptime (Within, L1, L2)"),
    UPTIME_TARGET("Uptime Target"),
    MAXIMUM_CAPACITY("Maximum Capacity");

    private String title;

    GraphItemType(String title) {
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
