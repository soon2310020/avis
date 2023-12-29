package saleson.service.transfer.oee;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.emoldino.framework.util.ValueUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.SyncCtrlUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertRepository;
import saleson.api.machineDowntimeAlert.MachineDowntimeReasonRepository;
import saleson.api.mold.MoldRepository;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.model.Cdata;
import saleson.model.Machine;
import saleson.model.MachineDowntimeAlert;
import saleson.model.MachineDowntimeReason;
import saleson.model.Mold;
import saleson.model.Statistics;

@Service
@Slf4j
public class MachineDowntimeAlertServiceOrigin implements MachineDowntimeAlertService {
	@Autowired
	MoldRepository moldRepository;

	@Autowired
	MachineDowntimeAlertRepository machineDowntimeAlertRepository;

	@Autowired
	MachineDowntimeReasonRepository machineDowntimeReasonRepository;

	public void proc(Statistics statistics) {
		LogicUtils.assertNotNull(statistics, "statistics");

		if (statistics.getMoldId() == null) {
			return;
		}

		// Use TFF instead of LST
		String time = ValueUtils.toString(statistics.getTff(), statistics.getLst());
		if (ObjectUtils.isEmpty(time) || time.length() < 10) {
			return;
		}

		try {
			Machine machine = OeeUtils.getMachineByMoldId(statistics.getMoldId());
			if (machine == null) {
				return;
			}

			SyncCtrlUtils.wrapWithLock(
					// Lock Name
					"machine-downtime-alert::id." + machine.getId(),
					// New DB Transaction
					true,
					// Logic
					() -> {
						Mold mold = moldRepository.getOne(statistics.getMoldId());
						Double idealShotCount = 3600 / (mold.getWeightedAverageCycleTime() == null || mold.getWeightedAverageCycleTime() == 0 ? (mold.getContractedCycleTime() / 10)
								: (mold.getWeightedAverageCycleTime() / 10));
						Double standardShotCount = idealShotCount / 4;
						Optional<MachineDowntimeAlert> optionalAlert = machineDowntimeAlertRepository.findByMachineIdAndDowntimeStatusIn(machine.getId(),
								Arrays.asList(MachineDowntimeAlertStatus.DOWNTIME, MachineDowntimeAlertStatus.REGISTERED));

						if (!optionalAlert.isPresent()) {
							/**
							 * if no downtime existed and shot count < standard shot count
							 * create an alert
							 */
							if (statistics.getShotCount() < standardShotCount) {
								Instant timeInstant = DateUtils2.toInstant(time, DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMold(mold));
								MachineDowntimeAlert newAlert = MachineDowntimeAlert.builder()//
										.machine(machine)//
										.machineId(machine.getId())//
										.moldId(statistics.getMoldId())
										.startTime(timeInstant)//
										.downtimeType(MachineAvailabilityType.UNPLANNED_DOWNTIME)//
										.downtimeStatus(MachineDowntimeAlertStatus.DOWNTIME)//
										.latest(true)//
										.build();
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
								Instant timeInstant = DateUtils2.toInstant(time, DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMold(mold));
								if (timeInstant.compareTo(existedAlert.getStartTime()) > 0) {
									existedAlert.setEndTime(timeInstant);
									existedAlert.setDowntimeStatus(MachineDowntimeAlertStatus.UNCONFIRMED);
									existedAlert.setLatest(true);
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
					});
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "MACHINE_DOWNTIME_FAIL", HttpStatus.EXPECTATION_FAILED, "Unexpected Error Happend moldId:" + statistics.getMoldId(), e);
		}
	}

}
