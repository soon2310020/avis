package saleson.common.enumeration;

public enum ChartDataConfigType
{
    PART_QUANTITY(ChartType.LINE, "Total Part Quantity"),
    SHOT(ChartType.LINE, "Shot"),
    CYCLE_TIME(ChartType.BAR, "Cycle Time (Within, L1, L2)"),
    UPTIME(ChartType.BAR, "Uptime (Within, L1, L2)"),
    TEMPERATURE(ChartType.LINE, "Temperature"),
    ACCELERATION(ChartType.BUBBLE, "Acceleration");


    private ChartType defaultType;
    private String name;

    ChartDataConfigType(ChartType defaultType, String name)
    {
        this.defaultType = defaultType;
        this.name = name;
    }

    public ChartType getDefaultType()
    {
        return defaultType;
    }

    public String getName()
    {
        return name;
    }
}
