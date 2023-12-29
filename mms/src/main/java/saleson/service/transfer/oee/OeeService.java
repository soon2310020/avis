package saleson.service.transfer.oee;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.machine.MachineOeeRepository;
import saleson.api.machine.MachineStatisticsService;
import saleson.api.machineDowntimeAlert.MachineDowntimeAlertRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.rejectedPart.ProducedPartRepository;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.rejectedPartRate.ProducedPart;

@Service
@Slf4j
public class OeeService {
	@Autowired
	MachineOeeRepository machineOeeRepository;

	@Autowired
	private MachineStatisticsService machineStatisticsService;

	@Autowired
	private ProducedPartRepository producedPartRepository;

	public void proc(Statistics statistics, int activeCavities) {
		if (statistics.getMoldId() == null || statistics.getShotCount() == null) {
			return;
		}

		try {
			Machine machine = OeeUtils.getMachineByMoldId(statistics.getMoldId());
			if (machine == null) {
				return;
			}

			// Use TFF instead of RT
			String time = ValueUtils.toString(statistics.getTff(), statistics.getRt());
			if (ObjectUtils.isEmpty(time) || time.length() < 10) {
				return;
			}

			Mold mold = BeanUtils.get(MoldRepository.class).getOne(statistics.getMoldId());
			boolean timeAfterMatchTime = OeeUtils.checkTimeAfterMatchTime(mold, time);
			if (!timeAfterMatchTime) return;

			String day = time.substring(0, 8);
			String hour = time.substring(0, 10);
			String tenMinute = time.substring(0, 11);
			int tenMinNum = Integer.parseInt(time.substring(10,11));

			SyncCtrlUtils.wrapWithLock(
					// Lock Name
					"machine-oee::id." + machine.getId() + "." + hour,
					// New DB Transaction
					true,
					// Logic
					() -> {
						List<MachineOee> saveList = new ArrayList<>();
						//Calculate ten-minute data
//						MachineOee tenMinuteOee = MachineOee.builder()
//								.machine(machine)//
//								.machineId(machine.getId())//
//								.periodType(Frequent.TEN_MINUTE)//
//								.day(day)//
//								.hour(hour)//
//								.tenMinute(tenMinute)//
//								.partProduced(statistics.getShotCount() != null ? (long) statistics.getShotCount() * activeCavities : 0)//
//								.partProducedVal(statistics.getShotCountVal() != null ? (long) statistics.getShotCountVal() * activeCavities : 0)//
//								.build();
//						Instant tenMinuteStart = DateUtils2.toInstant(tenMinute + "0", DateUtils2.DatePattern.yyyyMMddHHmm, LocationUtils.getZoneIdByMoldId(statistics.getMoldId()));
//						Instant tenMinuteEnd = tenMinuteStart.plus(10, ChronoUnit.MINUTES);
//						Double tenMinuteDowntime = machineStatisticsService.getMachineDowntimeValue(machine, tenMinuteStart, tenMinuteEnd);
//						Double tenMinuteFa = ((1/6D) - tenMinuteDowntime) * 100 / (1/6D);
//						tenMinuteOee.setFa(tenMinuteFa > 100 ? 100 : (tenMinuteFa < 0 ? 0 : tenMinuteFa));
//
////						Long tenMinuteAvailableTime = 600L; //10 minutes to seconds
						int act = mold.getContractedCycleTime() == null ? mold.getToolmakerContractedCycleTime() : mold.getContractedCycleTime();
//						Double tenMinuteWorkingTime = ((1/6D) - tenMinuteDowntime) * 3600; //working hour to seconds
//						Double tenMinuteFp = ((statistics.getShotCount() * activeCavities) / ((tenMinuteWorkingTime / (act * 0.1)) * activeCavities)) * 100;
//						tenMinuteOee.setFp(tenMinuteFp < 0 ? 0 : tenMinuteFp);
//
//						tenMinuteOee.setFq(100D);
//						saveList.add(tenMinuteOee);
//						machineOeeRepository.save(tenMinuteOee);

						// Calculate hourly data
						MachineOee machineOee;
						Optional<List<MachineOee>> optional = machineOeeRepository.findByMachineAndHourAndPeriodType(machine, hour, Frequent.HOURLY);
						if (optional.isPresent()) {
							machineOee = optional.get().get(0);
							machineOee.setPartProduced(machineOee.getPartProduced() + (statistics.getShotCount() != null ? (long) statistics.getShotCount() * activeCavities : 0));
							machineOee.setPartProducedVal(
									machineOee.getPartProducedVal() + (statistics.getShotCountVal() != null ? (long) statistics.getShotCountVal() * activeCavities : 0));
							machineOee.setTenMinute(time);
						} else {
							machineOee = MachineOee.builder()//
									.machine(machine)//
									.machineId(machine.getId())//
									.periodType(Frequent.HOURLY)//
									.moldId(statistics.getMoldId())//
									.day(day)//
									.hour(hour)//
									.tenMinute(time)
									.partProduced(statistics.getShotCount() != null ? (long) statistics.getShotCount() * activeCavities : 0)//
									.partProducedVal(statistics.getShotCountVal() != null ? (long) statistics.getShotCountVal() * activeCavities : 0)//
									.build();

							//check previous machine oee
							Optional<MachineDowntimeAlert> machineDowntimeAlert = BeanUtils.get(MachineDowntimeAlertRepository.class).findByMachineIdAndDowntimeStatusIn(machine.getId(), Arrays.asList(MachineDowntimeAlertStatus.REGISTERED, MachineDowntimeAlertStatus.DOWNTIME));
							if (machineDowntimeAlert.isPresent() && machineDowntimeAlert.get().getCreatedAt().compareTo(Instant.now().minus(5, ChronoUnit.MINUTES)) < 0) {
								Optional<MachineOee> optionalPreviousMachineOee = machineOeeRepository.findFirstByMachineAndPeriodTypeOrderByHourDesc(machine, Frequent.HOURLY);
								if (optionalPreviousMachineOee.isPresent()) {
									MachineOee previousMachineOee = optionalPreviousMachineOee.get();
									String lastMinute = previousMachineOee.getTenMinute();
									if (!StringUtils.isEmpty(lastMinute)) {
										Integer min = Integer.parseInt(lastMinute.substring(10,12));
										Integer sec = Integer.parseInt(lastMinute.substring(12,14));
										Integer remainingSecondsOfHour = 3600 - (min * 60 + sec);

										previousMachineOee.setDowntimeDuration((previousMachineOee.getDowntimeDuration() == null ? 0 : previousMachineOee.getDowntimeDuration()) + remainingSecondsOfHour);
										machineOeeRepository.save(previousMachineOee);
									}
								}
							}
						}
						Instant start = DateUtils2.toInstant(hour, DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByMoldId(statistics.getMoldId()));
						Instant end = DateUtils2.toInstant(time, DateUtils2.DatePattern.yyyyMMddHHmmss, LocationUtils.getZoneIdByMoldId(statistics.getMoldId()));
						if (end.compareTo(Instant.now()) > 0) {
							end = Instant.now();
						}
						Double downtime = machineStatisticsService.getMachineDowntimeValue(machine, start, end);
						machineOee.setDowntimeDuration((int)(downtime * 3600));
						Double plannedDowntime = machineStatisticsService.getMachineDowntimeValueByType(machine, start, end, MachineAvailabilityType.PLANNED_DOWNTIME);
						Double unPlannedDowntime = machineStatisticsService.getMachineDowntimeValueByType(machine, start, end, MachineAvailabilityType.UNPLANNED_DOWNTIME);
						Double currentWorkingHour = (double) Duration.between(start, end).getSeconds()/3600;
						Double fa;
						if ((currentWorkingHour - plannedDowntime) > 0) {
							fa = ((currentWorkingHour - plannedDowntime - unPlannedDowntime) / (currentWorkingHour - plannedDowntime)) * 100;
						} else fa = 0D;
						machineOee.setFa(fa > 100 ? 100 : (fa < 0 ? 0 : fa));

						ProducedPart producedPart = producedPartRepository.findFirstByMoldIdAndFrequentAndHour(statistics.getMoldId(), Frequent.HOURLY, hour);
						Integer newTotalAmount = statistics.getShotCount() * activeCavities;
						if (producedPart != null) {
							newTotalAmount = newTotalAmount + producedPart.getTotalProducedAmount();
							if (newTotalAmount > 0) {
								machineOee.setFq(100 - (producedPart.getTotalRejectedAmount() * 100.0 / newTotalAmount));
							} else {
								machineOee.setFq(0D);
							}
						} else {
							if (newTotalAmount > 0) {
								machineOee.setFq(100D);
							} else {
								machineOee.setFq(0D);
							}
						}

						if (newTotalAmount > 0) {
							Double fp = (newTotalAmount / (((currentWorkingHour * 3600) / (act * 0.1)) * activeCavities)) * 100;
							machineOee.setFp(fp < 0 ? 0 : fp);
						} else {
							machineOee.setFp(0D);
						}

						saveList.add(machineOee);
						machineOeeRepository.saveAll(saveList);
					});
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			LogUtils.saveErrorQuietly(e);
		}
	}
}
