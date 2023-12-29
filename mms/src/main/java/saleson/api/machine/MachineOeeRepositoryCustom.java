package saleson.api.machine;

import saleson.api.machine.payload.AvgOEE;
import saleson.api.machine.payload.OeeByShift;
import saleson.api.machine.payload.OeePayload;
import saleson.common.enumeration.Frequent;
import saleson.model.Machine;

import java.util.List;

public interface MachineOeeRepositoryCustom {
    AvgOEE findOeeData(OeePayload payload);
    AvgOEE findOeeDataByHour(OeePayload payload);
    List<OeeByShift> findAllOeeDataByHour(OeePayload payload);

    String findFirstHourByMachineIdAndPeriodTypeOrderByTenMinuteDesc(Long machineId, Frequent periodType);
    String findFirstHourByMachineIdAndPeriodTypeOrderByHourDesc(Long machineId);
    String findFirstTimeByMachineIdAndPeriodTypeOrderByHourDesc(Long machineId);
}
