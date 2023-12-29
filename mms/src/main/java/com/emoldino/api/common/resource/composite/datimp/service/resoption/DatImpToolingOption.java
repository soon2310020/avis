package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import static saleson.service.util.NumberUtils.assertNumberOrNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.composite.datimp.service.enumeration.MaintenanceIntervalType;
import com.emoldino.api.common.resource.composite.datimp.service.enumeration.MonthDays;
import com.emoldino.framework.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_FREQUENCY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.RECURR_CONSTRAINT_TYPE;
import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.mold.MoldController;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.user.UserRepository;
import saleson.common.enumeration.DayOfWeek;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.model.Mold;
import saleson.model.Part;
import saleson.model.User;

@Getter
@NoArgsConstructor
public class DatImpToolingOption implements ResOptionGetter<MoldPayload> {

	// TODO @duytctw move, should create a separate Enum
/*
	@AllArgsConstructor
	@Getter
	enum MAINTENANCE_INTERVAL_TYPE {
		SHOT_BASE("Shot Based"), WEEKLY_TIME_BASED("Weekly Time Based"), MONTLY_TIME_BASED("Monthly Time Based");

		private final String title;
	}

	@AllArgsConstructor
	@Getter
	enum MONTH_DAYS {
		FIRST("1st"), SECOND("2nd"), THIRD("3rd"), FORTH("4th");

		private final String title;
	}
 */

	private DatImpResourceType resourceType = DatImpResourceType.TOOLING;
	private ResOption<MoldPayload> resOption = new ResOption<MoldPayload>(//
			// 1. Sheet Name
			Arrays.asList("Tooling"), //
			// 2. Object Type
			ObjectType.TOOLING, //
			// 3. Item Class
			MoldPayload.class, //
			// 4. Code Field
			Arrays.asList("equipmentCode"),
			// 5. Populate
			item -> populate(item),
			// 6. Before Logic
			item -> doBefore(item), //
			// 7. Exists Logic
			item -> exists(item), //
			// 8. Post Logic
			item -> post(item), //
			// 9. Put Logic
			item -> put(item)//
	);

	private void populate(MoldPayload item) {
		if (ObjectUtils.isEmpty(item.getEquipmentCode())) {
			return;
		}
		Mold mold = BeanUtils.get(MoldRepository.class).findByEquipmentCode(item.getEquipmentCode());
		if (mold == null) {
			return;
		}
		item.populate(mold);
	}

