package saleson.api.machine;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.machine.payload.MachineMoldData;
import saleson.api.machine.payload.MachineMoldPayload;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.preset.PresetService;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Machine;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldMachinePairData;

@RestController
@RequestMapping("/api/machines")
@Slf4j
public class MachineController {

	@Autowired
	MachineService machineService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	VersioningService versioningService;

	@Autowired
	PresetService presetService;

	@Autowired
	ColumnTableConfigService columnTableConfigService;

	@GetMapping
	public ResponseEntity<Page<Machine>> getAllMaChines(MachinePayload payload, Pageable pageable, Model model) {
		machineService.changeTabPayload(payload);

		Page<Machine> pageContent = machineService.findAll(payload.getPredicate(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("list-to-match")
	public ResponseEntity<Page<MachineMoldData>> searchMachinesLiteData(MachinePayload payload, Pageable pageable, Model model) {
		Page<MachineMoldData> pageContent = machineService.getListMachineToMatch(payload.getPredicateForMatching(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/list-to-match-old")
	public ResponseEntity<List<MiniComponentData>> getAllMaChineIds() {
		List<MiniComponentData> list = machineService.getListMachineIdsToMatch();
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<?> newMaChine(@RequestBody MachinePayload payload) {

		try {
			Machine machine = payload.getModel();

			ResponseEntity<?> vaid = machineService.valid(payload, null);
			if (vaid != null)
				return vaid;

			/*
			Optional<Machine> optionalMachine = machineService.findByMaChineCode(payload.getMachineCode());
			if (optionalMachine.isPresent()) {
				return ResponseEntity.badRequest().body("The machine ID already exists.");
			}
			*/

			Machine m = machineService.save(payload.getModel(machine), payload);
			return ResponseEntity.ok(m);

		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		} catch (Exception e) {
			log.error("Error when create new machine", e);
			return ResponseEntity.badRequest().body("Fail!");
		}
//		return ApiResponse.success();
	}

	@PostMapping("add-multipart")
	public ResponseEntity<?> newMaChineMultipart(MultipartFormData formData) {
		try {
			MachinePayload payload = objectMapper.readValue(formData.getPayload(), MachinePayload.class);
			payload.setPictureFiles(formData.getThirdFiles());
			Machine machine = payload.getModel();
			ResponseEntity valid = machineService.valid(payload, null);
			if (valid != null) {
				return valid;
			}

			Machine m = machineService.save(payload.getModel(machine), payload);
			return ResponseEntity.ok(m);

		} catch (Exception e) {
			log.error("Error when create new machine", e);
			return ResponseEntity.badRequest().body("Fail!");
		}

	}

	@DataLeakDetector(disabled = true)
	@GetMapping("{id}")
	public ResponseEntity<Machine> getMaChine(@PathVariable("id") Long id) {

		try {
			Machine machine = machineService.findById(id);
			return new ResponseEntity<>(machine, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INSUFFICIENT_STORAGE);
		}
	}

	@PutMapping("/edit-multipart/{id}")
	public ResponseEntity<?> editMachineMultipart(@PathVariable("id") Long id, MultipartFormData formData) {
		MachinePayload payload;
		try {
			payload = objectMapper.readValue(formData.getPayload(), MachinePayload.class);
			payload.setPictureFiles(formData.getThirdFiles());
		} catch (Exception e) {
			log.error("Error when update machine", e);
			return ResponseEntity.badRequest().body("Fail!");
		}
		return editMachine(id, payload);
	}

	@PutMapping("{id}")
	public ResponseEntity<?> editMachine(@PathVariable("id") Long id, @RequestBody MachinePayload payload) {
		try {
			return TranUtils.doNewTran(() -> {
				ResponseEntity<?> vaid = machineService.valid(payload, id);
				if (vaid != null) {
					return vaid;
				}

				Machine machine = machineService.findById(id);
				Machine oldToCheck = DataUtils.deepCopyJackSon(machine, Machine.class);

				machine = machineService.save(payload.getModel(machine), payload);

				Machine machineFinal = machineService.findById(id);
				Machine newToCheck = DataUtils.deepCopyJackSon(machineFinal, Machine.class);
				oldToCheck.setCreatedAt(newToCheck.getCreatedAt());
				oldToCheck.setUpdatedAt(newToCheck.getUpdatedAt());
				oldToCheck.setCreatedBy(newToCheck.getCreatedBy());
				oldToCheck.setUpdatedBy(newToCheck.getUpdatedBy());
				oldToCheck.setMold(newToCheck.getMold());
				oldToCheck.setCompany(newToCheck.getCompany());
				oldToCheck.setLocation(newToCheck.getLocation());
				try {
					boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);
					if (!isIdentical) {
						versioningService.writeHistory(machineFinal);
					}
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw ValueUtils.toAe(e, null);
				}
				return ResponseEntity.ok(machine);
			});
		} catch (BizException e) {
			return ResponseEntity.badRequest().body(e.getCodeMessage());
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			return ResponseEntity.badRequest().body("Fail!");
		}
	}

	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody MachinePayload payload) {

		if (!payload.getId().equals(id)) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Machine machine = machineService.findById(id);
		machine.setEnabled(payload.isEnabled());
		machineService.save(machine, payload);
		versioningService.writeHistory(machineService.findById(id));

		return new ApiResponse(true, "OK!!");
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteMachine(@PathVariable("id") Long id) {
		machineService.deleteById(id);
		return new ApiResponse(true, "성공.");
	}

	@PostMapping("/match-with-tooling")
	public ApiResponse matchMachineWithTooling(@RequestBody MachineMoldPayload payload) {
		String result = machineService.matchMachineWithTooling(payload.getMachineId(), payload.getMoldId());
		if (result.equals(CommonMessage.OK))
			return ApiResponse.success(result);
		else
			return ApiResponse.error(result);
	}

	@PostMapping("/un-match-with-tooling")
	public ApiResponse unMatchMachineWithTooling(@RequestBody MachineMoldPayload payload) {
		String result = machineService.unMatchMachineWithTooling(payload.getMoldId(), payload.getMachineId());
		if (result.equals(CommonMessage.OK)) {
			return ApiResponse.success(result);
		} else {
			return ApiResponse.error(result);
		}
	}

	@PostMapping("/un-match-with-tooling-in-batch")
	public ApiResponse unMatchMachineWithToolingInBatch(@RequestBody BatchUpdateDTO payload) {
		String result = machineService.unMatchMachineWithToolingInBatch(payload);
		if (result.equals(CommonMessage.OK)) {
			return ApiResponse.success(result);
		} else {
			return ApiResponse.error(result);
		}
	}

	@GetMapping("/list-matched-old")
	public ResponseEntity<List<MoldMachinePairData>> getListMatchedMachine() {
		return new ResponseEntity<>(machineService.getListMatchedMachines(), HttpStatus.OK);
	}

	@GetMapping("/list-matched")
	public ResponseEntity<Page<MoldMachinePairData>> getListMatchedMachine(MachinePayload payload, Pageable pageable, Model model) {
		Page<MoldMachinePairData> pageContent = machineService.getPageMatchedMachines(payload.getPredicateForUnMatching(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/check-existed")
	public ResponseEntity<MachineMoldData> checkExistedMachine(@RequestParam("code") String code) {
		Optional<Machine> optionalMachine = machineService.findByMaChineCode(code);
		if (optionalMachine.isPresent()) {
			Machine machine = optionalMachine.get();
			MachineMoldData data = new MachineMoldData();
			data.setId(machine.getId());
			data.setName(machine.getMachineCode());
			if (machine.getMold() != null) {
				data.setMatchedId(machine.getMold().getId());
				data.setMatchedName(machine.getMold().getEquipmentCode());
			}
			return new ResponseEntity<>(data, HttpStatus.OK);
		} else {
			throw new RuntimeException(CommonMessage.OBJECT_NOT_FOUND);
		}
	}

	@GetMapping("/lines")
	public ResponseEntity<List<String>> getLines() {
		List<String> lines = machineService.getDistinctLines();
		return ResponseEntity.ok(lines);
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return machineService.changeStatusInBatch(dto);
	}

	@DeleteMapping("/delete/{id}")
	public ApiResponse delete(@PathVariable("id") Long id) {
		return machineService.delete(id);
	}

	@PostMapping("/restore/{id}")
	public ApiResponse restore(@PathVariable("id") Long id) {
		return machineService.restore(id);
	}

	@PostMapping("/delete-in-batch")
	public ApiResponse deleteInBatch(@RequestBody BatchUpdateDTO dto) {
		return machineService.deleteInBatch(dto);
	}
}
