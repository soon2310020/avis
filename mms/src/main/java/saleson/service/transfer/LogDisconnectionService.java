package saleson.service.transfer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.counter.CounterService;
import saleson.api.mold.MoldService;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.Event;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.util.SecurityUtils;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.Terminal;
import saleson.model.data.LogDisconnectionData;
import saleson.model.logs.LogDisconnection;
import saleson.model.support.Equipment;

@Service
public class LogDisconnectionService {
	@Autowired
	private LogDisconnectionRepository logDisconnectionRepository;
	@Autowired
	private CounterService counterService;

	@Lazy
	@Autowired
	private MoldService moldService;
	@Autowired
	private GeneralConfigService generalConfigService;

	public List<LogDisconnection> findByEquipmentIdAndStatus(EquipmentType type, Long id, NotificationStatus status) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		List<LogDisconnection> logDisconnectList = logDisconnectionRepository.findByEquipmentIdAndEquipmentType(id, type, sort);
		List<LogDisconnection> finalLogEfficiencyList;
		if (status != null) {
			finalLogEfficiencyList = logDisconnectList.stream().filter(x -> {
				if (x.getNotificationStatus() == null || !x.getNotificationStatus().equals(status))
					return false;
				return true;
			}).collect(Collectors.toList());
		} else
			finalLogEfficiencyList = logDisconnectList;
		return finalLogEfficiencyList;
	}

	// Directly saving a log
	public void save(LogDisconnection logDisconnection) {
		logDisconnectionRepository.save(logDisconnection);
	}

	// Saving log with equipment and event
	public void save(Long alertId, Equipment equipment, Event event) {
		LogDisconnection logDisconnection = new LogDisconnection();
		logDisconnection.setAlertId(alertId);
		logDisconnection.setEvent(event);

		List<LogDisconnection> logDisconnectionList;
		if (equipment instanceof Counter) {
			Counter counter = (Counter) equipment;
			logDisconnection.setEquipmentType(EquipmentType.COUNTER);
			logDisconnection.setEquipmentId(counter.getId());
			logDisconnection.setShots(counter.getShotCount());
			logDisconnectionList = getLog(EquipmentType.COUNTER, counter.getId());
			if (logDisconnectionList != null && logDisconnectionList.size() > 0) {
				logDisconnection.setGap(logDisconnection.getShots() - logDisconnectionList.get(0).getShots());
			}
			if (event != null && event.equals(Event.DISCONNECT)) {
				logDisconnection.setEventAt(generalConfigService.getDisconnectedChecktime());
			} else {
				logDisconnection.setEventAt(Instant.now());
			}
		} else if (equipment instanceof Mold) {
			Mold mold = (Mold) equipment;
			logDisconnection.setEquipmentId(mold.getId());
			logDisconnection.setEquipmentType(EquipmentType.MOLD);
			if (mold.getCounter() != null) {
				logDisconnection.setShots(mold.getLastShot());
			}
			logDisconnectionList = getLog(EquipmentType.MOLD, mold.getId());
			if (logDisconnectionList != null && logDisconnectionList.size() > 0 && logDisconnection.getShots() != null && logDisconnectionList.get(0).getShots() != null) {
				logDisconnection.setGap(logDisconnection.getShots() - logDisconnectionList.get(0).getShots());
			}
			if (event != null && event.equals(Event.DISCONNECT)) {
				logDisconnection.setEventAt(generalConfigService.getDisconnectedChecktime());
			} else {
				logDisconnection.setEventAt(Instant.now());
			}
		} else if (equipment instanceof Terminal) {
			Terminal terminal = (Terminal) equipment;
			logDisconnection.setEquipmentId(terminal.getId());
			logDisconnection.setEquipmentType(EquipmentType.TERMINAL);
			if (event != null && event.equals(Event.DISCONNECT)) {
				logDisconnection.setEventAt(Instant.now().minus(11, ChronoUnit.MINUTES));
			} else {
				logDisconnection.setEventAt(Instant.now());
			}
		}
		logDisconnectionRepository.save(logDisconnection);
	}

	// Saving log by calling API
	public void save(EquipmentType equipmentType, Long equipmentId, Event event) {
		LogDisconnection logDisconnection = new LogDisconnection();
		logDisconnection.setEvent(event);
		logDisconnection.setEquipmentType(equipmentType);
		logDisconnection.setEquipmentId(equipmentId);
		List<LogDisconnection> logDisconnectionList = new ArrayList<>();
		if (equipmentType.equals(EquipmentType.COUNTER)) {
			Counter counter = counterService.findById(equipmentId);
			if (counter != null) {
				logDisconnection.setShots(counter.getShotCount());
				logDisconnectionList = getLog(EquipmentType.COUNTER, equipmentId);
			}
		} else if (equipmentType.equals(EquipmentType.MOLD)) {
			Mold mold = moldService.findById(equipmentId);
			if (mold != null && mold.getCounter() != null) {
				logDisconnection.setShots(mold.getCounter().getShotCount());
				logDisconnectionList = getLog(EquipmentType.MOLD, equipmentId);
			}
		}
		if (logDisconnectionList != null && logDisconnectionList.size() > 0) {
			logDisconnection.setGap(logDisconnection.getShots() - logDisconnectionList.get(0).getShots());
		}
		logDisconnectionRepository.save(logDisconnection);
	}

	// Get all by id and type
	public List<LogDisconnection> getLog(EquipmentType type, Long id) {
		return logDisconnectionRepository.findAllByEquipmentTypeAndEquipmentId(type.name(), id);
	}

	public void confirm(Long alertId, String message) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		List<LogDisconnection> logDisconnectionList = logDisconnectionRepository.findByAlertId(alertId, sort);

		if (logDisconnectionList != null && logDisconnectionList.size() > 0) {
			LogDisconnection logEfficiency = confirm(logDisconnectionList.get(0), message);
			logDisconnectionRepository.save(logEfficiency);
		}
	}

	public void confirm(List<Long> alertIds, String message) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		List<LogDisconnection> logDisconnectionList = logDisconnectionRepository.findByAlertIdIsIn(alertIds, sort);
		List<LogDisconnection> finalDisconnectionList = new ArrayList<>();
		if (logDisconnectionList == null || logDisconnectionList.size() == 0)
			return;
		logDisconnectionList.forEach(logEfficiency -> {
			LogDisconnection aLog = finalDisconnectionList.stream().filter(x -> x.getAlertId().equals(logEfficiency.getAlertId())).findAny().orElse(null);
			if (aLog == null) {
				LogDisconnection aLogDisconnection = confirm(logDisconnectionList.get(0), message);
				finalDisconnectionList.add(aLogDisconnection);
			}
		});
		logDisconnectionRepository.saveAll(finalDisconnectionList);
	}

	private LogDisconnection confirm(LogDisconnection logDisconnection, String message) {
		logDisconnection.setNotificationStatus(NotificationStatus.CONFIRMED);
		logDisconnection.setConfirmedAt(Instant.now());
		logDisconnection.setConfirmedBy(SecurityUtils.getName());
		logDisconnection.setMessage(message);
		return logDisconnection;
	}

	public Page<LogDisconnection> findLogDisconnection(Predicate predicate, Pageable pageable) {
		return logDisconnectionRepository.findAll(predicate, pageable);
	}

	public List<LogDisconnectionData> findLogDisconnectionData(DashboardFilterPayload payload) {
		return logDisconnectionRepository.findLogDisconnectionData(payload);
	}

	public List<LogDisconnection> recoverEventTime() {
		List<LogDisconnection> logDisconnections = logDisconnectionRepository.findAll();
		logDisconnections.forEach(logDisconnection -> {
			if (logDisconnection.getEquipmentType() != null && logDisconnection.getEquipmentType().equals(EquipmentType.TERMINAL)) {
				if (logDisconnection.getEvent() != null && logDisconnection.getEvent().equals(Event.RECONNECT)) {
					logDisconnection.setEventAt(logDisconnection.getCreatedAt());
				} else {
					logDisconnection.setEventAt(logDisconnection.getCreatedAt().minus(11, ChronoUnit.MINUTES));
				}
			} else {
				if (logDisconnection.getEvent() != null && logDisconnection.getEvent().equals(Event.RECONNECT)) {
					logDisconnection.setEventAt(logDisconnection.getCreatedAt());
				} else {
					logDisconnection.setEventAt(logDisconnection.getCreatedAt().minus(6, ChronoUnit.HOURS));
				}
			}
		});
		return logDisconnectionRepository.saveAll(logDisconnections);
	}
}
