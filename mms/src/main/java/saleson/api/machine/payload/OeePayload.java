package saleson.api.machine.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import saleson.common.util.StringUtils;
import saleson.model.QMachine;
import saleson.model.QMachineOee;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OeePayload {
    private String start;
    private String end;
    private List<Long> locationIds;
    private List<Long> machineIds;
    private List<Long> shiftNumbers;
    private String queryMobile;
    private String colorCodeConfig;

    public Predicate getMachineOeePredicate() {
        BooleanBuilder builder = new BooleanBuilder();
        QMachineOee machineOee = QMachineOee.machineOee;

        if (CollectionUtils.isNotEmpty(locationIds)) {
            builder.and(machineOee.machine.locationId.in(locationIds));
        }
        if ((CollectionUtils.isNotEmpty(machineIds))) {
            builder.and(machineOee.machine.id.in(machineIds));
        }
        if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end))
            builder.and(machineOee.day.between(start, end));

        return builder;
    }

    public Predicate getMachinePredicate() {
        BooleanBuilder builder = new BooleanBuilder();
        QMachine machine = QMachine.machine;

        if (CollectionUtils.isNotEmpty(locationIds)) {
            builder.and(machine.locationId.in(locationIds));
        }
        if ((CollectionUtils.isNotEmpty(machineIds))) {
            builder.and(machine.id.in(machineIds));
        }

        return builder;
    }
}
