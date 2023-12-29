package saleson.api.machine;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.api.machine.payload.DetailOEE;


public interface MachineStatisticsRepositoryCustom {
    Page<DetailOEE> findMachineStatisticsForOEE(String start, String end, String line, Long companyId, Pageable pageable, boolean isAll);

}
