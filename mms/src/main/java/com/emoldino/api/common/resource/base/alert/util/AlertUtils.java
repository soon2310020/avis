package com.emoldino.api.common.resource.base.alert.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.mold.MoldRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.PeriodType;
import saleson.model.Mold;
import saleson.model.QUserAlert;
import saleson.model.Terminal;

public class AlertUtils {

	public static List<Long> getUserIdsByMold(AlertType alertType, Long moldId) {
		LogicUtils.assertNotNull(alertType, "alertType");
		LogicUtils.assertNotNull(moldId, "moldId");

		Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
		if (mold == null) {
			return Collections.emptyList();
		}

		List<Long> companyIds2 = AccessControlUtils.getAllAccessibleCompanyIds(mold.getCompanyId());
		List<Long> companyIds = ValueUtils.equals(mold.getCompanyId(), mold.getSupplierCompanyId()) ? //
				companyIds2 //
				: AccessControlUtils.getAllAccessibleCompanyIds(mold.getSupplierCompanyId())//
						.stream()//
						.filter(companyIds2::contains)//
						.collect(Collectors.toList());

		return getUserIdsByCompanies(alertType, companyIds);
	}

	public static List<Long> getUserIdsByMoldAndPeriodType(AlertType alertType, Long moldId, PeriodType periodType) {
		LogicUtils.assertNotNull(alertType, "alertType");
		LogicUtils.assertNotNull(moldId, "moldId");

		Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
		if (mold == null) {
			return Collections.emptyList();
		}

		List<Long> companyIds2 = AccessControlUtils.getAllAccessibleCompanyIds(mold.getCompanyId());
		List<Long> companyIds = AccessControlUtils.getAllAccessibleCompanyIds(mold.getSupplierCompanyId())//
				.stream()//
				.filter(companyIds2::contains)//
				.collect(Collectors.toList());

		if (periodType == null) {
			return getUserIdsByCompanies(alertType, companyIds);
		} else {
			return getUserIdsByCompaniesAndPeriodType(alertType, companyIds, periodType);
		}
	}

	public static List<Long> getUserIdsByTerminal(AlertType alertType, Long terminalId) {
		LogicUtils.assertNotNull(alertType, "alertType");
		LogicUtils.assertNotNull(terminalId, "terminalId");

		Terminal terminal = BeanUtils.get(TerminalRepository.class).findById(terminalId).orElse(null);
		if (terminal == null) {
			return Collections.emptyList();
		}

		List<Long> companyIds = AccessControlUtils.getAllAccessibleCompanyIds(terminal.getCompanyId());

		return getUserIdsByCompanies(alertType, companyIds);
	}

	public static List<Long> getUserIdsByTerminalAndPeriodType(AlertType alertType, Long terminalId, PeriodType periodType) {
		LogicUtils.assertNotNull(alertType, "alertType");
		LogicUtils.assertNotNull(terminalId, "terminalId");

		Terminal terminal = BeanUtils.get(TerminalRepository.class).findById(terminalId).orElse(null);
		if (terminal == null) {
			return Collections.emptyList();
		}

		List<Long> companyIds = AccessControlUtils.getAllAccessibleCompanyIds(terminal.getCompanyId());

		if (periodType == null) {
			return getUserIdsByCompanies(alertType, companyIds);
		} else {
			return getUserIdsByCompaniesAndPeriodType(alertType, companyIds, periodType);
		}
	}

	private static List<Long> getUserIdsByCompanies(AlertType alertType, List<Long> companyIds) {
		if (ObjectUtils.isEmpty(companyIds)) {
			return new ArrayList<>();
		}

		QUserAlert table = QUserAlert.userAlert;
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(table.user.id)//
				.from(table)//
				.where(new BooleanBuilder()//
						.and(table.alertType.eq(alertType))//
						.and(table.email.isNull().or(table.email.isTrue()))//
						.and(//
								(table.user.admin.isTrue().and(table.user.enabled.isTrue()).and(table.user.deleted.isFalse()).and(table.user.company.isEmoldino.isTrue()))//
										.or(table.user.companyId.in(companyIds))//
						));
		return query.fetch();
	}

	private static List<Long> getUserIdsByCompaniesAndPeriodType(AlertType alertType, List<Long> companyIds, PeriodType periodType) {
		if (ObjectUtils.isEmpty(companyIds)) {
			return new ArrayList<>();
		}

		QUserAlert table = QUserAlert.userAlert;
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(table.user.id)//
				.from(table)//
				.where(new BooleanBuilder()//
						.and(table.alertType.eq(alertType))//
						.and(table.email.isNull().or(table.email.isTrue()))//
						.and(table.periodType.eq(periodType))//
						.and(//
								(table.user.admin.isTrue().and(table.user.enabled.isTrue()).and(table.user.deleted.isFalse()).and(table.user.company.isEmoldino.isTrue()))//
										.or(table.user.companyId.in(companyIds))//
						));
		return query.fetch();
	}

}
