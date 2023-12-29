package com.emoldino.api.common.resource.composite.notbel.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.noti.dto.NotiEmailSendIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiGetIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiPostIn;
import com.emoldino.api.common.resource.base.noti.dto.NotiUserItem;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiCode;
import com.emoldino.api.common.resource.base.noti.enumeration.NotiStatus;
import com.emoldino.api.common.resource.base.noti.service.NotiService;
import com.emoldino.api.common.resource.base.noti.service.email.NotiEmailService;
import com.emoldino.api.common.resource.base.noti.util.NotiUtils;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelGetIn;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelGetOut;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelItem;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelPostEmailIn;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelPostOneIn;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.QueryResults;

import saleson.common.enumeration.ConfigCategory;
import saleson.common.util.SecurityUtils;

@Service
@Transactional
public class NotBelService {

	public NotBelGetOut get(NotBelGetIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new NotBelGetOut(Collections.emptyList(), pageable, 0);
		}

		boolean on = ValueUtils.toBoolean(OptionUtils.getUserFieldValue(ConfigCategory.NOTI_BELL, "on", null), true);

		QueryResults<NotiUserItem> results;
		{
			NotiGetIn reqin = new NotiGetIn();
			if (input.getNotiCategory() != null) {
				reqin.setNotiCategory(Arrays.asList(input.getNotiCategory()));
			}
			if (input.getNotiStatus() != null) {
				reqin.setNotiStatus(Arrays.asList(input.getNotiStatus()));
			}
			results = BeanUtils.get(NotiService.class).getBySession(reqin, pageable);
		}

		List<NotBelItem> content = results.getResults()//
				.stream()//
				.map(item -> new NotBelItem(item))//
				.collect(Collectors.toList());

		return new NotBelGetOut(content, pageable, results.getTotal(), on);
	}

	public void postOne(NotBelPostOneIn input) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		NotiUtils.post(input.getNotiCode(), //
				ValueUtils.map(input, NotiPostIn.class)//
						.addRecipient(userId)//
		);
	}

	public void postDummy(Long id, String code, String email) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}

//		NotiUtils.post(NotiCode.USER_ACCESS_REQUESTED, //
//				new NotiPostIn()//
//						.addRecipient(userId)//
//		);

//		NotiUtils.post(NotiCode.WO_CREATED, //
//				new NotiPostIn()//
//						.setNotiPriority(PriorityType.MEDIUM)//
//						.setDataId(id)//
//						.addRecipient(userId)//
//						.setParam("id", id)//
//						.setParam("workOrderId", "WO-001")//
//						.setParam("orderTypeName", "General Work Order")//
//						.setParam("orderTypeDesc", "a General Work Order")//
//						.setParam("senderCompanyName", "eMoldino")//
//		);

//		NotiCode notiCode = NotiCode.valueOf(code);
//		switch (notiCode) {
//		case TOOLING_UPTIME:
//			MoldEfficiency moldEfficiency = BeanUtils.get(MoldEfficiencyRepository.class).findAll().get(0);
//			BeanUtils.get(NotificationService.class).createAlertNotification(moldEfficiency, AlertType.EFFICIENCY, true, email);
//			break;
//		case TOOLING_CYCLE_TIME:
//			MoldCycleTime moldCycleTime = BeanUtils.get(MoldCycleTimeRepository.class).findAll().get(0);
//			BeanUtils.get(NotificationService.class).createAlertNotification(moldCycleTime, AlertType.CYCLE_TIME, true, email);
//			break;
//		case TOOLING_DISCONNECTED:
//			MoldDisconnect moldDisconnect = BeanUtils.get(MoldDisconnectRepository.class).findAll().get(0);
//			BeanUtils.get(NotificationService.class).createAlertNotification(moldDisconnect, AlertType.DISCONNECTED, true, email);
//			break;
//		case TERMINAL_DISCONNECTED:
//			TerminalDisconnect terminalDisconnect = BeanUtils.get(TerminalDisconnectRepository.class).findAll().get(0);
//			BeanUtils.get(NotificationService.class).createAlertNotification(terminalDisconnect, AlertType.TERMINAL_DISCONNECTED, true, email);
//			break;
//		case SENSOR_DETACHED:
//			MoldDetachment moldDetachment = BeanUtils.get(MoldDetachmentRepository.class).findAll().get(0);
//			BeanUtils.get(NotificationService.class).createAlertNotification(moldDetachment, AlertType.DETACHMENT, true, email);
//			break;
//		case TOOLING_RELOCATED:
//			MoldLocation moldLocation = BeanUtils.get(MoldLocationRepository.class).findAll().get(0);
//			BeanUtils.get(NotificationService.class).createAlertNotification(moldLocation, AlertType.RELOCATION, true, email);
//			break;
//		}

	}

	public void postEmail(NotBelPostEmailIn input) {
		ValueUtils.assertNotEmpty(input.getTitle(), "title");
		ValueUtils.assertNotEmpty(input.getTo(), "to");
		ValueUtils.assertNotEmpty(input.getContent(), "content");

		BeanUtils.get(NotiEmailService.class).send(NotiEmailSendIn.builder()//
				.from("eMoldino<noreply@emoldino.com>")//
				.to(Arrays.asList(input.getTo()))//
				.title(input.getTitle())//
				.content(input.getContent())//
				.subtype("html")//
				.build());

//		BeanUtils.get(NotiEmailService.class).send("eMoldino<noreply@emoldino.com>", Arrays.asList(input.getTo()), input.getTitle(), input.getContent(), "html");
	}

	public void turnOnOff(Boolean on) {
		BeanUtils.get(OptionService.class).saveUserFieldValue(ConfigCategory.NOTI_BELL, "on", on);
	}

	public void read(Long id) {
		NotiGetIn reqin = new NotiGetIn();
		reqin.setId(id);
		reqin.setNotiStatus(Arrays.asList(NotiStatus.UNREAD));
		List<NotiUserItem> items;
		while (!(items = BeanUtils.get(NotiService.class).getBySession(reqin, PageRequest.of(0, 100)).getResults()).isEmpty()) {
			items.forEach(item -> BeanUtils.get(NotiService.class).readBySession(item.getNotiRecipientId()));
		}
	}

	public void read(NotiCategory notiCategory) {
		NotiGetIn reqin = new NotiGetIn();
		if (notiCategory != null) {
			reqin.setNotiCategory(Arrays.asList(notiCategory));
		}
		reqin.setNotiStatus(Arrays.asList(NotiStatus.UNREAD));

		List<NotiUserItem> items;
		while (!(items = TranUtils.doNewTran(() -> BeanUtils.get(NotiService.class).getBySession(reqin, PageRequest.of(0, 100))).getResults()).isEmpty()) {
			items.forEach(item -> TranUtils.doNewTran(() -> BeanUtils.get(NotiService.class).readBySession(item.getNotiRecipientId())));
		}
	}

}
