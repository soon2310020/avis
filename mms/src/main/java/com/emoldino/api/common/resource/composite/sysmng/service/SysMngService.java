package com.emoldino.api.common.resource.composite.sysmng.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.jxls.common.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.client.util.ClientUtils;
import com.emoldino.api.common.resource.base.client.util.ClientUtils.ClientInfo;
import com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.OptionFieldValueRepository;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngDevice;
import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngGetOut;
import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngReportClientsOut;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.counter.CounterRepository;
import saleson.api.menu.MenuRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.Menu;
import saleson.model.QMenu;

@Service
public class SysMngService {

	public SysMngGetOut get() {
		SysMngGetOut output = new SysMngGetOut();

		output.setServerName(ConfigUtils.getServerName());
		output.setLicenseType(ConfigUtils.getLicenseType());

		// Terminals
		output.setTerminalCount(BeanUtils.get(TerminalRepository.class).count(//
				Q.terminal.enabled.isTrue()//
						.and(Q.terminal.equipmentStatus.notIn(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED))//
		));
		output.setTerminalConCount(BeanUtils.get(TerminalRepository.class).count(//
				Q.terminal.enabled.isTrue()//
						.and(Q.terminal.equipmentStatus.eq(EquipmentStatus.INSTALLED))//
						.and(Q.terminal.operatingStatus.eq(OperatingStatus.WORKING))//
		));
		output.setTerminalDisconCount(BeanUtils.get(TerminalRepository.class).count(//
				Q.terminal.enabled.isTrue()//
						.and(Q.terminal.equipmentStatus.notIn(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED, EquipmentStatus.AVAILABLE))//
						.and(Q.terminal.operatingStatus.isNull().or(Q.terminal.operatingStatus.notIn(OperatingStatus.WORKING)))//
		));
		output.setTerminalHoldCount(BeanUtils.get(TerminalRepository.class).count(//
				Q.terminal.enabled.isTrue()//
						.and(Q.terminal.equipmentStatus.isNull().or(Q.terminal.equipmentStatus.in(EquipmentStatus.AVAILABLE, EquipmentStatus.FAILURE)))//
		));
		output.setTerminalAbnormalCount(BeanUtils.get(TerminalRepository.class).count(//
				(Q.terminal.enabled.isFalse().or(Q.terminal.equipmentStatus.isNull()).or(Q.terminal.equipmentStatus.notIn(EquipmentStatus.INSTALLED)))//
						.and(Q.terminal.operatingStatus.eq(OperatingStatus.WORKING))//
		));

		// Sensors
		output.setSensorCount(BeanUtils.get(CounterRepository.class).count(//
				Q.counter.enabled.isTrue()//
						.and(Q.counter.equipmentStatus.notIn(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED))//
		));
		output.setSensorConCount(BeanUtils.get(CounterRepository.class).count(//
				Q.counter.enabled.isTrue()//
						.and(Q.counter.equipmentStatus.in(EquipmentStatus.INSTALLED, EquipmentStatus.DETACHED))//
						.and(Q.counter.operatingStatus.notIn(OperatingStatus.DISCONNECTED))//
		));
		output.setSensorDisconCount(BeanUtils.get(CounterRepository.class).count(//
				Q.counter.enabled.isTrue()//
						.and(Q.counter.equipmentStatus.notIn(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED, EquipmentStatus.AVAILABLE))//
						.and(Q.counter.operatingStatus.isNull().or(Q.counter.operatingStatus.eq(OperatingStatus.DISCONNECTED)))//
		));
		output.setSensorHoldCount(BeanUtils.get(CounterRepository.class).count(//
				Q.counter.enabled.isTrue()//
						.and(Q.counter.equipmentStatus.isNull().or(Q.counter.equipmentStatus.eq(EquipmentStatus.AVAILABLE)))//
		));
		output.setSensorAbnormalCount(BeanUtils.get(CounterRepository.class).count(//
				(Q.counter.enabled.isFalse().or(Q.counter.equipmentStatus.isNull()).or(Q.counter.equipmentStatus.notIn(EquipmentStatus.INSTALLED)))//
						.and(Q.counter.operatingStatus.eq(OperatingStatus.WORKING))//
		));

		BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.user.count())//
				.from(Q.user)//
				.groupBy(Q.user.email)//
				.fetch()//
				.forEach(count -> {
					if (count <= 1) {
						return;
					}
					output.setUserAbnormalCount(output.getPlantAbnormalCount() + (count - 1));
				});

		BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.location.count())//
				.from(Q.location)//
				.groupBy(Q.location.locationCode)//
				.fetch()//
				.forEach(count -> {
					if (count <= 1) {
						return;
					}
					output.setPlantAbnormalCount(output.getPlantAbnormalCount() + (count - 1));
				});

		Instant dbInstant = BeanUtils.get(OptionFieldValueRepository.class).getTime();
		Instant serverInstant = DateUtils2.newInstant();
		String serverTime = DateUtils2.format(serverInstant, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
		String serverTimeSys = DateUtils2.format(serverInstant, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
		String dbTime = DateUtils2.format(dbInstant, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
		String dbTimeSys = DateUtils2.format(dbInstant, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);

		output.setServerTime(serverTime);
		output.setServerTimeSys(serverTimeSys);
		output.setDbTime(dbTime);
		output.setDbTimeSys(dbTimeSys);
		output.setTimeZoneId(Zone.SYS);
		output.setOffset(ZoneId.of(Zone.SYS).getRules().getOffset(serverInstant).getId());
		output.setMinsDiffWithDbTime(minsDiff(serverInstant.toEpochMilli(), dbInstant.toEpochMilli()));

		if (!"central".equals(ConfigUtils.getServerName())) {
			if (output.getTerminalAbnormalCount() > 0) {
				BeanUtils.get(TerminalRepository.class).findAll(//
						(Q.terminal.enabled.isFalse().or(Q.terminal.equipmentStatus.notIn(EquipmentStatus.INSTALLED)))//
								.and(Q.terminal.operatingStatus.eq(OperatingStatus.WORKING)), //
						PageRequest.of(0, 100, Sort.by("equipmentStatus", "equipmentCode"))//
				).forEach(terminal -> output.addAbnormalTerminal(new SysMngDevice(//
						terminal.getEquipmentCode(), terminal.isEnabled(), terminal.getEquipmentStatus(), terminal.getOperatingStatus()//
				)));
			}
			if (output.getSensorAbnormalCount() > 0) {
				BeanUtils.get(CounterRepository.class).findAll(//
						(Q.counter.enabled.isFalse().or(Q.counter.equipmentStatus.notIn(EquipmentStatus.INSTALLED)))//
								.and(Q.counter.operatingStatus.eq(OperatingStatus.WORKING)), //
						PageRequest.of(0, 100, Sort.by("equipmentStatus", "equipmentCode"))//
				).forEach(counter -> output.addAbnormalSensor(new SysMngDevice(//
						counter.getEquipmentCode(), counter.isEnabled(), counter.getEquipmentStatus(), counter.getOperatingStatus()//
				)));
			}

			List<SysMngDevice> abnormalToolings = BeanUtils.get(JPAQueryFactory.class)//
					.select(Projections.constructor(SysMngDevice.class, //
							Q.mold.equipmentCode, //
							Q.mold.enabled, //
							Q.mold.equipmentStatus, //
							Q.mold.operatingStatus, //
							Q.counter.equipmentCode, //
							Q.counter.enabled, //
							Q.counter.equipmentStatus, //
							Q.counter.operatingStatus //
					))//
					.from(Q.mold)//
					.join(Q.counter).on(//
							Q.counter.id.eq(Q.mold.counterId)//
									.and(Q.counter.equipmentStatus.ne(Q.mold.equipmentStatus)//
											.or(Q.counter.operatingStatus.ne(Q.mold.operatingStatus))//
											.or(Q.counter.enabled.ne(Q.mold.enabled)))//
					)//
					.fetch();
			output.setAbnormalToolings(abnormalToolings);
			output.getAbnormalToolings().forEach(tooling -> tooling.setCode("?"));
		}

		return output;
	}

	public Map<String, SysMngGetOut> getClients() {
		if (!"central".equals(ConfigUtils.getServerName())) {
			return new LinkedHashMap<>();
		}

		Map<Integer, SysMngGetOut> map = new TreeMap<>();
		long[] time = { 0 };
		int[] is = { 0 };
		ClientUtils.getCodes().stream()//
				.filter(serverName -> ClientUtils.exists(serverName) && ClientUtils.get(serverName).isReportEnabled())//
				.forEach(serverName -> {
					int i = is[0]++;
					ClientInfo client = ClientUtils.get(serverName);
					String url = client.getUrl();
					SysMngGetOut reqout;
					try {
						reqout = HttpUtils.call(HttpMethod.GET, url + "/api/common/sys-mng", null, null, null, null, SysMngGetOut.class, null, 5000);
						if (time[0] == 0) {
							time[0] = DateUtils2.toInstant(reqout.getServerTime(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT).toEpochMilli();
						} else {
							long thisTime = DateUtils2.toInstant(reqout.getServerTime(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT).toEpochMilli();
							reqout.setMinsDiffWithFirstServer(minsDiff(thisTime, time[0]));
						}
					} catch (Exception e) {
						reqout = new SysMngGetOut();
						reqout.setErrorMessage(e.getLocalizedMessage());
					}
					if (ObjectUtils.isEmpty(reqout.getServerName())) {
						reqout.setServerName(serverName);
					} else {
						reqout.setServerNameMatched(ValueUtils.equals(serverName, reqout.getServerName()));
					}
					reqout.setClientName(client.getName());
					reqout.setClientUrl(client.getUrl());
					if (client.isEmoldino()) {
						reqout.setEmoldino(true);
					}
					map.put(i, reqout);
				});

		Map<String, SysMngGetOut> output = new LinkedHashMap<>();
		map.values().forEach(client -> output.put(client.getServerName(), client));
		return output;
	}

	public void exportClients(HttpServletResponse response) {
		SysMngReportClientsOut output = reportClients();
		Context context = new Context();
		TranUtils.doTran(() -> {
			context.putVar("output", output);
			context.putVar("content", output.getContent());
		});
		DatExpUtils.exportByJxls(//
				"CEN_SYS_RPT", //
				context, //
				"Clients Report", response//
		);
	}

	public SysMngReportClientsOut reportClients() {
		SysMngReportClientsOut output = new SysMngReportClientsOut();

		List<SysMngGetOut> clients = new ArrayList<>(getClients().values());
		clients.forEach(client -> {
			if (!ObjectUtils.isEmpty(client.getAbnormalTerminals())) {
				client.getAbnormalTerminals().forEach(device -> device.setClientName(client.getClientName()));
				output.getAbnormalTerminals().addAll(client.getAbnormalTerminals());
			}
			if (!ObjectUtils.isEmpty(client.getAbnormalSensors())) {
				client.getAbnormalSensors().forEach(device -> device.setClientName(client.getClientName()));
				output.getAbnormalSensors().addAll(client.getAbnormalSensors());
			}
			if (!ObjectUtils.isEmpty(client.getAbnormalToolings())) {
				client.getAbnormalToolings().forEach(device -> device.setClientName(client.getClientName()));
				output.getAbnormalToolings().addAll(client.getAbnormalToolings());
			}
			client.setAbnormalTerminals(null);
			client.setAbnormalSensors(null);
			client.setAbnormalToolings(null);
		});

//		BeanUtils.get(NotiEmailService.class).send(NotiEmailSendIn.builder()//
//				.from("eMoldino<noreply@emoldino.com>")//
////				.to(Arrays.asList(input.getTo()))//
////				.title(input.getTitle())//
////				.content(input.getContent())//
//				.subtype("html")//
//				.build());

		output.setContent(clients);
		return output;
	}

	private static int minsDiff(long thisTime, long time) {
		int value = ValueUtils.toInteger(thisTime - time, 0);
		int minsDiff = value / 60000;
		return Math.abs(minsDiff) > 1 ? minsDiff : 0;
	}

	@Autowired
	private MenuRepository repo;

	public String getMenuStr() {
		Map<Long, Menu> all = repo.findAll().stream().collect(Collectors.toMap(item -> item.getId(), item -> item));
		StringBuilder buf = new StringBuilder();
		buf.append(System.lineSeparator()).append("good");
		append(buf, null, all);
		buf.append(System.lineSeparator()).append("bad");
		all.values().forEach(item -> append(buf, null, item, null));
		System.out.println(buf.toString());
		return buf.toString();
	}

	private void append(StringBuilder buf, Menu parent, Map<Long, Menu> all) {
		QMenu table = QMenu.menu;
		BooleanBuilder builder = new BooleanBuilder();
		if (parent == null) {
			builder.and(table.level.eq(0));
		} else {
			builder.and(table.parent.in(parent));
		}
		repo.findAll(builder).forEach((item) -> {
			append(buf, parent, item, all);
			append(buf, item, all);
		});
	}

	private void append(StringBuilder buf, Menu parent, Menu item, Map<Long, Menu> all) {
		if (all != null) {
			all.remove(item.getId());
		}

		buf.append(System.lineSeparator());
		if (parent != null) {
			buf.append(parent.getLevel());
		}
		int depth = item.getLevel() + 1;
		for (int i = 0; i < depth; i++) {
			buf.append("\t");
		}
		buf.append(item.getId()).append(" ").append(item.getMenuName());
	}

}
