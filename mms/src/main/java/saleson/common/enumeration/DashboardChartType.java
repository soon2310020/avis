package saleson.common.enumeration;

public enum DashboardChartType {
    OVERALL_TOOLING_EFFECTIVENESS(true),
    DOWNTIME(true),
    OVERALL_EQUIPMENT_EFFECTIVENESS(true),
    PRODUCTION_QUANTITY(true),
    PRODUCTION_PATTERN_ANALYSIS(true),
    ON_TIME_DELIVERY(true),
    ACTUAL_TARGET_UPTIME_RATIO(true),
    CYCLE_TIME(true),
    MAXIMUM_CAPACITY(true),
    DATA_COMPLETION_RATE(true),
    OEE_CENTER(false),

    PART_PRODUCE(false),
    TOTAL_PARTS(false),
    TOTAL_TOOLING(false),
    INACTIVE_TOOLING(false),
    TOTAL_COST(false),
    DIGITALIZATION_RATE(false),
    CT_DEVIATION(false),
    MAINTENANCE(false),
    END_OF_LIFE_CYCLE(false),
    PRODUCTION_CAPACITY(false),
    PART_PRODUCED_COLOR_CODE_CONFIG(false);

    private Boolean isDashboardChart;

    DashboardChartType(Boolean isDashboardChart) {
        this.isDashboardChart = isDashboardChart;
    }

    public Boolean getDashboardChart() {
        return isDashboardChart;
    }
}
