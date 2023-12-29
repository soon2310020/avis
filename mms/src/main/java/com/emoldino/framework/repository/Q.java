package com.emoldino.framework.repository;

import java.util.List;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.QCycleTimeDeviation;
import com.emoldino.api.analysis.resource.base.data.repository.cycletimefluctuation.QCycleTimeFluctuation;
import com.emoldino.api.analysis.resource.base.data.repository.data.QData;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.moldprocchg.QMoldProcChg;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.QStatisticsExt;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.QMoldPmPlan;
import com.emoldino.api.common.resource.base.location.repository.area.QArea;
import com.emoldino.api.supplychain.resource.base.product.repository.partdemand.QPartDemand;
import com.emoldino.api.supplychain.resource.base.product.repository.partplan.QPartPlan;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.QPartStat;
import com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat.QProdMoldStat;
import com.emoldino.api.supplychain.resource.base.product.repository.productdemand.QProductDemand;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.QProductStat;

import saleson.common.enumeration.CompanyType;
import saleson.model.QCategory;
import saleson.model.QCdata;
import saleson.model.QCompany;
import saleson.model.QCounter;
import saleson.model.QDowntimeItem;
import saleson.model.QLocation;
import saleson.model.QLogUserAlert;
import saleson.model.QMachine;
import saleson.model.QMachineDowntimeAlert;
import saleson.model.QMachineDowntimeReason;
import saleson.model.QMachineMoldMatchingHistory;
import saleson.model.QMachineOee;
import saleson.model.QMachineStatistics;
import saleson.model.QMold;
import saleson.model.QMoldCycleTime;
import saleson.model.QMoldDataSubmission;
import saleson.model.QMoldDetachment;
import saleson.model.QMoldDisconnect;
import saleson.model.QMoldEfficiency;
import saleson.model.QMoldLocation;
import saleson.model.QMoldMaintenance;
import saleson.model.QMoldMisconfigure;
import saleson.model.QMoldPart;
import saleson.model.QMoldRefurbishment;
import saleson.model.QPart;
import saleson.model.QStatistics;
import saleson.model.QStatisticsPart;
import saleson.model.QTdata;
import saleson.model.QTerminal;
import saleson.model.QTerminalDisconnect;
import saleson.model.QTransfer;
import saleson.model.QUser;
import saleson.model.QUserAlert;
import saleson.model.QWorkOrder;
import saleson.model.QWorkOrderAsset;
import saleson.model.QWorkOrderUser;

public class Q {
	public static final QCompany company = QCompany.company;
	public static final QCompany inhouse = new QCompany("inhouse");
	public static final QCompany supplier = new QCompany("supplier");
	public static final QCompany toolmaker = new QCompany("toolmaker");
	// TODO manufacturer
//	public static final QCompany manufacturer = new QCompany("manufacturer");

//	public static QCompany company(CompanyType... companyType) {
//		QCompany qCompany;
//		if (ObjectUtils.isEmpty(companyType)) {
//			qCompany = Q.company;
//		} else {
//			qCompany = company(Arrays.asList(companyType));
//		}
//		return qCompany;
//	}

	public static QCompany company(List<CompanyType> companyType) {
		QCompany qCompany;
		if (ObjectUtils.isEmpty(companyType)) {
			qCompany = Q.company;
		} else {
			boolean inHouse = companyType.contains(CompanyType.IN_HOUSE);
			boolean supplier = companyType.contains(CompanyType.SUPPLIER);
			boolean toolmaker = companyType.contains(CompanyType.TOOL_MAKER);
			if (inHouse && supplier && toolmaker) {
				qCompany = Q.company;
			} else if (inHouse && supplier) {
				// TODO manufacturer
//				qCompany = Q.manufacturer;
				qCompany = Q.supplier;
			} else if (toolmaker) {
				qCompany = Q.toolmaker;
			} else if (supplier) {
				qCompany = Q.supplier;
			} else if (inHouse) {
				qCompany = Q.inhouse;
			} else {
				qCompany = Q.company;
			}
		}
		return qCompany;
	}

