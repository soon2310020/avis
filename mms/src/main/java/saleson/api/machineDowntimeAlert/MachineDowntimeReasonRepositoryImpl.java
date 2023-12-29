package saleson.api.machineDowntimeAlert;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.MachineDowntimeReason;

public class MachineDowntimeReasonRepositoryImpl  extends QuerydslRepositorySupport implements MachineDowntimeReasonRepositoryCustom{
    public MachineDowntimeReasonRepositoryImpl() {
        super(MachineDowntimeReason.class);
    }
}
