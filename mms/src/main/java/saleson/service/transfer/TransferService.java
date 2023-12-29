package saleson.service.transfer;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExt;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExtRepository;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.TransferIndex;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.TransferIndexRepository;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.api.asset.resource.base.mold.enumeration.RelocationType;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.rststp.service.RstStpService;
import com.emoldino.api.common.resource.base.location.repository.area.Area;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.extern.slf4j.Slf4j;
import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldDowntimeEventRepository;
import saleson.api.mold.MoldLocationRepository;
import saleson.api.mold.MoldMisconfigureRepository;
import saleson.api.mold.MoldPartRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.part.PartProjectProducedRepository;
import saleson.api.part.PartRepository;
import saleson.api.preset.PresetRepository;
import saleson.api.preset.PresetService;
import saleson.api.rejectedPart.RejectedPartService;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.api.user.UserAlertRepository;
import saleson.api.user.UserRepository;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.BatteryStatus;
import saleson.common.enumeration.DowntimeStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.IpType;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PeriodType;
import saleson.common.enumeration.PresetStatus;
import saleson.common.notification.MailService;
import saleson.common.util.DataUtils;
import saleson.model.Adata;
import saleson.model.Category;
import saleson.model.Cdata;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.LogTransfer;
import saleson.model.LogUserAlert;
import saleson.model.Mold;
import saleson.model.MoldDowntimeEvent;
import saleson.model.MoldLocation;
import saleson.model.MoldMisconfigure;
import saleson.model.MoldPart;
import saleson.model.Part;
import saleson.model.PartProjectProduced;
import saleson.model.Preset;
import saleson.model.QStatistics;
import saleson.model.Statistics;
import saleson.model.StatisticsPart;
import saleson.model.Terminal;
import saleson.model.Transfer;
import saleson.model.User;
import saleson.model.UserAlert;
import saleson.service.transfer.maintenance.MoldMaintenanceService;
import saleson.service.transfer.oee.MachineDowntimeAlertServiceNew;
import saleson.service.transfer.oee.OeeService;

@Slf4j
@Service
public class TransferService {

	@Value("${customer.server.name}")
	private String serverName;

//	@Value("${app.transfer.machine-downtime-alert-service.class:saleson.service.transfer.oee.MachineDowntimeAlertServiceOrigin}")
//	private Class<? extends MachineDowntimeAlertService> machineDowntimeAlertService;

	private final static List<String> BS_VALID_LIST = Arrays.asList("H", "L", "M", "E");

	@Transactional
	public Page<Transfer> findAll(Predicate predicate, Pageable pageable) {
		return BeanUtils.get(TransferRepository.class).findAll(predicate, pageable);
	}

	@Transactional
	public void saveLog(List<Transfer> dataList) {
		dataList.forEach(transfer -> {
			LogTransfer log = transfer.toLogTransfer();
			BeanUtils.get(LogTransferRepository.class).save(log);
			transfer.setLi(log.getId());
		});
	}

	@Transactional
	public void save(Transfer transfer) {
		BeanUtils.get(TransferRepository.class).save(transfer);
	}

