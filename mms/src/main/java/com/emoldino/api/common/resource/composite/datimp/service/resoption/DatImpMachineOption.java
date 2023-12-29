package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.machine.MachineController;
import saleson.api.machine.MachineService;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.user.UserRepository;
import saleson.common.enumeration.ObjectType;
import saleson.model.Machine;
import saleson.model.User;

@Getter
@NoArgsConstructor
public class DatImpMachineOption implements ResOptionGetter<MachinePayload> {
	private DatImpResourceType resourceType = DatImpResourceType.MACHINE;
	private ResOption<MachinePayload> resOption = new ResOption<MachinePayload>(//
			// 1. Sheet Name
			Arrays.asList("Machine"), //
			// 2. Object Type
			ObjectType.MACHINE, //
			// 3. Item Class
			MachinePayload.class, //
			// 4. Code Field
			Arrays.asList("machineCode"), //
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

	private void populate(MachinePayload item) {
		if (ObjectUtils.isEmpty(item.getMachineCode())) {
			return;
		}
		Machine machine = BeanUtils.get(MachineService.class).findByMaChineCode(item.getMachineCode()).orElse(null);
		if (machine == null) {
			return;
		}
		ValueUtils.map(machine, item);
	}

	private void doBefore(MachinePayload item) {
		if (!ObjectUtils.isEmpty(item.getMachineType())) {
			if ("사출".equals(item.getMachineType())) {
				item.setMachineType("INJECTION_MOLD");
			} else if ("프레스".equals(item.getMachineType())) {
				item.setMachineType("STAMPING");
			}
		}
		item.setCompanyId(DatImpUtils.getCompanyId(item.getCompanyCode(), "companyCode", item.getCompanyId()));
		item.setLocationId(DatImpUtils.getLocationId(item.getLocationCode(), "locationCode", item.getLocationId()));
		if (!ObjectUtils.isEmpty(item.getEngineerEmails())) {
			List<String> engineerEmails = Arrays.asList(item.getEngineerEmails().split(","));
			List<User> engineers = new ArrayList<>();
			engineerEmails.forEach(email -> {
				if (ObjectUtils.isEmpty(email.trim())) {
					return;
				}
				User user = BeanUtils.get(UserRepository.class)//
						.findByEmailAndDeletedIsFalse(email.trim())//
						.orElseThrow(() -> DataUtils.newDataNotFoundException(User.class, "engineer_in_charge", email));
				engineers.add(user);
			});
			if (!engineers.isEmpty()) {
				item.setEngineerIds(engineers.stream().map(e -> e.getId()).collect(Collectors.toList()));
			}
		}
	}

	private boolean exists(MachinePayload item) {
		return BeanUtils.get(MachineService.class).existsCode(item.getMachineCode(), null);
	}

	private void post(MachinePayload item) {
		item.setEnabled(true);
		item.setDeleted(false);
		ResponseEntity<?> response = BeanUtils.get(MachineController.class).newMaChine(item);
		if (response.getBody() instanceof Machine && response.getBody() != null) {
			item.setId(((Machine) response.getBody()).getId());//return id for save custom field
		}
		DatImpUtils.response(response);
	}

	private void put(MachinePayload item) {
		Machine machine = BeanUtils.get(MachineService.class)//
				.findByMaChineCode(item.getMachineCode())//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Machine.class, DatImpUtils.toXlsColumnTitle("machineCode"), item.getMachineCode()));
		item.setEnabled(machine.isEnabled());
		item.setDeleted(false);
		ResponseEntity<?> response = BeanUtils.get(MachineController.class).editMachine(machine.getId(), item);
		item.setId(machine.getId());
		DatImpUtils.response(response);
	}
}