	private void doBefore(MoldPayload item) {
		// toolingType
		if (!ObjectUtils.isEmpty(item.getToolingType())) {
			if ("사출".equals(item.getToolingType())) {
				item.setToolingType("INJECTION_MOLD");
			} else if ("프레스".equals(item.getToolingType())) {
				item.setToolingType("STAMPING");
			}
		}

		// counterId
		item.setCounterId(DatImpUtils.getSensorId(item.getCounterCode(), "counterCode", item.getCounterId()));

		// locationId
		item.setLocationId(DatImpUtils.getLocationId(item.getLocationCode(), "locationCode", item.getLocationId()));

		// supplierCompanyId
		item.setSupplierCompanyId(DatImpUtils.getCompanyId(item.getSupplierCompanyCode(), "supplierCompanyCode", item.getSupplierCompanyId()));

		// engineerEmailsStr -> engineerIds
		if (StringUtils.isNotBlank(item.getEngineerEmailsStr())) {
			List<String> engineerEmails = Arrays.asList(item.getEngineerEmailsStr().split(","));
			Set<User> engineerSet = new HashSet<>();
			engineerEmails.stream().forEach(email -> {
				if (StringUtils.isEmpty(email.trim())) {
					return;
				}
				User user = BeanUtils.get(UserRepository.class)//
						.findByEmailAndDeletedIsFalse(email.trim())//
						.orElseThrow(() -> DataUtils.newDataNotFoundException(User.class, MessageUtils.get("engineer_in_charge", null), email));
				engineerSet.add(user);
			});
			if (!engineerSet.isEmpty()) {
				item.setEngineerIds(engineerSet.stream().map(e -> e.getId()).collect(Collectors.toList()));
			}
		}

		// plantEngineerEmailsStr -> plantEngineerIds
		if (StringUtils.isNotBlank(item.getPlantEngineerEmailsStr())) {
			List<String> plantEngineerEmails = Arrays.asList(item.getPlantEngineerEmailsStr().split(","));
			Set<User> plantEngineerSet = new HashSet<>();
			plantEngineerEmails.stream().forEach(email -> {
				if (StringUtils.isEmpty(email.trim())) {
					return;
				}
				User user = BeanUtils.get(UserRepository.class)//
						.findByEmailAndDeletedIsFalse(email.trim())//
						.orElseThrow(() -> DataUtils.newDataNotFoundException(User.class, MessageUtils.get("plant_engineer_in_charge", null), email));
				plantEngineerSet.add(user);
			});
			if (!plantEngineerSet.isEmpty()) {
				item.setPlantEngineerIds(plantEngineerSet.stream().map(User::getId).collect(Collectors.toList()));
			}
		}

		if (item.getToolmakerContractedCycleTimeSec() != null) {
			item.setToolmakerContractedCycleTime(ValueUtils.toInteger(item.getToolmakerContractedCycleTimeSec() * 10d, 0));
		}
		if (item.getContractedCycleTimeSec() != null) {
			item.setContractedCycleTime(ValueUtils.toInteger(item.getContractedCycleTimeSec() * 10d, 0));
		}

		// TODO @viet where is this used?
		// toSize -> size
		/**
		 * Because in the database the column size is store in the format as "w x L x H".
		 * But in the import file and the input form, the user enters independent fields.
		 * input: sizeW, sizeL, sizeH
		 * output size = sizeW + " x " + sizeL + " x " + sizeH
		 */
		assertNumberOrNull("tooling", item.getSizeW(), "Tool Size (W)");
		assertNumberOrNull("tooling", item.getSizeL(), "Tool Size (L)");
		assertNumberOrNull("tooling", item.getSizeH(), "Tool Size (H)");
		/*
		if (!NumberUtils.isNumberOrNull(item.getSizeW()))
			throw DataUtils.newDataValueInvalidException("tooling", "Tool Size (W)", item.getSizeW());
		if (!NumberUtils.isNumberOrNull(item.getSizeL()))
			throw DataUtils.newDataValueInvalidException("tooling", "Tool Size (L)", item.getSizeL());
		if (!NumberUtils.isNumberOrNull(item.getSizeH()))
			throw DataUtils.newDataValueInvalidException("tooling", "Tool Size (H)", item.getSizeH());
		*/

		item.setSize(item.toSize());

		if (StringUtils.isNotBlank(item.getSizeUnitTitle())) {
			SizeUnit enumItem = Arrays.stream(SizeUnit.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(item.getSizeUnitTitle()))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "Size Unit", item.getSizeUnitTitle()));
			item.setSizeUnit(enumItem);
		}
		assertNumberOrNull("tooling", item.getWeight(), "tool_weight");
		/*
				if (!NumberUtils.isNumberOrNull(item.getWeight()))
					throw DataUtils.newDataValueInvalidException("tooling", "tool_weight", item.getWeight());
		*/
		if (StringUtils.isNotBlank(item.getWeightUnitTitle())) {
			WeightUnit enumItem = Arrays.stream(WeightUnit.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(item.getWeightUnitTitle()))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "Weight Unit", item.getWeightUnitTitle()));
			item.setWeightUnit(enumItem);
		}

		// toolMakerCompanyId
		item.setToolMakerCompanyId(DatImpUtils.getCompanyId(item.getToolMakerCompanyCode(), "toolMakerCompanyCode", item.getToolMakerCompanyId()));

		// supplierCompanyId
		/*
		item.setSupplierCompanyId(
				DatImpUtils.getCompanyId(item.getSupplierCompanyName(), "companyName", item.getSupplierCompanyCode(), "companyCode", item.getSupplierCompanyId()));
		*/

		// cycleTimeLimit1Unit
		if (StringUtils.isNotBlank(item.getCycleTimeLimit1UnitTitle())) {
			if ("초".equals(item.getCycleTimeLimit1UnitTitle())) {
				item.setCycleTimeLimit1UnitTitle("second");
			}
			OutsideUnit enumItem = Arrays.stream(OutsideUnit.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(OutsideUnit.convertToTitle(item.getCycleTimeLimit1UnitTitle())))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "Cycle Time Tolerance L1 Unit", item.getCycleTimeLimit1UnitTitle()));
			item.setCycleTimeLimit1Unit(enumItem);
		}

		// cycleTimeLimit2Unit
		if (StringUtils.isNotBlank(item.getCycleTimeLimit2UnitTitle())) {
			if ("초".equals(item.getCycleTimeLimit2UnitTitle())) {
				item.setCycleTimeLimit2UnitTitle("second");
			}
			OutsideUnit enumItem = Arrays.stream(OutsideUnit.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(OutsideUnit.convertToTitle(item.getCycleTimeLimit2UnitTitle())))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "Cycle Time Tolerance L2 Unit", item.getCycleTimeLimit2UnitTitle()));
			item.setCycleTimeLimit2Unit(enumItem);
		}

		Mold mold = BeanUtils.get(MoldRepository.class)//
				.findFirstByEquipmentCode(item.getEquipmentCode())//
				.orElse(null);

		// moldParts
		if (!ObjectUtils.isEmpty(item.getMoldParts())) {
			Arrays.stream(item.getMoldParts()).forEach(moldPart -> {
				moldPart.setTotalCavities(item.getTotalCavities());
				if (moldPart.getCavity() == null) {
					moldPart.setCavity(0);
				}
				/*
				// TODO @viet Why don't we put these below validations into post and put mold logic?
				{
					if (moldPart.getCavity() > item.getTotalCavities()) {
						throw new BizException("less_than_or_equal_to", Mold.class.getSimpleName(), new Property("field1", MessageUtils.get("working_cavities", null)),
								new Property("field2", MessageUtils.get("total_number_of_cavities", null)));
					}
				}
				*/

				Part part = DatImpUtils.getPart(moldPart.getPartName(), "part_name", moldPart.getPartCode(), "part_id", moldPart.getPartId(), "part_id");
				if (part != null) {
					moldPart.setPartId(part.getId());
				}
				moldPart.setPart(part);
			});
		} else {
			item.setMoldParts(null);
		}

		// TODO @viet Why don't we put these below validations into post and put mold logic? => I moved them.

		if (StringUtils.isBlank(item.getShiftsPerDay())) {
			if (mold != null) {
				item.setShiftsPerDay(mold.getShiftsPerDay());
			} else {
				item.setShiftsPerDay("24");
			}
		}
		if (StringUtils.isBlank(item.getProductionDays())) {
			if (mold != null) {
				item.setProductionDays(mold.getProductionDays());
			} else {
				item.setProductionDays("7");
			}
		}

		// time-based PM
		if (StringUtils.isBlank(item.getMaintenanceIntervalType())) {
			item.setMaintenanceIntervalType(MaintenanceIntervalType.SHOT_BASED.getTitle());
		}
		MaintenanceIntervalType maintenanceIntervalType = Arrays.stream(MaintenanceIntervalType.values())//
				.filter(t -> t.getTitle().equalsIgnoreCase(item.getMaintenanceIntervalType()))//
				.findFirst()//
				.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "Maintenance Interval Type", item.getMaintenanceIntervalType()));

		if (maintenanceIntervalType.equals(MaintenanceIntervalType.SHOT_BASED)) {
			item.setPmStrategy(PM_STRATEGY.SHOT_BASED);
			setValueIn(item,"preventCycle",item.getPreventCycleIn(),"maintenance_interval");
			setValueIn(item,"preventUpcoming",item.getPreventUpcomingIn(),"upcoming_maintenance_tolerance");

		} else {
			item.setPmStrategy(PM_STRATEGY.TIME_BASED);
			if(item.getPreventCycle() == null) item.setPreventCycle(50000);// default
			if(item.getPreventUpcoming() == null) item.setPreventUpcoming(10000);// default
			if (maintenanceIntervalType.equals(MaintenanceIntervalType.WEEKLY_TIME_BASED)) {
				item.setPmFrequency(PM_FREQUENCY.WEEKLY);
				setValueIn(item,"schedInterval",item.getSchedIntervalWeek(),"Schedule every (week(s))");

				if (StringUtils.isNotBlank(item.getSchedDayOfWeekTitle())) {
					DayOfWeek schedDayOfWeek = Arrays.stream(DayOfWeek.values())//
							.filter(t -> t.getName().equalsIgnoreCase(item.getSchedDayOfWeekTitle()))//
							.findFirst()//
							.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "On Date (Monday, Tuesday, etc)", item.getSchedDayOfWeekTitle()));

					item.setSchedDayOfWeek(schedDayOfWeek);
				}
				//schedStartDate
				if(StringUtils.isNotBlank(item.getSchedStartDateWeek())){
					item.setSchedStartDate(item.getSchedStartDateWeek());
				}

			} else if (maintenanceIntervalType.equals(MaintenanceIntervalType.MONTHLY_TIME_BASED)) {
				item.setPmFrequency(PM_FREQUENCY.MONTHLY);
				setValueIn(item,"schedInterval",item.getSchedIntervalMonthly(),"Schedule every (month(s))");

				if (StringUtils.isNotBlank(item.getSchedOrdinalNumTitle())) {
					MonthDays schedOrdinalNum = Arrays.stream(MonthDays.values())//
							.filter(t -> t.getTitle().equalsIgnoreCase(item.getSchedOrdinalNumTitle()))//
							.findFirst()//
							.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "On (1st ,2nd,etc.)", item.getSchedOrdinalNumTitle()));
					item.setSchedOrdinalNum(schedOrdinalNum.ordinal() + 1);
				}
				if (StringUtils.isNotBlank(item.getSchedDayOfWeekMonthlyTitle())) {
					DayOfWeek schedDayOfWeek = Arrays.stream(DayOfWeek.values())//
							.filter(t -> t.getName().equalsIgnoreCase(item.getSchedDayOfWeekMonthlyTitle()))//
							.findFirst()//
							.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "Date (Monday, Tuesday, etc)", item.getSchedDayOfWeekMonthlyTitle()));

					item.setSchedDayOfWeek(schedDayOfWeek);
				}
				//schedStartDate
				if (StringUtils.isNotBlank(item.getSchedStartDateMonthly())) {
					item.setSchedStartDate(item.getSchedStartDateMonthly());
				}
			}

			if (StringUtils.isBlank(item.getForever()) || item.getForever().equalsIgnoreCase("false")) {
				if (StringUtils.isNotBlank(item.getRecurrNumIn())) {
					item.setRecurrConstraintType(RECURR_CONSTRAINT_TYPE.FOR);
				} else if (StringUtils.isNotBlank(item.getRecurrDueDateIn())) {
					item.setRecurrConstraintType(RECURR_CONSTRAINT_TYPE.UNTIL);
				}else{
					item.setRecurrConstraintType(RECURR_CONSTRAINT_TYPE.FOR);
				}
			} else {
				item.setRecurrConstraintType(RECURR_CONSTRAINT_TYPE.FOREVER);
			}
			//get value
			if(item.getRecurrConstraintType().equals(RECURR_CONSTRAINT_TYPE.FOR)){
				setValueIn(item,"recurrNum",item.getRecurrNumIn(),"For (number of times)");
			}else if(item.getRecurrConstraintType().equals(RECURR_CONSTRAINT_TYPE.UNTIL)){
				item.setRecurrDueDate(item.getRecurrDueDateIn());
			}
			setValueIn(item,"schedUpcomingTolerance",item.getSchedUpcomingToleranceIn(),"Upcoming Maintenance Tolerance (only for Time Based)");

		}


		if (item.getPreventUpcoming() == null && item.getPreventCycle() != null && !item.getPreventCycle().equals(0)) {
			item.setPreventUpcoming(item.getPreventCycle() / 10);
		}


	}

	private boolean exists(MoldPayload item) {
		return BeanUtils.get(MoldService.class).existsByEquipmentCode(item.getEquipmentCode(), null);
	}

	private void post(MoldPayload item) {
		ResponseEntity<?> response = BeanUtils.get(MoldController.class).newMold(item);
		if (response.getBody() instanceof Mold && response.getBody() != null) {
			item.setId(((Mold) response.getBody()).getId());//return id for save custom field
		}
		DatImpUtils.response(response);
	}

	private void put(MoldPayload item) {
		ResponseEntity<?> response = BeanUtils.get(MoldController.class).editMold(item.getId(), item);
		DatImpUtils.response(response);
	}

	private void setValueIn(Object item, String fieldName, Object value, String title) {
		try {
		ValueUtils.set(item, fieldName, value);

		Class<?> type = ReflectionUtils.getGetter(item, fieldName) != null ? ReflectionUtils.getGetter(item, fieldName).getReturnType() : null;
		if (saleson.common.util.DataUtils.isInterger(type)
			&& !saleson.common.util.DataUtils.compareExactlyNumberValue(value, ValueUtils.get(item, fieldName)))
			throw DataUtils.newDataValueInvalidException("tooling", title, value);
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			throw DataUtils.newDataValueInvalidException("tooling", title, value);
		}
	}
}