	@Transactional
	public void procTdata(List<Transfer> list) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}

		// Activate
		{
			String ti = list.get(0).getTi();
			Terminal terminal = ThreadUtils.getProp("terminalByCode." + ti, () -> BeanUtils.get(TerminalRepository.class).findByEquipmentCode(ti).orElse(null));
			if (terminal != null && terminal.getActivatedAt() == null) {
				terminal.setActivatedAt(DateUtils2.getInstant());
				BeanUtils.get(TerminalRepository.class).save(terminal);
			}
		}

		list.forEach(transfer -> {
			BeanUtils.get(TransferRepository.class).save(transfer);
			BeanUtils.get(TdataRepository.class).save(transfer.toTdata());

			String ti = transfer.getTi();
			Terminal terminal = ThreadUtils.getProp("terminalByCode." + ti, () -> BeanUtils.get(TerminalRepository.class).findByEquipmentCode(ti).orElse(null));

			if (terminal != null) {
				terminal.setOperatingStatus(OperatingStatus.WORKING);
				terminal.setOperatedAt(DateUtils2.getInstant());
				BeanUtils.get(TerminalRepository.class).save(terminal);
			} else {
				// #202. terminal is not registered. 2020-02-10
				terminal = new Terminal();
				terminal.setEquipmentCode(transfer.getTi());
				terminal.setIpAddress(transfer.getIp());
				terminal.setGateway(transfer.getGw());
				terminal.setDns(transfer.getDn());
				terminal.setEquipmentStatus(EquipmentStatus.INSTALLED);
				terminal.setOperatingStatus(OperatingStatus.WORKING);
				terminal.setOperatedAt(DateUtils2.getInstant());
				if ("1".equals(transfer.getDh())) {
					terminal.setIpType(IpType.DYNAMIC);
				} else {
					terminal.setIpType(IpType.STATIC);
				}
				terminal.setEnabled(true);

				Location location = LocationUtils.getDefault();
				terminal.setLocation(location);
				terminal.setCompanyId(location.getCompanyId());

				BeanUtils.get(TerminalRepository.class).save(terminal);
			}
		});
	}

	@Transactional
	public void procAdata(List<Transfer> transferList) {
		transferList.forEach(transfer -> {
			if (isDuplicated("ADATA", transfer)) {
				return;
			}

			Adata adata = transfer.toAdata();
			// CDATA에 MoldCode 등록
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(adata.getCi()).orElse(null);
			if (counter != null && counter.getMold() != null) {
				adata.setMoldId(counter.getMold().getId());
				adata.setMoldCode(counter.getMold().getEquipmentCode());
			}

			BeanUtils.get(TransferRepository.class).save(transfer);
			BeanUtils.get(AdataRepository.class).save(adata);
		});
	}

	public void procCdata(List<Transfer> list) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}

		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(list.get(0).getCi()).orElse(null);
		if (counter != null && counter.getActivatedAt() == null) {
			counter.setActivatedAt(Instant.now());
			BeanUtils.get(CounterRepository.class).save(counter);
		}

		list.forEach(transfer -> BeanUtils.get(TransferService.class).procCdata(transfer));
	}

	private static Set<String> PROCESSING_CI = Collections.synchronizedSet(new HashSet<>());

	@Transactional
	public void procCdata(Transfer transfer) {
		int i = 0;
		String ci = transfer.getCi();
		while (PROCESSING_CI.contains(ci)) {
			if (i++ % 100 == 0) {
				LogUtils.saveErrorQuietly(ErrorType.LOGIC, "TRS_UNCONROLLED_CI", HttpStatus.EXPECTATION_FAILED, //
						"[TRS] Uncontrolled Parallel CI Process 1!! ci:" + transfer.getCi() + " li:" + transfer.getLi());
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
			}
		}
		PROCESSING_CI.add(ci);
		try {
			_procCdata(transfer);
		} finally {
			PROCESSING_CI.remove(ci);
		}
	}

	private void _procCdata(Transfer transfer) {
		/**
		 * 1. Validation
		 */
		if (isDuplicated("CDATA", transfer)) {
			return;
		}

		if (transfer.getBs() != null && !BS_VALID_LIST.contains(transfer.getBs())) {
			LogUtils.saveErrorQuietly(ErrorType.REQ, "TRS_INVALID_BS", HttpStatus.BAD_REQUEST, //
					"[TRS] Invalid bs:" + transfer.getBs() + " ci:" + transfer.getCi() + " sn:" + transfer.getSn()//
							+ " ti:" + transfer.getTi() + " li:" + transfer.getLi());
			return;
		} else if (transfer.getSc() < 0 || transfer.getSc() > 1000000000) {
			LogUtils.saveErrorQuietly(ErrorType.REQ, "TRS_INVALID_SC", HttpStatus.BAD_REQUEST, //
					"[TRS] Invalid sc:" + transfer.getSc() + " ci:" + transfer.getCi() + " sn:" + transfer.getSn()//
							+ " ti:" + transfer.getTi() + " li:" + transfer.getLi());
			return;
		}

		/**
		 * 2. Save Transfer
		 */
		if (transfer.getId() == null) {
			BeanUtils.get(TransferRepository.class).save(transfer);
		}

		/**
		 * 3. Proc CDATA
		 */

		Cdata cdata = transfer.toCdata();

		Terminal terminal = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(transfer.getTi()).orElse(null);
		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(transfer.getCi()).orElse(null);
		Location location = getLocation(terminal, counter);
		Area area = getArea(terminal, counter);

		String zoneId = LocationUtils.getZoneIdByLocation(location);

		Instant now = DateUtils2.newInstant();
		Instant rt = DateUtils2.adjust(DateUtils2.toInstant(transfer.getRt(), DatePattern.yyyyMMddHHmmss, zoneId, now), now);
		Instant tff = DateUtils2.adjust(DateUtils2.toInstant(transfer.getTff(), DatePattern.yyyyMMddHHmmss, zoneId, rt), rt);
		boolean firstGen = transfer.getCi() != null && transfer.getCi().startsWith("CMS");

		int accShotCount = 0;
		int incrShotCount = 0;
		boolean newCounter = false;
		Mold mold = null;

		// 2.2 Proc Counter
		// Counter Not Exists
		if (counter == null) {
			newCounter = true;

			counter = new Counter();
			counter.setEquipmentCode(transfer.getCi());
			populate(counter, terminal, location, transfer, rt, tff);
			counter.setLastShotAtVal(counter.getLastShotAt());
			counter.setPresetStatus(PresetStatus.APPLIED);
			counter.setActivatedAt(rt);
			counter.setEnabled(true);

			BeanUtils.get(CounterRepository.class).save(counter);

		} else {
			// ShotCount from Transfer
			accShotCount = transfer.getSc();
			// LastShotTime from Transfer
			Instant lst = DateUtils2.toInstant(transfer.getLst(), DatePattern.yyyyMMddHHmmss, zoneId);
			EquipmentStatus oldEquipmentStatus = counter.getEquipmentStatus();

			incrShotCount = DatColUtils.calcIncrShotCount(transfer.getCtt());
			populate(counter, terminal, location, transfer, rt, tff);

			if (counter.getMold() != null) {
				mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(counter.getMold().getId()).orElse(null);
				if (mold == null) {
					throw new LogicException("TRS_MOLD_NOT_FOUND", "Unexpected Mold Not Found:" + counter.getMold().getId());
				}

				mold.setLastShot(ValueUtils.toInteger(mold.getLastShot(), 0));

				Instant operatedAt = DateUtils2.maxReasonable(tff, mold.getOperatedAt(), rt);
				boolean lastOperated = mold.getOperatedAt() == null || (operatedAt != null && operatedAt.compareTo(mold.getOperatedAt()) > 0);

				// 2019-02-26 Last cycleTime 정보는 0보다 큰 경우에 저장함.
				if (lastOperated && transfer.getCt() != null && transfer.getCt() > 0) {
					// [2021-03-01] Update last cycle time if CT > 2secs for Dyson only
					double cycleTime = transfer.getCt();
					if (serverName.equalsIgnoreCase("dyson")) {
						if (cycleTime > 20) {
							mold.setLastCycleTime(cycleTime);
						}
					} else {
						mold.setLastCycleTime(cycleTime);
					}
				}

				// Fixed Logic
				if (lastOperated) {
					counter.setShotCount(accShotCount);
				}
				if (counter.getEquipmentCode().startsWith("EMA") || counter.getEquipmentCode().startsWith("NCM")) {
					Preset preset = BeanUtils.get(RstStpService.class).getLastByCounterCode(counter.getEquipmentCode());
					if (preset == null || preset.getCreatedAt().compareTo(operatedAt) < 0) {
						mold.setLastShot(mold.getLastShot() + incrShotCount);
					}
				} else if (lastOperated) {
					mold.setLastShot(accShotCount);
				}
//				// 2023.08.30 Mickey.Park
//				// *** Set Total ShotCount in Counter and Set Last ShotCount in Mold ***
//				// If the reset time occurred in the last operation has occurred before, add the Reset Count and the current Shot Count and store it.
//				if (lastOperated) {
//					if (counter.getEquipmentCode().startsWith("NCM")) {
//						Preset lastPreset = BeanUtils.get(RstStpService.class).getLastByCounterCode(counter.getEquipmentCode());
//						if (lastPreset != null && PresetStatus.READY.equals(counter.getPresetStatus()) //
//								&& lastPreset.getCreatedAt().compareTo(operatedAt) < 0) {
//
//							// The counter uses ShotCount transmitted from the sensor. However, mold applies on a Reset basis.
//							counter.setShotCount(accShotCount);
//							counter.setPresetStatus(PresetStatus.APPLIED);
//							mold.setLastShot(Integer.parseInt(lastPreset.getPreset()) + incrShotCount);
//
//						} else {
//							counter.setShotCount(accShotCount);
//							mold.setLastShot(mold.getLastShot() + incrShotCount);
//						}
//
//					} else if (counter.getEquipmentCode().startsWith("EMA")) {
//						DeviceCommand lastCmd = BeanUtils.get(RstStpService.class).getLastCommandByCounterCode(counter.getEquipmentCode());
//						if (lastCmd != null && PresetStatus.READY.equals(counter.getPresetStatus()) //
//								&& lastCmd.getCreatedAt().compareTo(operatedAt) < 0) {
//							counter.setShotCount(Integer.parseInt(lastCmd.getData()) + incrShotCount);
//							counter.setPresetStatus(PresetStatus.APPLIED);
//							mold.setLastShot(counter.getShotCount());
//						} else {
//							counter.setShotCount(accShotCount);
//							mold.setLastShot(mold.getLastShot() + incrShotCount);
//						}
//					}
//				}

				mold.setLastShotAt(DateUtils2.maxReasonable(lst, mold.getLastShotAt(), now));
				mold.setLastShotMadeAt(DateUtils2.adjust(mold.getLastShotMadeAt(), now));
				// Update last shot made at if the shot count is changed, auto confirm downtime alert
				if (incrShotCount > 0) {
					mold.setLastShotMadeAt(mold.getLastShotAt());

					// If there's an alert, then confirm it and set other confirmations to be old
					List<MoldDowntimeEvent> currentDowntimeAlert = BeanUtils.get(MoldDowntimeEventRepository.class)
							.findByMoldIdInAndDowntimeStatusAndLatest(Arrays.asList(mold.getId()), DowntimeStatus.DOWNTIME, true);
					if (currentDowntimeAlert != null && currentDowntimeAlert.size() > 0) {
						currentDowntimeAlert.forEach(x -> {
							x.setDowntimeStatus(DowntimeStatus.CONFIRMED);
							x.setConfirmedAt(Instant.now());
						});

						List<MoldDowntimeEvent> currentConfirmedAlert = BeanUtils.get(MoldDowntimeEventRepository.class)
								.findByMoldIdInAndDowntimeStatusAndLatest(Arrays.asList(mold.getId()), DowntimeStatus.CONFIRMED, true);
						if (currentConfirmedAlert != null && currentConfirmedAlert.size() > 0) {
							currentConfirmedAlert.forEach(x -> x.setLatest(false));
							BeanUtils.get(MoldDowntimeEventRepository.class).saveAll(currentConfirmedAlert);
						}
						BeanUtils.get(MoldDowntimeEventRepository.class).saveAll(currentDowntimeAlert);
					}
				}

				// Mold Status
				// 처음 수신인 경우 작동 상태를 '작업중'으로 변경..
				// 이후에는 수신 날짜만 체크한 후 TDATA 수신 (10분 간격) 시에
				// OperatingStatus를 업데이트 함.
				if (mold.getOperatedAt() == null) {
					mold.setOperatingStatus(OperatingStatus.WORKING);
				}
				if (lastOperated) {
					mold.setOperatedAt(operatedAt);
					mold.setEquipmentStatus(EquipmentStatus.INSTALLED);
				}
				if (mold.getOperatedStartAt() == null) {
					mold.setOperatedStartAt(DateUtils2.maxReasonable(lst, null, mold.getOperatedAt()));
				}

				// Company/Location/Area
				if (lastOperated) {
					// Relocation History
					Long areaId = area == null ? null : area.getId();
					boolean plantChanged = !ValueUtils.equals(mold.getLocationId(), location.getId());
					boolean relocated = plantChanged || !ValueUtils.equals(mold.getAreaId(), areaId);
					if (relocated) {
						mold.setLocationChanged(true);

						// Update latest = false at Previous Mold Locations
						if (mold.getId() != null) {
							List<MoldLocation> pendingList = BeanUtils.get(MoldLocationRepository.class)//
									.findByMoldIdAndMoldLocationStatusAndLatest(mold.getId(), MoldLocationStatus.PENDING, true);
							if (!ObjectUtils.isEmpty(pendingList)) {
								pendingList.forEach(moldLocation -> moldLocation.setLatest(false));
								BeanUtils.get(MoldLocationRepository.class).saveAll(pendingList);
							}
						}
						MoldLocation moldLocation = new MoldLocation();
						if (plantChanged) {
							moldLocation.setRelocationType(RelocationType.PLANT);
							moldLocation.setMoldLocationStatus(MoldLocationStatus.PENDING);
						} else {
							moldLocation.setRelocationType(RelocationType.AREA);
							moldLocation.setMoldLocationStatus(MoldLocationStatus.APPROVED);
						}

						moldLocation.setMoldLocationStatus(RelocationType.AREA.equals(moldLocation.getRelocationType()) ? MoldLocationStatus.APPROVED : MoldLocationStatus.PENDING);
						moldLocation.setNotificationAt(DateUtils2.getInstant());
						moldLocation.setLatest(true);
						moldLocation.setMold(mold);
						moldLocation.setLocation(location);
						moldLocation.setArea(area);
						moldLocation.setPreviousLocation(mold.getLocation());
						moldLocation.setPreviousArea(mold.getArea());
						BeanUtils.get(MoldLocationRepository.class).save(moldLocation);
					}

					mold.setCompanyId(location.getCompanyId());
					mold.setLocationId(location.getId());
					mold.setLocation(location);
					mold.setAreaId(areaId);
					mold.setArea(area);
				}

				if (!firstGen && lastOperated) {
					BeanUtils.get(RstStpService.class).postAlert(mold, counter);
				}

				BeanUtils.get(MoldRepository.class).save(mold);

				// CDATA에 MoldCode 등록
				cdata.setMoldId(mold.getId());
				cdata.setMoldCode(mold.getEquipmentCode());

				//write attachment sign
				if (lastOperated) {
					updateDetachmentSignal(mold, counter, transfer, oldEquipmentStatus);
				}
			}

			BeanUtils.get(CounterRepository.class).save(counter);
		}

		// log.info("Saving CDATA ID: " + cdata.getId() + "...");
		BeanUtils.get(CdataRepository.class).save(cdata);

		/**
		 * 4. Proc Statistics
		 */
		if (firstGen) {
			return;
		}

		// 통계데이터 저장 (STATISTICS)
		// 최초 카운트 등록 시 9999995 가 넘어오 통계데이터에 날짜별로 저장됨 방지.
		// && cycleTime > 0  : #39 사이클타입이 0보다 큰 경우에만 통계에 저장함.(20190226) ==> ct=0이면서 sc가 증가된 경우 통계 누락이 있어 조건 삭제함 (20200715)
		if (accShotCount < 9999900 && !newCounter && counter != null && counter.getPresetStatus() == PresetStatus.APPLIED) {
			//log.info("[saveCdata]save statistics for cycleTime > 0 {}", cdata.getCi());
			saveStatistics(cdata, incrShotCount, mold);
		}
		// Ver.2
		else if (VersionChecker.VERSION_R2.equals(cdata.getVer())) {
			//log.info("[saveCdata]save statistics for VERSION_R2 {}", cdata.getCi());
			saveStatistics(cdata, incrShotCount, mold);
		}
		// save statistics for preset status of counter not applied
		else if (accShotCount < 9999900) {
			//log.info("[saveCdata]save statistics for preset status of counter not applied: {}", cdata.getCi());
			Cdata cdata1 = DataUtils.deepCopy(cdata, Cdata.class);
			cdata1.setMoldCode(null);
			cdata1.setMoldId(null);
			saveStatistics(cdata1, incrShotCount, mold);
		} else {
			log.info("[saveCdata]SKIP save statistics : {}", cdata.getCi());
		}
//		});

		Statistics lastStat = BeanUtils.get(StatisticsRepository.class).findFirstByCiOrderByIdDesc(transfer.getCi()).orElse(null);
		BeanUtils.get(MoldMaintenanceService.class).proc(lastStat);
		if (mold != null) {
			BeanUtils.get(MoldService.class).enqueuePMWorkOrderDueDate(mold.getId());
		}
	}

	private void populate(Counter counter, Terminal terminal, Location location, Transfer transfer, Instant rt, Instant tff) {
		if (counter == null) {
			return;
		}

		String zoneId = LocationUtils.getZoneIdByLocation(location);

		Instant lst = DateUtils2.toInstant(transfer.getLst(), DatePattern.yyyyMMddHHmmss, zoneId);
		Instant operatedAt = DateUtils2.maxReasonable(tff, counter.getOperatedAt(), rt);
		boolean lastOperated = counter.getOperatedAt() == null || (operatedAt != null && operatedAt.compareTo(counter.getOperatedAt()) > 0);

		counter.setLastShotAt(DateUtils2.maxReasonable(lst, counter.getLastShotAt(), rt));
		// Actually, the Operating Status is updated by Scheduled Job
		if (counter.getOperatedAt() == null) {
			counter.setOperatingStatus(OperatingStatus.WORKING);
		}

		if (lastOperated) {
			if (terminal != null) {
				counter.setLastTerminalId(terminal.getId());
			}
			if (location != null) {
				counter.setLocation(location);
				counter.setCompanyId(location.getCompanyId());
			}

			counter.setShotCount(transfer.getSc());
			counter.setOperatedAt(operatedAt);
			if (counter.getMold() != null) {
				counter.setEquipmentStatus(EquipmentStatus.INSTALLED);
			} else {
				counter.setEquipmentStatus(EquipmentStatus.AVAILABLE);
			}
			counter.setBatteryStatus(getBatteryStatusByBS(transfer.getBs()));
		}
	}

	private boolean isDuplicated(String at, Transfer transfer) {
		LogicUtils.assertNotEmpty(at, "at");
		LogicUtils.assertNotNull(transfer, "transfer");

		String ci = transfer.getCi();
		String ti = transfer.getTi();
		Long li = transfer.getLi();
		Integer sn = transfer.getSn();
		String tff = transfer.getTff();
		if (!at.equals(transfer.getAt())) {
			LogUtils.saveErrorQuietly(ErrorType.WARN, "TRS_DIFFERENT_AT", HttpStatus.BAD_REQUEST, //
					"[TRS] At are different each other!! at:" + at + " trs.at:" + transfer.getAt() + " ci:" + ci + " sn:" + sn + " ti:" + ti + " li:" + li, transfer.getDs());
		}
		boolean firstGen = ci != null && ci.startsWith("CMS");
		boolean tffEmpty = ObjectUtils.isEmpty(tff) || firstGen;
		if ((tffEmpty && BeanUtils.get(TransferRepository.class).countByAtAndCiAndSn(at, ci, sn) > 0) //
				|| (!tffEmpty && BeanUtils.get(TransferRepository.class).countByAtAndCiAndSnAndTff(at, ci, sn, tff) > 0)) {
			if (!firstGen) {
				LogUtils.saveErrorQuietly(ErrorType.WARN, "TRS_SAME_CI_SN", HttpStatus.BAD_REQUEST, //
						"[TRS] Same CI with SN is Transferred Again!! at:" + at + " ci:" + ci + " sn:" + sn + " tff:" + tff + " ti:" + ti + " li:" + li, transfer.getDs());
			}
			return true;
		}
		if (ci != null && !tffEmpty && "CDATA".equals(at)) {
			if (BeanUtils.get(TransferIndexRepository.class).countByCiAndTff(ci, tff) > 0) {
				LogUtils.saveErrorQuietly(ErrorType.WARN, "TRS_SAME_CI_TFF", HttpStatus.BAD_REQUEST, //
						"[TRS] Same CI with TFF is Transferred Again!! at:" + at + " ci:" + ci + " sn:" + sn + " tff:" + tff + " ti:" + ti + " li:" + li, transfer.getDs());
				return true;
			}
			BeanUtils.get(TransferIndexRepository.class).save(new TransferIndex(ci, tff));
		}
		return false;
	}

	private Location getLocation(Terminal terminal, Counter counter) {
		if (terminal == null && counter == null) {
			return LocationUtils.getDefault();
		}

		if (terminal != null) {
			Location location = terminal.getLocation();
			if (location != null) {
				return location;
			}
		}
		if (counter != null) {
			Location location = counter.getLocation();
			if (location != null) {
				return location;
			}
		}

		return LocationUtils.getDefault();
	}

	private Area getArea(Terminal terminal, Counter counter) {
		if (terminal == null && counter == null) {
			return null;
		}

		if (terminal != null) {
			// If terminal location is null, it might mean terminal is not located correctly, yet
			// So the area might not be set up, yet.
			if (terminal.getLocation() != null) {
				return terminal.getArea();
			}
		}
		if (counter != null) {
			return counter.getArea();
		}

		return null;
	}

	public void makePresetMisconfigured(Mold mold, Counter counter) {
		//skip for reset counter
		if (mold == null) {
			return;
		}

		if (PresetStatus.READY == counter.getPresetStatus()) {
			List<Preset> presets = BeanUtils.get(PresetRepository.class).findAllByCiAndPresetStatusOrderByIdDesc(counter.getEquipmentCode(), PresetStatus.READY);

			if (presets.size() > 0) {
				Preset preset = presets.get(0);
				//update preset before alert
				BeanUtils.get(PresetService.class).autoUpdatePreset(counter, preset);

				// Last Preset Date => 마지막으로 Preset을 설정했던 시간? 적용된 시간?

				List<MoldMisconfigure> moldMisconfigureExist = BeanUtils.get(MoldMisconfigureRepository.class).findByCounterIdAndMisconfigureStatusInAndLatest(counter.getId(),
						Arrays.asList(MisconfigureStatus.MISCONFIGURED, MisconfigureStatus.CANCELED), true);
				if (moldMisconfigureExist != null && moldMisconfigureExist.size() > 0) {
					moldMisconfigureExist.forEach(moldMisconfigure -> {
						moldMisconfigure.setLatest(false);
					});
					BeanUtils.get(MoldMisconfigureRepository.class).saveAll(moldMisconfigureExist);
				}

				String triggeredBy = "";
				if (preset.getTriggeredBy() != null) {
					User user = BeanUtils.get(UserRepository.class).findById(preset.getTriggeredBy()).orElse(null);
					if (user != null) {
						triggeredBy += user.getName();
						if (user.getCompany() != null)
							triggeredBy += " (" + user.getCompany().getName() + ")";
					}
				}
				boolean isNew = false;
				MoldMisconfigure moldMisconfigure = moldMisconfigureExist != null
						? moldMisconfigureExist.stream().filter(m -> MisconfigureStatus.MISCONFIGURED.equals(m.getMisconfigureStatus())).findFirst().orElse(null)
						: null;

				if (moldMisconfigure == null) {
					moldMisconfigure = MoldMisconfigure.builder().notificationAt(Instant.now()).build();
					isNew = true;
				}
				moldMisconfigure.setMold(mold);
				moldMisconfigure.setCounterId(counter.getId());
				moldMisconfigure.setCounterCode(counter.getEquipmentCode());
				moldMisconfigure.setLastPresetDate(preset.getCreatedAt());
				moldMisconfigure.setPreset(preset.getPreset());
				moldMisconfigure.setMisconfigureStatus(MisconfigureStatus.MISCONFIGURED);
				moldMisconfigure.setLastShot(preset.getShotCount());
				if (counter.getShotCount() != null) {
					moldMisconfigure.setShotIncrease(counter.getShotCount() - preset.getShotCount());
				}
				moldMisconfigure.setLatest(true);
				moldMisconfigure.setTriggeredBy(triggeredBy);
				moldMisconfigure.setMessage(preset.getComment());

				moldMisconfigure = BeanUtils.get(MoldMisconfigureRepository.class).save(moldMisconfigure);

				if (isNew) {
					List<UserAlert> userAlertList = BeanUtils.get(UserAlertRepository.class).findByAlertTypeAndPeriodType(AlertType.MISCONFIGURE, PeriodType.REAL_TIME);
					List<MoldMisconfigure> moldMisconfigureList = Arrays.asList(moldMisconfigure);
					Map<User, List<AlertType>> userAlertMap = BeanUtils.get(MailService.class).getUserAlertListMap(userAlertList);
					List<LogUserAlert> logUserAlertList = BeanUtils.get(MoldService.class).buildLogUserAlert(userAlertMap, null, null, null, null, null, moldMisconfigureList, null,
							null, null, true);
					BeanUtils.get(LogUserAlertRepository.class).saveAll(logUserAlertList);

					List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> (x.getAlertOn() != null && x.getAlertOn()) && x.getEmail() == true)
							.collect(Collectors.toList());
					Map<User, List<AlertType>> toSendMailUserAlertMap = BeanUtils.get(MailService.class).getUserAlertListMap(toSendMailUserAlertList);
					BeanUtils.get(MailService.class).sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME, null, null, null, null, null, moldMisconfigureList, null,
							null, null, null);

					// Set status true for sent email users
					List<Long> sentMailUserIds = toSendMailUserAlertList.stream().map(x -> x.getUser().getId()).collect(Collectors.toList());
					List<Long> sentEmailAlertIds = logUserAlertList.stream().map(x -> x.getAlertId()).distinct().collect(Collectors.toList());
					List<LogUserAlert> sentMailLogUserAlertList = BeanUtils.get(LogUserAlertRepository.class).findByUserIdInAndAlertTypeAndAlertIdIn(sentMailUserIds,
							AlertType.MISCONFIGURE, sentEmailAlertIds);
					sentMailLogUserAlertList.forEach(sentMailLogUserAlert -> {
						sentMailLogUserAlert.setEmail(true);
					});
					BeanUtils.get(LogUserAlertRepository.class).saveAll(sentMailLogUserAlertList);
				}
				// [AlertEevent] 메세지 발송
				/*AlertEvent event = new MisconfigureAlertEvent(moldMisconfigure);
				applicationEventPublisher.publishEvent(event);
				log.info("[ALERT EVENT] Misconfigure Alert!!");*/

				mold.setMisconfigureStatus(MisconfigureStatus.MISCONFIGURED);
				BeanUtils.get(MoldService.class).save(mold);
			}
		}
	}

	public void updateDetachmentSignal(Mold mold, Counter counter, Transfer transfer, EquipmentStatus oldEquipmentStatus) {
		try {
			if ("Y".equals(transfer.getRat()) && (EquipmentStatus.DETACHED.equals(oldEquipmentStatus))) {
				counter.setEquipmentStatus(EquipmentStatus.INSTALLED);
			} else if ("N".equals(transfer.getRat()) && (EquipmentStatus.INSTALLED.equals(oldEquipmentStatus) || EquipmentStatus.INSTALLED.equals(counter.getEquipmentStatus()))) {
				counter.setEquipmentStatus(EquipmentStatus.DETACHED);
			} else
				return;

			mold.setEquipmentStatus(counter.getEquipmentStatus());
			BeanUtils.get(MoldRepository.class).save(mold);
			BeanUtils.get(CounterRepository.class).save(counter);
			if ("Y".equals(transfer.getRat()) && EquipmentStatus.DETACHED.equals(oldEquipmentStatus)
					|| "N".equals(transfer.getRat()) && EquipmentStatus.INSTALLED.equals(oldEquipmentStatus))
				BeanUtils.get(MoldService.class).createOrUpdateMoldDetachment(mold, oldEquipmentStatus, counter.getEquipmentStatus());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * #202. batteryStatus
	 *
	 * @param bs
	 * @return
	 */
	private BatteryStatus getBatteryStatusByBS(String bs) {
		if (bs == null) {
			return null;
		}
		BatteryStatus batteryStatus = null; // 배터리
		if ("H".equals(bs)) {
			batteryStatus = BatteryStatus.HIGH;
		} else if ("L".equals(bs)) {
			batteryStatus = BatteryStatus.LOW;
		} else if ("E".equals(bs)) {
			batteryStatus = BatteryStatus.EMPTY;
		}
		return batteryStatus;
	}

	private void saveStatistics(Cdata cdata, int incrShotCount, Mold mold) {
		// 마지막 Preset 설정 정보
		// preset = READY인 경우
		Preset preset = BeanUtils.get(PresetRepository.class).findFirstByCiAndPresetStatusOrderByIdDesc(cdata.getCi(), PresetStatus.APPLIED);

		int lastPresetShot = preset == null ? 0 : Integer.parseInt(preset.getPreset());
//		if (preset != null) {
//			lastPresetShot = Integer.parseInt(preset.getPreset());
//			if (preset != null) {
//				lastPresetShot = Integer.parseInt(preset.getPreset());
//				shotMissing = preset.getShotMissing();
//				preset.setApplied(true);
//				presetRepository.save(preset);
//			}
//		}

		Statistics stat = new Statistics(cdata);

		// lst 최종 데이터 조회
		Statistics prevStat = null;
		{
			QStatistics table = QStatistics.statistics;
			BooleanBuilder filter = new BooleanBuilder();
			Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lst", "id"));
			if (cdata.getMoldCode() != null) {
				filter.and(table.ci.eq(cdata.getCi())).and(table.moldId.eq(cdata.getMoldId())); // 이전 샷 정보 기준을 변경.(moldCode + ci ==> moldId + ci)
			} else {
				filter.and(table.ci.eq(cdata.getCi())); // 이전 샷 정보 기준을 변경.(moldCode + ci ==> moldId + ci)
			}
			Page<Statistics> page = BeanUtils.get(StatisticsRepository.class).findAll(filter, pageable);
			if (!page.getContent().isEmpty()) {
				prevStat = page.getContent().get(0);
			}
		}

		// 이전 데이터 기준으로 fsc와 shotCount를 계산
		if (prevStat == null) {
			stat.setFsc(0);
			stat.setFirstData(true);
		} else {
			stat.setFsc(prevStat.getSc());
			stat.setFirstData(false);

			// CT = 0
//			if (statistics.getCt() == 0) {
//				statistics.setFsc(statistics.getSc());
//				statistics.setFsc(statistics.getSc());
//				statistics.setShotCount(0);
//			}
		}

		// 마지막 설정 Preset과 비교
		//for new case
//		if (preset != null && shotMissing != null && shotMissing > 0) {
//			statistics.setFsc(statistics.getFsc() + shotMissing);
//		} else
		if (stat.getFsc() < lastPresetShot) {
			stat.setFsc(lastPresetShot);
		}
		stat.setShotCount(incrShotCount);
//		stat.setShotCount(stat.getSc() - stat.getFsc());

		DatColUtils.populateValidShot(cdata, stat, mold, false);

		log.info("[saveStatistics] " + cdata.getCtVal() == null ? "NULL" : "" + cdata.getCtVal());
		BeanUtils.get(CdataRepository.class).save(cdata);

		List<Statistics> insertStats = new ArrayList<>();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());
		DateTimeFormatter dayHourFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

		// 신규 터미널은 1시간 단위로 데이터가 전송되므로 날짜, 시간으로 분할하지 않고 기준 시간으로 통계 데이터를 저장함.
		// 기준 데이터는 ( temp > 온도1 시각? )
		if (VersionChecker.VERSION_R2.equals(cdata.getVer())) {
			// [2020-08-26] If cycle time <= 2s, it's wrong data --> set shot count is 0 (only Dyson)
			if (serverName.equalsIgnoreCase("dyson") && stat.getCt() <= 20 && stat.getCt() > 0) {
				stat.setShotCount(0);
			}

			// 전체 작동 시간 (초) = 샷 증가량 * CT(s)
			long uptimeSeconds = (long) (stat.getShotCount() * (stat.getCt() * 0.1));

			// 통계 기준시간
			String time = ValueUtils.toString(stat.getTff(), stat.getRt()); //온도 체크 시간? lastShot? Rt?
			if (!ObjectUtils.isEmpty(time) && time.startsWith("2036") && !ObjectUtils.isEmpty(stat.getRt()) && !stat.getRt().startsWith("2036")) {
				time = stat.getRt();
			}

			stat.setYear(ValueUtils.abbreviate(time, 4));
			stat.setMonth(ValueUtils.abbreviate(time, 6));
			stat.setDay(ValueUtils.abbreviate(time, 8));
			stat.setHour(ValueUtils.abbreviate(time, 10));
			stat.setWeek(DateUtils2.toOtherPattern(time, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));

			LocalDateTime startDate = LocalDateTime.parse(stat.getHour() + "0000", formatter);
			LocalDateTime endDate = startDate.plus(Duration.ofSeconds(uptimeSeconds));
			if (uptimeSeconds > 3600) {
				endDate = startDate.plus(Duration.ofSeconds(3600));
			}

			String fst = startDate.format(formatter);
			String lst = endDate.format(formatter);

			stat.setFst(fst);
			stat.setLst(lst);
			stat.setUptimeSeconds(uptimeSeconds);

			insertStats.add(stat);

		} else if (stat.getShotCount() > 0) { // ShotCount == 0 이면 pass (R1)
			if (stat.getCt() == 0) {
				LocalDateTime endDate = LocalDateTime.parse(stat.getLst(), formatter);
				String fst = endDate.format(formatter);
				String lst = endDate.format(formatter);

				stat.setFst(fst);
				stat.setLst(lst);
				stat.setUptimeSeconds(0L);

				stat.setYear(ValueUtils.abbreviate(lst, 4));
				stat.setMonth(ValueUtils.abbreviate(lst, 6));
				stat.setDay(ValueUtils.abbreviate(lst, 8));
				stat.setHour(ValueUtils.abbreviate(fst, 10));
				stat.setWeek(DateUtils2.toOtherPattern(lst, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));

				insertStats.add(stat);

				log.debug("## Stat. {}", stat);
			} else {
				// [2020-08-26] If cycle time <= 2s, it's wrong data --> set shot count is 0 (only Dyson)
				if (serverName.equalsIgnoreCase("dyson") && stat.getCt() <= 20) {
					stat.setShotCount(0);
				}

				// [2020-04-17] 통계 저장 시 시간 단위 까지 분리해서 저장함.

				// 전체 작동 시간 (초) = 샷 증가량 * CT(s)
				long totalUptimeSeconds = (long) (stat.getShotCount() * (stat.getCt() * 0.1));

				LocalDateTime endDateInDay = LocalDateTime.parse(stat.getLst(), formatter);
				LocalDateTime startDateInDay = endDateInDay.minus(Duration.ofSeconds(totalUptimeSeconds));

				long hours = HOURS.between(startDateInDay, endDateInDay) + 1; // 표시 시간을 위해 + 1
				int baseFsc = stat.getFsc();

				for (long i = 0; i <= hours; i++) {
					LocalDateTime startDateTime = endDateInDay.minus(Duration.ofHours(hours - i));
					LocalDateTime endDateTime = endDateInDay.minus(Duration.ofHours(hours - (i + 1)));

					String baseStartTime = startDateTime.format(dayHourFormatter) + "0000";
					String baseEndTime = endDateTime.format(dayHourFormatter) + "0000";

					LocalDateTime startDate = LocalDateTime.parse(baseStartTime, formatter);
					LocalDateTime endDate = LocalDateTime.parse(baseEndTime, formatter);

					// 실제 시작 시간과 종료시간을 계산 (1시간 꽉 채우지 않는 경우)
					long startSeconds = SECONDS.between(startDate, startDateInDay);
					long endSeconds = SECONDS.between(endDate, endDateInDay);

					if (startSeconds > 0)
						startDate = startDateInDay;
					if (endSeconds < 0)
						endDate = endDateInDay;

					// 해당 시간 범위에 실제 동작 시간 계산
					long uptimeSeconds = SECONDS.between(startDate, endDate);

					if (uptimeSeconds <= 0) {
						continue;
					}

					int shotCount = (int) (((double) uptimeSeconds) / (stat.getCt() / 10));
					int fsc = baseFsc;
					int sc = fsc + shotCount;
					String fst = startDate.format(formatter);
					String lst = endDate.format(formatter);

					Statistics statNew = new Statistics();
					statNew.setMoldId(stat.getMoldId());
					statNew.setMoldCode(stat.getMoldCode());
					statNew.setCdataId(cdata.getId());
					statNew.setTi(stat.getTi());
					statNew.setCi(stat.getCi());
					statNew.setCt(stat.getCt());

					statNew.setFst(fst);
					statNew.setLst(lst);
					statNew.setFsc(fsc);
					statNew.setSc(sc);
					statNew.setShotCount(shotCount);
					statNew.setUptimeSeconds(uptimeSeconds);

					stat.setYear(ValueUtils.abbreviate(lst, 4));
					stat.setMonth(ValueUtils.abbreviate(lst, 6));
					stat.setDay(ValueUtils.abbreviate(lst, 8));
					stat.setHour(ValueUtils.abbreviate(fst, 10));
					stat.setWeek(DateUtils2.toOtherPattern(lst, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));
					statNew.setFirstData((i == 0 && stat.getFirstData() == true) ? true : false);

					insertStats.add(statNew);

					log.debug("## Stat. {}", statNew);

					baseFsc = statNew.getSc();
				}
			}
		}
		//

		// Statistics Part 등록
		List<MoldPart> moldParts = BeanUtils.get(MoldPartRepository.class).findAllByMoldIdOrderById(stat.getMoldId());

		// 2020.07.13 Part
		List<Part> parts = moldParts.stream().filter(mp -> mp.getPart() != null).map(mp -> {
			Part part = mp.getPart();
			part.setDataUpdatedOn(Instant.now());
			return part;
		}).collect(Collectors.toList());

		BeanUtils.get(PartRepository.class).saveAll(parts);

		// Statistics / StatisticsPart 등록
		for (int i = 0; i < insertStats.size(); i++) {
			Statistics st = insertStats.get(i);
			DatColUtils.populateValidShot(cdata, st, null, false);
			BeanUtils.get(StatisticsRepository.class).save(st);
			if (st.getId() != null) {
				StatisticsExt ext = new StatisticsExt();
				ext.setId(st.getId());
				ext.setCdataId(st.getCdataId());
				if (st.getShotCount() == null || cdata.getCtt() == null || ObjectUtils.isEmpty(StringUtils.replace(cdata.getCtt(), "/", ""))) {
					ext.setShotCountCtt(0);
					ext.setShotCountCttVal(0);
				} else {
					ext.setShotCountCtt(st.getShotCount());
					if (stat.getCt() == null || stat.getCt() * st.getShotCount() > 72000) {
						ext.setShotCountCttVal(0);
					} else {
						ext.setShotCountCttVal(st.getShotCount());
					}
				}
				BeanUtils.get(StatisticsExtRepository.class).save(ext);
			}

			BeanUtils.get(MachineDowntimeAlertServiceNew.class).proc(st);

			int activeCavities = moldParts.stream().filter(m -> m.getCavity() != null && m.getCavity() > 0).mapToInt(MoldPart::getCavity).sum();
			BeanUtils.get(OeeService.class).proc(st, activeCavities);

			for (MoldPart moldPart : moldParts) {
				StatisticsPart sPart = new StatisticsPart();

				sPart.setStatistics(st);

				if (moldPart.getPart() != null && moldPart.getCavity() > 0) {
					Part part = moldPart.getPart();
					sPart.setPartId(part.getId());
					sPart.setPartCode(part.getPartCode());
					sPart.setCavity(moldPart.getCavity());

					if (part.getCategory() != null) {
						Category project = part.getCategory();
						sPart.setProjectId(project.getId());
						sPart.setProjectName(project.getName());

						if (project.getParent() != null) {
							Category category = project.getParent();
							sPart.setCategoryId(category.getId());
							sPart.setCategoryName(category.getName());
						}
					}

					updatePartProjectProduced(moldPart, sPart, st);
				}

				BeanUtils.get(StatisticsPartRepository.class).save(sPart);
			}

			// update produced part for rejected part
			BeanUtils.get(RejectedPartService.class).saveProducedPart(st, moldParts);
		}
		if (!insertStats.isEmpty() && mold != null) {
			BeanUtils.get(MoldService.class).procWact(mold);
		}
		//process for uptimeSeconds=0;
		if (!insertStats.isEmpty() && insertStats.get(0).getUptimeSeconds() != null && insertStats.get(0).getUptimeSeconds().intValue() == 0
				&& insertStats.get(0).getMoldId() != null) {
			/*
			//todo: turn off wut service
			BeanUtils.get(MoldService.class).checkWUTForStatisticsNew(insertStats.get(0).getMoldId(), cdata.getLst());
			*/
		}
	}

	private void updatePartProjectProduced(MoldPart moldPart, StatisticsPart sPart, Statistics statistics) {
		try {
			int newProduced = statistics.getShotCount() * moldPart.getCavity();
			int newProducedVal = statistics.getShotCountVal() * moldPart.getCavity();

			Optional<PartProjectProduced> optional = BeanUtils.get(PartProjectProducedRepository.class).findByPartIdAndProjectId(sPart.getPartId(), sPart.getProjectId());
			PartProjectProduced partProjectProduced;
			if (optional.isPresent()) {
				partProjectProduced = optional.get();
			} else {
				partProjectProduced = new PartProjectProduced();
				partProjectProduced.setPartId(sPart.getPartId());
				partProjectProduced.setProjectId(sPart.getProjectId());
			}

			partProjectProduced.setTotalProduced(partProjectProduced.getTotalProduced() + newProduced);
			partProjectProduced.setTotalProducedVal(partProjectProduced.getTotalProducedVal() + newProducedVal);
			BeanUtils.get(PartProjectProducedRepository.class).save(partProjectProduced);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -- Start
	@Deprecated
	private void initCdataAndStatisticsVal(final Cdata cdata, final Statistics statistics) {
		if (statistics.getShotCount() == null || statistics.getShotCount() < 0) {
			statistics.setShotCount(0);
		}
		statistics.setCtVal(statistics.getCt());
		statistics.setLlctVal(statistics.getLlct());
		statistics.setUlctVal(statistics.getUlct());
		statistics.setShotCountVal(statistics.getShotCount());
		if (cdata != null) {
			cdata.setCttVal(cdata.getCtt());
			cdata.setCtVal(cdata.getCt());
			cdata.setLlctVal(cdata.getLlctVal());
			cdata.setUlctVal(cdata.getUlct());
		}
	}

	@Deprecated
	private void populateValidShot(final Cdata cdata, final Statistics statistics, Mold mold) {
		try {
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(statistics.getCi()).orElse(null);

			if (statistics.getShotCount() == null) {
				initCdataAndStatisticsVal(cdata, statistics);
				return;
			}
			int thi = cdata.getThi() != null ? cdata.getThi().intValue() : 0;
			int tlo = cdata.getTlo() != null ? cdata.getTlo().intValue() : 0;
			Integer tdev = thi - tlo;

			if (mold == null) {
				if (statistics.getMoldId() == null) {
					initCdataAndStatisticsVal(cdata, statistics);
					return;
				}
				mold = BeanUtils.get(MoldService.class).findByIdWithoutAdditionalInfo(statistics.getMoldId());
				if (mold == null) {
					initCdataAndStatisticsVal(cdata, statistics);
					return;
				}
			}

			double wact = MoldUtils.getWact(mold.getId(), mold.getContractedCycleTime(), cdata.getMonth());

			Double ctVal = cdata.getCt();
			String cttVal = cdata.getCtt();
			Double llctVal = cdata.getLlct();
			Double ulctVal = cdata.getUlct();

			Double ctValStatistics = statistics.getCt();
			Integer shotCountVal = statistics.getShotCount();
			Double llctValStatistics = statistics.getLlct();
			Double ulctValStatistics = statistics.getUlct();

			if (Math.abs(tdev) <= 20 && statistics.getShotCount() <= 10) {

				ctVal = 0d;
				cttVal = "///////////////////";
				llctVal = 0d;
				ulctVal = 0d;

				ctValStatistics = 0d;
				llctValStatistics = 0d;
				ulctValStatistics = 0d;
				shotCountVal = 0;

			} else {

				double ctMin = (wact - 50) / 3 + 50;
				double ctMax = (wact - 50) * 2 + 50;

				if (ctMin < cdata.getCt() && cdata.getCt() < ctMax) {
					//default
				} else {
					ctVal = 0d;
					cttVal = "///////////////////";
					llctVal = 0d;
					ulctVal = 0d;

					ctValStatistics = 0d;
					llctValStatistics = 0d;
					ulctValStatistics = 0d;
					shotCountVal = 0;
				}
			}

			cdata.setCtVal(ctVal);
			cdata.setCttVal(cttVal);
			cdata.setLlctVal(llctVal);
			cdata.setUlctVal(ulctVal);

			statistics.setLlctVal(llctValStatistics);
			statistics.setUlctVal(ulctValStatistics);
			statistics.setCtVal(ctValStatistics);
			statistics.setShotCountVal(shotCountVal);
			if (statistics.getShotCountVal() != 0) {
				mold.setLastShotAtVal(DateUtils2.maxReasonable(mold.getLastShotAt(), mold.getLastShotAtVal(), null));
				BeanUtils.get(MoldRepository.class).save(mold);
				if (counter != null) {
					counter.setLastShotAtVal(counter.getLastShotAt());
					BeanUtils.get(CounterRepository.class).save(counter);
				}
			}
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.LOGIC, "VALID_SHOT_FAIL", HttpStatus.EXPECTATION_FAILED, "Error Occurred during Filtering Valid Shot", e);
		}

	}

	// -- End

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void procValidShotAll() {
		JobUtils.runIfNotRunning("TRS::validShotAll", new JobOptions().setClustered(true), () -> {
			int i = 0;
			while (i++ < 10000 && procValidShotLoop(500)) {
			}
			// 트랜잭션 수행 중인 다른 곳으로 부터 호출될 경우 위 분리된 트랜잭션 로직에서 변경한 데이터를 인식할 수 없으므로, 같이 분리해 줍니다.
			BeanUtils.get(TransferService.class).updateValsAsOriginWhenIsNullWithNewTran();
		});
	}

	private boolean procValidShotLoop(int pageSize) {
		List<Statistics> list = BeanUtils.get(StatisticsRepository.class).findAllByCdataIdIsNotNullAndMoldIdIsNotNullAndShotCountValIsNull(PageRequest.of(0, pageSize))
				.getContent();
		return procValidShotAll(list);
	}

	@Transactional(propagation = Propagation.NEVER)
	public void procValidShotByMoldId(Long moldId, List<String> months) {
		if (ObjectUtils.isEmpty(months)) {
			months = Arrays.asList(DateUtils2.format(Instant.now(), DatePattern.yyyyMM, Zone.SYS));
		}
		List<String> _months = months;
		JobUtils.runIfNotRunning("TRS::validShot." + moldId, new JobOptions().setClustered(true), () -> {
			QStatistics table = QStatistics.statistics;

			Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);

			for (String month : _months) {
				MoldUtils.deleteStandardValue(moldId, mold.getContractedCycleTime(), month);
				BooleanBuilder filter = new BooleanBuilder()//
						.and(table.moldId.eq(moldId))//
						.and(table.cdataId.isNotNull())//
						.and(table.month.eq(month));

				Page<Statistics> page;
				int i = 0;
				while (i < 10000 && !(page = BeanUtils.get(StatisticsRepository.class).findAll(filter, PageRequest.of(i++, 500, Direction.ASC, "id"))).isEmpty()) {
					procValidShotAll(page.getContent());
				}
			}
		});
	}

	private boolean procValidShotAll(List<Statistics> list) {
		if (ObjectUtils.isEmpty(list)) {
			return false;
		}

		list.forEach(stat -> {
			try {
				TranUtils.doNewTran(() -> {
					Mold mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(stat.getMoldId()).orElse(null);
					Cdata cdata = BeanUtils.get(CdataRepository.class).findById(stat.getCdataId()).orElse(null);
					if (cdata == null || mold == null) {
						DatColUtils.initCdataAndStatisticsVal(cdata, stat);
					} else {
						DatColUtils.populateValidShot(cdata, stat, mold, false);
					}
					if (cdata != null) {
						BeanUtils.get(CdataRepository.class).save(cdata);
					}
					BeanUtils.get(StatisticsRepository.class).save(stat);
				});
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "VALID_SHOT_FAIL", HttpStatus.EXPECTATION_FAILED, "Valid Shot Failed statisticsId:" + stat.getId(), e);
			}
		});
		return true;
	}

	public void procWutAll() {
		BeanUtils.get(MoldService.class).procWutAll();
	}

	@Transactional
	@Deprecated
	public void resetStoredAllValidShotVals() {
		BeanUtils.get(StatisticsRepository.class).resetStoredAllValidShotVals();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateValsAsOriginWhenIsNullWithNewTran() {
		BeanUtils.get(StatisticsRepository.class).updateValsAsOriginWhenIsNull();
	}

}
