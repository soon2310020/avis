package com.emoldino.api.asset.resource.composite.rststp.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommand;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.QDeviceCommand;
import com.emoldino.api.analysis.resource.base.command.service.device.DeviceCommandService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.counter.CounterRepository;
import saleson.api.endLifeCycle.MoldEndLifeCycleService;
import saleson.api.mold.MoldMisconfigureRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.preset.PresetRepository;
import saleson.api.user.UserAlertRepository;
import saleson.api.user.UserRepository;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.MisconfigureStatus;
import saleson.common.enumeration.PeriodType;
import saleson.common.enumeration.PresetStatus;
import saleson.common.notification.MailService;
import saleson.model.Counter;
import saleson.model.LogUserAlert;
import saleson.model.Mold;
import saleson.model.MoldMisconfigure;
import saleson.model.Preset;
import saleson.model.User;
import saleson.model.UserAlert;
import saleson.service.transfer.LogUserAlertRepository;

@Service
@Transactional
public class RstStpService {

	public void apply(Preset preset) {

		String scStr = StringUtils.trimAllWhitespace(preset.getPreset());
		if (!StringUtils.hasText(scStr)) {
			return;
		}

		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi()).orElse(null);
		if (counter == null) {
			return;
		}

		// 1. Change existing preset status : CANCELED
		List<Preset> presets = BeanUtils.get(PresetRepository.class).findAllByCiAndPresetStatusOrderByIdDesc(counter.getEquipmentCode(), PresetStatus.READY);
		if (!ObjectUtils.isEmpty(presets)) {
			for (Preset item : presets) {
				if (item.getId().equals(preset.getId())) {
					continue;
				}
				item.setPresetStatus(PresetStatus.CANCELED);
			}
			BeanUtils.get(PresetRepository.class).saveAll(presets);
			BeanUtils.get(PresetRepository.class).flush();
		}

		Mold mold = counter.getMold();
		int sc = Integer.parseInt(scStr);

		// 2. Generate Reset Alert, Command
		if (preset.getCi().startsWith("NCM")) {
			// 2nd Sensor: Generated Reset Alert
			counter.setPresetStatus(PresetStatus.READY);
			postAlert(mold, counter);

			// Setup Preset Status: 2nd Gen - READY 
			preset.setApplyDesc("[" + DateUtils2.getInstant().toString() + "] Preset count is applied in system. (But Preset is not applied at counter)");
			preset.setPresetStatus(PresetStatus.READY);
			BeanUtils.get(PresetRepository.class).saveAndFlush(preset);
		} else if (preset.getCi().startsWith("EMA")) {
			// 3rd Sensor: Generate Reset Command
			counter.setPresetStatus(PresetStatus.READY);
			BeanUtils.get(DeviceCommandService.class).post(DeviceCommandDto.builder()//
					.deviceType("SS3")//
					.deviceId(preset.getCi())//
					.command("1")//
					.comment("RESET")//
					.data(Integer.toString(sc))//
					.build());

			// Setup Preset Status: 3rd Gen - APPLID
			preset.setApplyDesc("[" + DateUtils2.getInstant().toString() + "] Preset count is applied in system. (But Preset is not applied at counter)");
			preset.setPresetStatus(PresetStatus.APPLIED);
			BeanUtils.get(PresetRepository.class).saveAndFlush(preset);
		}

		// 3. Set Preset Count 
		counter.setPresetCount(sc);
		BeanUtils.get(CounterRepository.class).save(counter);

