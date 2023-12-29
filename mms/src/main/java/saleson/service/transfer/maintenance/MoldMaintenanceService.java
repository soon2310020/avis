package saleson.service.transfer.maintenance;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlan;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlanRepository;
import com.emoldino.framework.util.BeanUtils;

import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldMaintenanceRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.workOrder.WorkOrderService;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.MoldMaintenance;
import saleson.model.Statistics;

@Service
public class MoldMaintenanceService {

	@Autowired
	private CounterRepository counterRepository;

	@Autowired
	private MoldRepository moldRepository;

	@Autowired
	private MoldMaintenanceRepository moldMaintenanceRepository;

	@Lazy
	@Autowired
	private MoldService moldService;

	public void proc(Statistics statistics) {
		if (statistics == null || statistics.getMoldId() == null || statistics.getCi() == null) {
			return;
		}

		Mold mold = moldService.findByIdWithoutAdditionalInfo(statistics.getMoldId());
		if (mold == null) {
			return;
		}
		MoldPmPlan pmPlan = BeanUtils.get(MoldPmPlanRepository.class).findByMoldId(mold.getId()).orElse(null);
		if (pmPlan != null && pmPlan.getPmStrategy() == PM_STRATEGY.TIME_BASED) {
			return;
		}

		Counter counter = counterRepository.findByEquipmentCode(statistics.getCi()).orElse(null);
		if (counter == null) {
			return;
		}

		//filter last maintenance

		Integer scLastMaintenance = moldService.getScLastMaintenance(mold.getId());

		// Update 05-10-2020: maintenance confirmed doesn't matter
		int moldLastShot = mold.getLastShot();
		int numShotFromMaintenance = (moldLastShot - scLastMaintenance);
		int maintenanceCount = numShotFromMaintenance / mold.getPreventCycle();

		//for over period
		int periodOverdue = 0;
		if (maintenanceCount > 0 && numShotFromMaintenance % mold.getPreventCycle() == 0 ) {
			maintenanceCount--;
		}
		int period = mold.getPreventCycle() * (maintenanceCount + 1) + scLastMaintenance;
		int periodStart = period - mold.getPreventUpcoming();
		int periodEnd = period + periodOverdue;
		boolean isMaintenanced = mold.getMaintenanced();

		// 정비대상
		List<MoldMaintenance> lastMoldMaintenanceList = moldMaintenanceRepository.findAllByMoldIdAndMaintenanceStatusIsInAndLatestOrderByIdDesc(mold.getId(),
				Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);

		// 예방정비 기간 (UPCOMMING)
		if (moldLastShot >= periodStart && moldLastShot <= periodEnd) {

			boolean existsOverdue = false;
			boolean isNewAlertCycle = true; // check if all previous alerts were done
			MoldMaintenance dummyMoldMaintenance = null;
			if (!isMaintenanced || !lastMoldMaintenanceList.isEmpty()) {
				if (lastMoldMaintenanceList != null && lastMoldMaintenanceList.size() > 0) {
					isNewAlertCycle = false;
					if (lastMoldMaintenanceList.get(0).getMaintenanceStatus().equals(MaintenanceStatus.UPCOMING))
						if (lastMoldMaintenanceList.get(0).getPeriodStart() == periodStart) {
							dummyMoldMaintenance = lastMoldMaintenanceList.get(0);
							lastMoldMaintenanceList.remove(0);
						} else {
							isNewAlertCycle = true;
						}
					else {
						existsOverdue = true;
					}
				}
				lastMoldMaintenanceList.stream().forEach(x -> x.setLatest(false));
				moldMaintenanceRepository.saveAll(lastMoldMaintenanceList);
			}

			if (!isMaintenanced || existsOverdue || isNewAlertCycle) {

				if (dummyMoldMaintenance != null) {
					dummyMoldMaintenance.setLatest(false);
					moldMaintenanceRepository.save(dummyMoldMaintenance);
				}

				mold.setMaintenanced(true);
				// 정비 내역 등록 .
				MoldMaintenance maintenance = new MoldMaintenance();
				maintenance.setMold(mold);
				maintenance.setAccumulatedShot(moldLastShot);
				maintenance.setMaintenanceStatus(MaintenanceStatus.UPCOMING);
				maintenance.setPreventCycle(mold.getPreventCycle());
				maintenance.setPreventUpcoming(mold.getPreventUpcoming());
				maintenance.setPeriodStart(periodStart);
				maintenance.setPeriodEnd(periodEnd);
				maintenance.setLatest(true);

				moldMaintenanceRepository.save(maintenance);
				moldService.procMaintenanceDueDate(maintenance, scLastMaintenance);
				if (EquipmentStatus.INSTALLED.equals(mold.getEquipmentStatus()))
					BeanUtils.get(WorkOrderService.class).generateWorkOrder(maintenance);
			}

		} else if (isMaintenanced && moldLastShot > (periodEnd - mold.getPreventCycle()) && moldLastShot < (periodStart)) { // 예방정비 기간 지남 (OVERDUE)
			MoldMaintenance maintenance = new MoldMaintenance();
			boolean existsUpcoming = false;
			if (lastMoldMaintenanceList != null && lastMoldMaintenanceList.size() > 0) {
				if (lastMoldMaintenanceList.get(0).getMaintenanceStatus().equals(MaintenanceStatus.OVERDUE)) {
					lastMoldMaintenanceList.remove(0);
				} else {
					maintenance = lastMoldMaintenanceList.get(0);
					existsUpcoming = true;
				}
			}
			lastMoldMaintenanceList.stream().forEach(x -> x.setLatest(false));
			moldMaintenanceRepository.saveAll(lastMoldMaintenanceList);
			if (existsUpcoming) {
				//change status

				maintenance.setMold(mold);
				maintenance.setMaintenanceStatus(MaintenanceStatus.OVERDUE);
				maintenance.setPreventCycle(mold.getPreventCycle());
				maintenance.setPreventUpcoming(mold.getPreventUpcoming());
				maintenance.setPeriodStart(periodStart - mold.getPreventCycle());
				maintenance.setPeriodEnd(periodEnd - mold.getPreventCycle());
				maintenance.setShotCount(periodStart - mold.getPreventCycle() + mold.getPreventUpcoming());
				maintenance.setLatest(true);
				maintenance.setOverdueTime(Instant.now());

				moldMaintenanceRepository.save(maintenance);
				moldService.procMaintenanceDueDate(maintenance, scLastMaintenance);




			}
		}

		// Calculate and save last shot made from last period
		List<MoldMaintenance> finalLastMoldMaintenanceList = moldMaintenanceRepository.findAllByMoldIdAndMaintenanceStatusIsInAndLatestOrderByIdDesc(mold.getId(),
				Arrays.asList(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE), true);
		if (finalLastMoldMaintenanceList != null && finalLastMoldMaintenanceList.size() > 0) {
			MoldMaintenance finalLastMoldMaintenance = finalLastMoldMaintenanceList.get(0);

			Integer finalPeriod = 0;
			if (finalLastMoldMaintenance.getPeriodStart() != null && finalLastMoldMaintenance.getMold().getPreventUpcoming() != null)
				finalPeriod = finalLastMoldMaintenance.getPeriodStart() + finalLastMoldMaintenance.getMold().getPreventUpcoming();
			else if (finalLastMoldMaintenance.getPeriodEnd() != null && finalLastMoldMaintenance.getMold().getPreventOverdue() != null)
				finalPeriod = finalLastMoldMaintenance.getPeriodEnd() - finalLastMoldMaintenance.getMold().getPreventOverdue();

			if (finalLastMoldMaintenance.getMold().getLastShot() != null) {
				Integer untilNextPM = finalLastMoldMaintenance.getMold().getLastShot() - finalPeriod + finalLastMoldMaintenance.getMold().getPreventCycle();
				finalLastMoldMaintenance.setLastShotMade(untilNextPM % finalLastMoldMaintenance.getMold().getPreventCycle());
			} else {
				finalLastMoldMaintenance.setLastShotMade(0);
			}
			moldMaintenanceRepository.save(finalLastMoldMaintenance);
		}
		moldRepository.save(mold);
	}

	/**
	 * 예방 정비 Alert Event
	 *
	 * @param maintenance
	 */
	private void publishMaintenanceAlertEvent(MoldMaintenance maintenance) {
		/*AlertEvent event = new MaintenanceAlertEvent(maintenance);
		applicationEventPublisher.publishEvent(event);
		log.info("[ALERT EVENT] Maintenance Alert!!");*/
	}

}
