package saleson.api.preset;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.api.analysis.resource.base.command.service.device.DeviceCommandService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.counter.CounterRepository;
import saleson.api.endLifeCycle.MoldEndLifeCycleService;
import saleson.api.mold.MoldMisconfigureRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.statistics.StatisticsPresetRepository;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.PresetStatus;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.MoldMisconfigure;
import saleson.model.Preset;
import saleson.model.StatisticsPreset;

@Slf4j
@Service
@Transactional
public class PresetService {

	/**
	 * Find Recent Preset which is ready 
	 * @param transfer
	 * @return
	 */
	@Deprecated
	public Optional<Preset> findRecentReady(String ci) {
		List<Preset> presets = BeanUtils.get(PresetRepository.class).findAllByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.READY);
		if (ObjectUtils.isEmpty(presets)) {
			return Optional.empty();
		}
		return Optional.of(presets.get(0));
	}

	/**
	 * PRESET 두번째 요청에 대한 처리.
	 * @param transfer
	 * @return
	 */
	@Deprecated
	public Optional<Preset> verifyAndApply(String ci, int sc) {
		List<Preset> presets = BeanUtils.get(PresetRepository.class).findAllByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.READY);
		if (ObjectUtils.isEmpty(presets)) {
			return Optional.empty();
		}
		Preset preset = presets.get(0);

		String scStr = StringUtils.trimAllWhitespace(preset.getPreset());
		// preset 데이터 값이 비어있는 경우
		if (ObjectUtils.isEmpty(scStr)) {
			return Optional.empty();
		}

		// preset 값과 sc(shot count) 값이 일치하는가?
		if (Integer.parseInt(scStr) != sc) {
			return Optional.empty();
		}

		// Apply
		Preset savedEntity = apply(preset);

		// Confirm
		return savedEntity == null ? Optional.empty() : Optional.of(savedEntity);
	}

	@Deprecated
	public Preset apply(Preset preset) {
		String scStr = StringUtils.trimAllWhitespace(preset.getPreset());
		// preset 데이터 값이 비어있는 경우
		if (ObjectUtils.isEmpty(scStr)) {
			return null;
		}
		int sc = Integer.parseInt(scStr);

		// 일치하는 경우 (CI 기준 데이터 삭제)  ==> 2018-09-14 로그를 남기기 위해 삭제 대상 상태로 변경
		// presetRepository.deleteAllByCi(transfer.getCi());
		preset.setPresetStatus(PresetStatus.APPLIED);
		preset.setApplyDesc(preset.getApplyDesc() + "\\n[" + DateUtils.getToday() + "] Preset count is applied.");
		Preset savedEntity = BeanUtils.get(PresetRepository.class).save(preset);

		// Counter Preset 적용 상태를 저장 (statistics 저장 시 preset 상태가 APPLIED 인 경우에만 통계 데이터 저장)
		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi()).orElse(null);
		if (counter == null) {
			return savedEntity;
		}

		counter.setPresetStatus(PresetStatus.APPLIED);

		if (counter.getEquipmentCode() != null && counter.getEquipmentCode().startsWith("EM")) {
			counter.setShotCount(sc);
			BeanUtils.get(CounterRepository.class).save(counter);

			DeviceCommandDto command = new DeviceCommandDto();
			command.setDeviceId(counter.getEquipmentCode());
			command.setDeviceType("SS3");
			command.setCommand("1");
			command.setComment("RESET");
			command.setData(sc + "");
			BeanUtils.get(DeviceCommandService.class).post(command);

		} else if (counter.getMold() == null) {
			counter.setShotCount(sc);
			BeanUtils.get(CounterRepository.class).save(counter);
		} else {
			Mold mold = counter.getMold();
			// Add calculator miss day
			if (preset.getShotMissing() > 0) {
				Instant operatedStartAt = mold.getOperatedStartAt();
				if (operatedStartAt == null) {
					operatedStartAt = Instant.now().minus(1, ChronoUnit.DAYS);
				}
				Double dualDate = (Double.valueOf(Instant.now().getEpochSecond() - operatedStartAt.getEpochSecond())) / (24 * 3600);
				if (dualDate == 0d) {
					dualDate = 1d;
				}
				Integer forecastShotsPerDay = ((Long) Math.round(preset.getShotCount() / dualDate)).intValue();
				forecastShotsPerDay = forecastShotsPerDay == 0 ? 1 : forecastShotsPerDay;

				Integer missDays = preset.getShotMissing() / forecastShotsPerDay;
				preset.setMissingDays(missDays);
				BeanUtils.get(PresetRepository.class).save(preset);
				//update forecasted max shots
				if (preset.getForecastedMaxShots() != null && preset.getForecastedMaxShots() > 0) {
					mold.setDesignedShot(preset.getForecastedMaxShots());
					BeanUtils.get(MoldRepository.class).save(mold);
					BeanUtils.get(MoldEndLifeCycleService.class).createOrUpdateMoldEndLifeCycle(mold, true);
				}
			}

		}

		List<MoldMisconfigure> moldMisconfigureConfirmedList = BeanUtils.get(MoldMisconfigureRepository.class).findByCounterIdAndMisconfigureStatusAndLatest(counter.getId(),
				MisconfigureStatus.CONFIRMED, true);
		moldMisconfigureConfirmedList.forEach(x -> x.setLatest(false));
		BeanUtils.get(MoldMisconfigureRepository.class).saveAll(moldMisconfigureConfirmedList);

		List<MoldMisconfigure> moldMisconfigure = BeanUtils.get(MoldMisconfigureRepository.class).findByCounterIdAndMisconfigureStatusAndLatest(counter.getId(),
				MisconfigureStatus.MISCONFIGURED, true);
		if (moldMisconfigure != null && moldMisconfigure.size() > 0) {
			moldMisconfigure.get(0).setConfirmedAt(Instant.now());
			moldMisconfigure.get(0).setMisconfigureStatus(MisconfigureStatus.CONFIRMED);
			BeanUtils.get(MoldMisconfigureRepository.class).save(moldMisconfigure.get(0));
		}
		// Save statistics
		if (preset.getShotMissing() != null && preset.getShotMissing() != 0 && counter.getMold() != null) {
			StatisticsPreset statisticsPreset = new StatisticsPreset(preset.getId(), counter.getMold().getId(), counter.getMold().getEquipmentCode(), preset.getShotMissing(),
					preset.getMissingDays(), counter.getShotCount());
			BeanUtils.get(StatisticsPresetRepository.class).save(statisticsPreset);
		}

		return savedEntity;
	}

	/**
	 * Preset 정보를 등록함.
	 * @param preset
	 */
	public Preset save(Preset preset) {

		Integer shotCount = 0;

		// Counter Preset 적용 상태를 저장 (statistics 저장 시 preset 상태가 APPLIED 인 경우에만 통계 데이터 저장)
		Optional<Counter> optional = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi());
		if (optional.isPresent()) {
			Counter counter = optional.get();
			counter.setPresetStatus(PresetStatus.READY);

			BeanUtils.get(CounterRepository.class).saveAndFlush(counter);

			shotCount = counter.getShotCount() == null ? 0 : counter.getShotCount();

			if (counter.getMold() != null) {
				Integer presetValue = preset.getPreset() != null && !preset.getPreset().equalsIgnoreCase("null") ? Integer.valueOf(preset.getPreset()) : 0;
				Integer lastShort = counter.getMold().getLastShot() != null ? counter.getMold().getLastShot() : 0;
				preset.setShotMissing(presetValue - lastShort);
			}
		}

		// Preset 정보 등록
		preset.setPresetStatus(PresetStatus.READY);
		preset.setApplyDesc("[" + DateUtils.getToday() + "] Preset setting is ready.");
		preset.setShotCount(shotCount); // #25 PRESET 신청 시점의 counter의 shotCount 정보를 저장.
		preset.setTriggeredBy(SecurityUtils.getUserId());

		// 1. 기존 카운터 CI로 READY 인 상태가 있는 경우 CANCEL로 업데이트 ..
		BeanUtils.get(PresetRepository.class).updateReadyToCancelByCi(preset.getCi());

		// 2. Preset 정보 등록
		return BeanUtils.get(PresetRepository.class).saveAndFlush(preset);

	}

	/**
	 * PRESET 정보를 취소한다.
	 * @param preset
	 */
	public void cancel(Preset preset) {
		Optional<Counter> optional = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi());
		if (optional.isPresent()) {
			Counter counter = optional.get();
			counter.setPresetStatus(PresetStatus.APPLIED); // PRESET 설정 전 상태를 알 수 없음 (NULL OR APPLIED) -> 취소시 카운터의 PRESET_STATUS = 'APPLIED'로 변경함.

			BeanUtils.get(CounterRepository.class).save(counter);

			List<MoldMisconfigure> moldMisconfigure = BeanUtils.get(MoldMisconfigureRepository.class).findByCounterIdAndMisconfigureStatusAndLatest(counter.getId(),
					MisconfigureStatus.MISCONFIGURED, true);
			if (moldMisconfigure != null && moldMisconfigure.size() > 0) {
				moldMisconfigure.get(0).setMisconfigureStatus(MisconfigureStatus.CANCELED);
				BeanUtils.get(MoldMisconfigureRepository.class).save(moldMisconfigure.get(0));
			}
		}

		// Preset 정보 등록
		preset.setPresetStatus(PresetStatus.CANCELED);
		preset.setApplyDesc("[" + DateUtils.getToday() + "] Preset setting is canceled.");
		preset.setTriggeredBy(null);

		// 2. Preset 정보 등록
		BeanUtils.get(PresetRepository.class).save(preset);
		
		// 3. Reset Rollback
		// 2023.09.22 Mickey.Park
		// Product Team과 상의 후 결정
		// BeanUtils.get(RstStpService.class).cancelPreset(preset);

	}

	@Deprecated
	public void autoCreatePreset(Counter counter) {
		return;
		/*if (counter.getPresetStatus() == PresetStatus.APPLIED || (counter.getPresetCount() == null && counter.getPresetStatus() == PresetStatus.READY)) {
			Preset preset = new Preset(counter.getEquipmentCode());
			Integer presetValue = counter.getPresetCount() != null ? counter.getPresetCount() : 0;
			preset.setPreset(String.valueOf(presetValue));
		//			Integer lastShort = counter.getMold().getLastShot() != null ? counter.getMold().getLastShot() : 0;
		//			preset.setShotMissing(counter.getMold() != null ?
		//					presetValue - lastShort
		//					: null);
			save(preset);
		}*/
	}

	@Deprecated
	public void autoUpdatePreset(Counter counter, Preset preset) {
		return;
//		if (counter == null || preset == null || !PresetStatus.READY.equals(counter.getPresetStatus()) || counter.getEquipmentCode().startsWith("EM")) {
//			return;
//		}
//
//		try {
//			// Preset preset = presets.get(0);
//			Integer lastShot = counter.getShotCount() != null ? counter.getShotCount() : 0;
//			Integer shotCount = preset.getShotCount() != null ? preset.getShotCount() : 0;
//			Integer shotsFromLastPreset = lastShot - shotCount;
//			Integer presetValue = preset.getPreset() != null && !preset.getPreset().equalsIgnoreCase("null") ? Integer.valueOf(preset.getPreset()) : 0;
//			preset.setShotCount(lastShot);
//			// fix for case reset not change to applied.
//			if (shotsFromLastPreset < 0) {
//				if (presetValue < lastShot) {
//					shotsFromLastPreset = lastShot - presetValue;
//				} else {
//					shotsFromLastPreset = 0;
//				}
//			}
//
//			preset.setPreset(String.valueOf(presetValue + shotsFromLastPreset));
//			BeanUtils.get(PresetRepository.class).save(preset);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Transactional(propagation = Propagation.NEVER)
	public void jobCalMissingDay() {
		log.info("[jobCalMissingDay] JOB START  ------------------------------------");

		int[] success = { 0 };
		List<Preset> presets = TranUtils.doNewTran(() -> BeanUtils.get(PresetRepository.class)//
				.findAllByPresetStatusAndShotMissingGreaterThanAndMissingDaysIsNull(PresetStatus.APPLIED, 0));
		log.info("presets count: " + presets.size());

		for (Preset preset : presets) {
			if (ValueUtils.toInteger(preset.getShotMissing(), 0) <= 0 || preset.getUpdatedAt() == null) {
				continue;
			}

			TranUtils.doNewTran(() -> {
				List<StatisticsPreset> statPresets = BeanUtils.get(StatisticsPresetRepository.class).findAllByPresetId(preset.getId());
				if (ObjectUtils.isEmpty(statPresets)) {
					return;
				}

				StatisticsPreset statPreset = statPresets.get(0);
				if (statPreset.getMoldId() == null) {
					return;
				}

				Mold mold = BeanUtils.get(MoldRepository.class).findById(statPreset.getMoldId()).orElse(null);
				if (mold == null) {
					return;
				}

				//add calculator miss day
				Instant operatedStartAt = mold.getOperatedStartAt();
				if (operatedStartAt == null) {
					operatedStartAt = Instant.now().minus(1, ChronoUnit.DAYS);
				}
				Double dualDate = (Double.valueOf(preset.getUpdatedAt().getEpochSecond() - operatedStartAt.getEpochSecond())) / (24 * 3600);
				if (dualDate < 0) {
					return;
				}
				if (dualDate == 0d) {
					dualDate = 1d;
				}
				Integer forecastShotsPerDay = ((Long) Math.round(preset.getShotCount() / dualDate)).intValue();
				forecastShotsPerDay = forecastShotsPerDay == 0 ? 1 : forecastShotsPerDay;

				Integer missDays = preset.getShotMissing() / forecastShotsPerDay;
				preset.setMissingDays(missDays);
				BeanUtils.get(PresetRepository.class).save(preset);
				statPreset.setMissingDays(missDays);
				BeanUtils.get(StatisticsPresetRepository.class).save(statPreset);
				success[0]++;
			});
		}

		log.info("[jobCalMissingDay] success: " + success[0]);
		log.info("[jobCalMissingDay] End ");
	}
}