		// 4. Set Shot Count in Mold				
		if (mold != null) {
			if (preset.getForecastedMaxShots() != null && preset.getForecastedMaxShots() > 0) {
				mold.setDesignedShot(preset.getForecastedMaxShots());
				BeanUtils.get(MoldEndLifeCycleService.class).createOrUpdateMoldEndLifeCycle(mold, true);
			}
			// Mold Last Shot is set by 
			mold.setLastShot(sc);
			BeanUtils.get(MoldRepository.class).save(mold);
		}

	}

	public void cancel(Preset preset) {
//		// 1. Mold ShotCount Rollback
//		int shotCount = preset.getShotCount();
//		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi()).orElse(null);
//		if (!ObjectUtils.isEmpty(counter)) {
//			counter.setShotCount(shotCount);
//			counter.setPresetCount(null);
//			counter.setPresetStatus(PresetStatus.CANCELED);
//			Mold mold = counter.getMold();
//			if (!ObjectUtils.isEmpty(mold)) {
//				mold.setLastShot(shotCount);
//				BeanUtils.get(MoldRepository.class).save(mold);
//			}
//			BeanUtils.get(CounterRepository.class).save(counter);
//		}
//
//		// 2. 3rd Gen Command Change Release Status
//		if (preset.getCi().startsWith("EMA")) {
//			QDeviceCommand table = QDeviceCommand.deviceCommand;
//			List<DeviceCommand> commands = BeanUtils.get(JPAQueryFactory.class) //
//					.selectFrom(table) //
//					.where(table.deviceId.eq(preset.getCi()) //
//							.and(table.command.eq("1")) //
//							.and(table.status.in(Arrays.asList("CREATED")))) //
//					.fetch();
//			if (!ObjectUtils.isEmpty(commands)) {
//				for (DeviceCommand command : commands) {
//					command.setStatus("RELEASED");
//				}
//				BeanUtils.get(DeviceCommandRepository.class).saveAll(commands);
//			}
//		}
	}

	public DeviceCommand getLastCommandByCounterCode(String ci) {
		QDeviceCommand table = QDeviceCommand.deviceCommand;
		DeviceCommand command = BeanUtils.get(JPAQueryFactory.class) //
				.selectFrom(table) //
				.where(table.deviceId.eq(ci) //
						.and(table.command.eq("1")) //
						.and(table.status.in(Arrays.asList("CREATED", "RELEASED")))) //
				.orderBy(table.id.desc(), table.createdAt.desc()) //
				.fetchFirst();
		return command;
	}

	public Preset getLastByCounterCode(String ci) {

		Preset preset = BeanUtils.get(PresetRepository.class).findFirstByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.READY);
		if (preset != null) {
			return preset;
		}

		preset = BeanUtils.get(PresetRepository.class).findFirstByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.APPLIED);
		if (!ObjectUtils.isEmpty(preset)) {
			return preset;
		}

		return null;
	}

	public Preset completeAllByCounterCode(String ci) {
		List<Preset> presets = BeanUtils.get(PresetRepository.class).findAllByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.READY);

		if (ObjectUtils.isEmpty(presets)) {
			return null;
		}

		Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(ci).orElse(null);
		if (counter == null) {
			return null;
		}

		// 1. Complete Process MoldMisconfigure 
		List<MoldMisconfigure> comfirmedPresetAlerts = BeanUtils.get(MoldMisconfigureRepository.class).findByCounterIdAndMisconfigureStatusAndLatest(counter.getId(),
				MisconfigureStatus.CONFIRMED, true);
		comfirmedPresetAlerts.forEach(x -> x.setLatest(false));
		BeanUtils.get(MoldMisconfigureRepository.class).saveAll(comfirmedPresetAlerts);

		List<MoldMisconfigure> moldMisconfigure = BeanUtils.get(MoldMisconfigureRepository.class).findByCounterIdAndMisconfigureStatusAndLatest(counter.getId(),
				MisconfigureStatus.MISCONFIGURED, true);
		if (moldMisconfigure != null && moldMisconfigure.size() > 0) {
			moldMisconfigure.get(0).setConfirmedAt(DateUtils2.getInstant());
			moldMisconfigure.get(0).setMisconfigureStatus(MisconfigureStatus.CONFIRMED);
			BeanUtils.get(MoldMisconfigureRepository.class).save(moldMisconfigure.get(0));
		}

		// 2. Complete Preset
		int i = 0;
		for (Preset item : presets) {
			item.setPresetStatus(i++ == 0 ? PresetStatus.APPLIED : PresetStatus.CANCELED);
			BeanUtils.get(PresetRepository.class).save(item);
		}
		Preset preset = presets.get(0);
		preset.setApplyDesc(preset.getApplyDesc() + "\\n[" + DateUtils2.getInstant().toString() + "]  Preset is applied at counter)");
		return preset;
	}

	// TODO: Refactoring (Alternative TransferService.makePresetMisconfigured)
	public void postAlert(Mold mold, Counter counter) {
		if (PresetStatus.READY == counter.getPresetStatus()) {
			List<Preset> presets = BeanUtils.get(PresetRepository.class).findAllByCiAndPresetStatusOrderByIdDesc(counter.getEquipmentCode(), PresetStatus.READY);

			if (presets.size() > 0) {
				Preset preset = presets.get(0);

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
				if (mold == null) {
					return;
				}

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

}
