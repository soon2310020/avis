package saleson.service.transfer.oee;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.LogicUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.machine.MachineMoldMatchingHistoryRepository;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertRepository;
import saleson.api.machineDowntimeAlert.MachineDowntimeReasonRepository;
import saleson.api.mold.MoldRepository;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.model.Cdata;
import saleson.model.Machine;
import saleson.model.MachineDowntimeAlert;
import saleson.model.MachineDowntimeReason;
import saleson.model.MachineMoldMatchingHistory;
import saleson.model.Mold;
import saleson.model.Statistics;

@Service
@Slf4j
public class MachineDowntimeAlertServiceOld implements MachineDowntimeAlertService {
	@Autowired
	MoldRepository moldRepository;

	@Autowired
	MachineDowntimeAlertRepository machineDowntimeAlertRepository;

	@Autowired
	MachineDowntimeReasonRepository machineDowntimeReasonRepository;

	@Autowired
	private MachineMoldMatchingHistoryRepository machineMoldMatchingHistoryRepository;

	public void proc(Statistics statistics) {
		LogicUtils.assertNotNull(statistics, "statistics");

		if (statistics.getMoldId() == null) {
			return;
		}

//		String cdataLst = cdata.getLst();
//		String cdataTff = cdata.getTff();
		try {
			if (statistics.getMoldId() != null) {
				Mold mold = moldRepository.getOne(statistics.getMoldId());
				Double idealShotCount = 3600d / (mold.getWeightedAverageCycleTime() == null || mold.getWeightedAverageCycleTime() == 0d ? (mold.getContractedCycleTime() / 10d)
						: (mold.getWeightedAverageCycleTime() / 10d));
				Double standardShotCount = idealShotCount / 4;

				Optional<MachineMoldMatchingHistory> optional = machineMoldMatchingHistoryRepository
						.findFirstByMoldAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(mold);
				if (optional.isPresent()) {
					MachineMoldMatchingHistory history = optional.get();
					Machine machine = history.getMachine();
					Optional<MachineDowntimeAlert> optionalAlert = machineDowntimeAlertRepository.findByMachineIdAndDowntimeStatusIn(machine.getId(),
							Arrays.asList(MachineDowntimeAlertStatus.DOWNTIME, MachineDowntimeAlertStatus.REGISTERED));

					if (!optionalAlert.isPresent()) {
						/**
						 * if no downtime existed and shot count < standard shot count
						 * create an alert
						 */
						if (statistics.getShotCount() < standardShotCount) {
							MachineDowntimeAlert newAlert = MachineDowntimeAlert.builder()
									.machine(machine)
									.machineId(machine.getId())
									.moldId(statistics.getMoldId())
									.startTime(Instant.now())
									.downtimeType(MachineAvailabilityType.UNPLANNED_DOWNTIME).downtimeStatus(MachineDowntimeAlertStatus.DOWNTIME).build();
							machineDowntimeAlertRepository.save(newAlert);
						}
					} else {
						/**
						 * if downtime existed and shot count >= standard shot count
						 * - set end time and change status for alert
						 * - set end time for reason if existed
						 */
						MachineDowntimeAlert existedAlert = optionalAlert.get();
						if (statistics.getShotCount() >= standardShotCount) {
							existedAlert.setEndTime(Instant.now());
							existedAlert.setDowntimeStatus(MachineDowntimeAlertStatus.UNCONFIRMED);
							machineDowntimeAlertRepository.save(existedAlert);
							List<MachineDowntimeReason> reasons = machineDowntimeReasonRepository.findByMachineDowntimeAlertId(existedAlert.getId());
							if (CollectionUtils.isNotEmpty(reasons)) {
								reasons.get(0).setStartTime(existedAlert.getStartTime());
								reasons.get(0).setEndTime(existedAlert.getEndTime());
								machineDowntimeReasonRepository.save(reasons.get(0));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "MACHINE_DOWNTIME_FAIL", HttpStatus.EXPECTATION_FAILED, "Unexpected Error Happend moldId:" + statistics.getMoldId(), e);
		}
	}

}