	public static final QUser user = QUser.user;

	public static final QLocation location = QLocation.location;
	public static final QArea area = QArea.area;

	public static final QCategory category = QCategory.category;
	public static final QCategory brand = new QCategory("brand");
	public static final QCategory product = new QCategory("product");
	public static final QPart part = QPart.part;

	public static final QMold mold = QMold.mold;
	public static final QMoldPmPlan moldPmPlan = QMoldPmPlan.moldPmPlan;
	public static final QMachine machine = QMachine.machine;

	public static final QMoldPart moldPart = QMoldPart.moldPart;

	public static final QTerminal terminal = QTerminal.terminal;
	public static final QCounter counter = QCounter.counter;

	public static final QData data = QData.data;
	public static final QDataCounter dataCounter = QDataCounter.dataCounter;
	public static final QDataAcceleration dataAcceleration = QDataAcceleration.dataAcceleration;

	public static final QTransfer transfer = QTransfer.transfer;
	public static final QTdata tdata = QTdata.tdata;
	public static final QCdata cdata = QCdata.cdata;
	public static final QStatistics statistics = QStatistics.statistics;
	public static final QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
	public static final QStatisticsExt statisticsExt = QStatisticsExt.statisticsExt;
	public static final QProdMoldStat prodMoldStat = QProdMoldStat.prodMoldStat;
	public static final QMoldProcChg moldProcChg = QMoldProcChg.moldProcChg;

	public static final QProductDemand productDemand = QProductDemand.productDemand;
	public static final QPartDemand partDemand = QPartDemand.partDemand;
	public static final QPartPlan partPlan = QPartPlan.partPlan;
	public static final QProductStat productStat = QProductStat.productStat;
	public static final QPartStat partStat = QPartStat.partStat;

	public static final QWorkOrder workOrder = QWorkOrder.workOrder;
	public static final QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
	public static final QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;

	public static final QMachineOee machineOee = QMachineOee.machineOee;
	public static final QMachineMoldMatchingHistory machineMoldMatchingHistory = QMachineMoldMatchingHistory.machineMoldMatchingHistory;
	public static final QMachineDowntimeReason machineDowntimeReason = QMachineDowntimeReason.machineDowntimeReason;
	public static final QDowntimeItem downtimeItem = QDowntimeItem.downtimeItem;
	public static final QMachineStatistics machineStatistics = QMachineStatistics.machineStatistics;

	public static final QUserAlert userAlert = QUserAlert.userAlert;
	public static final QLogUserAlert logUserAlert = QLogUserAlert.logUserAlert;

	public static final QTerminalDisconnect terminalDisconnect = QTerminalDisconnect.terminalDisconnect;
	public static final QMoldDisconnect moldDisconnect = QMoldDisconnect.moldDisconnect;
	public static final QMoldDetachment moldDetachment = QMoldDetachment.moldDetachment;
	public static final QMoldMisconfigure moldMisconfigure = QMoldMisconfigure.moldMisconfigure;
	public static final QMoldDataSubmission moldDataSubmission = QMoldDataSubmission.moldDataSubmission;

	public static final QMoldLocation moldLocation = QMoldLocation.moldLocation;
	public static final QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
	public static final QMoldRefurbishment moldRefurbishment = QMoldRefurbishment.moldRefurbishment;

	public static final QMachineDowntimeAlert machineDowntimeAlert = QMachineDowntimeAlert.machineDowntimeAlert;
	public static final QMoldCycleTime moldCycleTime = QMoldCycleTime.moldCycleTime;
	public static final QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;

	public static final QCycleTimeDeviation cycleTimeDeviation = QCycleTimeDeviation.cycleTimeDeviation;
	public static final QCycleTimeDeviation ctd = QCycleTimeDeviation.cycleTimeDeviation;
	public static final QCycleTimeFluctuation cycleTimeFluctuation = QCycleTimeFluctuation.cycleTimeFluctuation;
	public static final QCycleTimeFluctuation ctf = QCycleTimeFluctuation.cycleTimeFluctuation;
}
