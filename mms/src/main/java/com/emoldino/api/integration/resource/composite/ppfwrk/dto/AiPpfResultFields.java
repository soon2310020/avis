package com.emoldino.api.integration.resource.composite.ppfwrk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class AiPpfResultFields {
    private EndOfLifeCycleData endOfLifeCycle;
    private CapacityPlanningData capacityPlanning;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class EndOfLifeCycleData {
        private float hourlyProductionRate;
        private float downtimeRate;
        private String predictedEndOfLife;
        private int remainingDays;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class CapacityPlanningData {
        private float hourlyProductionRateShortBreaks;
        private float weeklyEstimatedCapacity;
    }
}
