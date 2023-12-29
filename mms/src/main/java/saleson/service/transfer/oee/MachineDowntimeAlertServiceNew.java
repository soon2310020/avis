package saleson.service.transfer.oee;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertRepository;
import saleson.api.machineDowntimeAlert.MachineDowntimeReasonRepository;
import saleson.api.mold.MoldRepository;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.common.util.DateUtils;
import saleson.model.*;

@Service
@Slf4j
public class MachineDowntimeAlertServiceNew implements MachineDowntimeAlertService {

	@Autowired
	MachineDowntimeAlertRepository machineDowntimeAlertRepository;

	@Autowired
	MoldRepository moldRepository;

	@Autowired
	MachineDowntimeReasonRepository machineDowntimeReasonRepository;

	public void proc_old(Statistics statistics, Cdata cdata) {
		LogicUtils.assertNotNull(statistics, "statistics");
		LogicUtils.assertNotNull(cdata, "cdata");

		if (statistics.getMoldId() == null) {
			return;
		}

		Mold mold = BeanUtils.get(MoldRepository.class).getOne(statistics.getMoldId());
		String cdataLst = cdata.getLst();
		String cdataTff = cdata.getTff();

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
						Optional<MachineDowntimeAlert> optional = machineDowntimeAlertRepository.findByMachineIdAndDowntimeStatusIn(machine.getId(),
								Arrays.asList(MachineDowntimeAlertStatus.DOWNTIME, MachineDowntimeAlertStatus.REGISTERED));
						Instant lst = DateUtils.getInstant(cdataLst, DateUtils.DEFAULT_DATE_FORMAT);
						Instant tff = DateUtils.getInstant(cdataTff, DateUtils.DEFAULT_DATE_FORMAT);
						Duration res = Duration.between(lst, tff);
						if (!optional.isPresent()) {
							Instant startTime = DateUtils2.toInstant(cdataLst, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMold(mold)).plus(5, ChronoUnit.MINUTES);

							/**
							 * Duplicated downtime of this machine by start time
							 */
							MachineDowntimeAlert oldAlert = machineDowntimeAlertRepository.findFirstByMachineIdAndStartTimeOrderByCreatedAtDesc(machine.getId(), startTime)
									.orElse(null);
							if (oldAlert != null) {
								return;
							}

							/**
							 * if no downtime existed and tff - lst >= 20 minutes
							 * create an alert
							 */
							if (res.abs().toMinutes() >= 20 && tff.isAfter(lst)) {
								MachineDowntimeAlert newAlert = MachineDowntimeAlert.builder()//
										.machine(machine)//
										.machineId(machine.getId())//
										.startTime(DateUtils.getInstant(cdataLst, DateUtils.DEFAULT_DATE_FORMAT).plus(5, ChronoUnit.MINUTES))//
										.downtimeType(MachineAvailabilityType.UNPLANNED_DOWNTIME)//
										.downtimeStatus(MachineDowntimeAlertStatus.DOWNTIME)//
										.latest(true)//
										.build();
								machineDowntimeAlertRepository.saveAndFlush(newAlert);
							}
						} else {
							/**
							 * if downtime existed and shot tff - lst < 10 minutes
							 * - set end time and change status for alert
							 * - set end time for reason if existed
							 */
							MachineDowntimeAlert existedAlert = optional.get();
							if (res.abs().toMinutes() < 10 && tff.isAfter(lst)) {
								existedAlert.setEndTime(DateUtils2.toInstant(cdataLst, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMold(mold)).minus(15, ChronoUnit.MINUTES));
								existedAlert.setDowntimeStatus(MachineDowntimeAlertStatus.UNCONFIRMED);
								existedAlert.setLatest(true);
								machineDowntimeAlertRepository.saveAndFlush(existedAlert);
								List<MachineDowntimeReason> reasons = machineDowntimeReasonRepository.findByMachineDowntimeAlertId(existedAlert.getId());
								if (CollectionUtils.isNotEmpty(reasons)) {
									reasons.get(0).setStartTime(existedAlert.getStartTime());
									reasons.get(0).setEndTime(existedAlert.getEndTime());
									machineDowntimeReasonRepository.save(reasons.get(0));
								}
							}
						}
					});
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "MACHINE_DOWNTIME_FAIL", HttpStatus.EXPECTATION_FAILED, "Unexpected Error Happened moldId:" + statistics.getMoldId(), e);
		}
	}

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

		Instant timeInstant = DateUtils2.toInstant(time, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMoldId(statistics.getMoldId()));

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
						Optional<MachineDowntimeAlert> optional = machineDowntimeAlertRepository.findByMachineIdAndDowntimeStatusIn(machine.getId(),
								Arrays.asList(MachineDowntimeAlertStatus.DOWNTIME, MachineDowntimeAlertStatus.REGISTERED));
						if (!optional.isPresent()) {
							MachineDowntimeAlert oldAlert = machineDowntimeAlertRepository.findFirstByMachineIdAndStartTimeOrderByCreatedAtDesc(machine.getId(), timeInstant)
									.orElse(null);
							if (oldAlert != null) {
								return;
							}

							if (statistics.getShotCount() == null || statistics.getShotCount() <= 0) {
								MachineDowntimeAlert newAlert = MachineDowntimeAlert.builder()
										.machine(machine)
										.machineId(machine.getId())
										.moldId(statistics.getMoldId())
										.startTime(timeInstant)
										.downtimeType(MachineAvailabilityType.UNPLANNED_DOWNTIME)
										.downtimeStatus(MachineDowntimeAlertStatus.DOWNTIME)
										.latest(true)
										.build();
								machineDowntimeAlertRepository.saveAndFlush(newAlert);
							}
						} else {
							MachineDowntimeAlert existedAlert = optional.get();
							if (statistics.getShotCount() != null && statistics.getShotCount() > 0) {
								if (timeInstant.compareTo(existedAlert.getStartTime()) > 0) {
									existedAlert.setEndTime(timeInstant);
									existedAlert.setDowntimeStatus(MachineDowntimeAlertStatus.UNCONFIRMED);
									existedAlert.setLatest(true);
									machineDowntimeAlertRepository.saveAndFlush(existedAlert);
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
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "MACHINE_DOWNTIME_FAIL", HttpStatus.EXPECTATION_FAILED, "Unexpected Error Happened moldId:" + statistics.getMoldId(), e);
		}
	}

}
