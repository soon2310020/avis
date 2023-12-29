package saleson.api.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.terminal.payload.TerminalAlertPayload;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PageType;
import saleson.common.payload.ApiResponse;
import saleson.common.payload.ApiResponseEntity;
import saleson.common.util.DataUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.data.CounterToolingCode;
import saleson.model.data.TerminalData;
import saleson.model.logs.LogDisconnection;
import saleson.service.transfer.LogDisconnectionService;

@RestController
@RequestMapping("/api/terminals")
public class TerminalController {

	@Autowired
	TerminalService terminalService;

	@Autowired
	LogDisconnectionService logDisconnectionService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	VersioningService versioningService;

	@Autowired
	ColumnTableConfigService columnTableConfigService;

	@GetMapping
	public ResponseEntity getAllTerminals(TerminalPayload payload, Pageable pageable) {
		terminalService.changeTabPayload(payload);

		Page<TerminalData> pageContent = terminalService.findTerminalData(payload.getPredicate(), pageable);
//		Page<Terminal> pageContent = terminalService.findAll(payload.getPredicate(), pageable);
//		List<TerminalData> terminalDataList = terminalService.findLastStatisticsTerminalCounter(pageContent.getContent());
//		Page<TerminalData> content = new PageImpl<>(terminalDataList, pageContent.getPageable(), pageContent.getTotalElements());
		return ApiResponseEntity.ok(pageContent);
	}

	@PostMapping
	public ApiResponse newTerminal(@RequestPart("payload") String json, @RequestParam("files") MultipartFile[] files) {
		try {
			TerminalPayload payload = objectMapper.readValue(json, TerminalPayload.class);

			Terminal terminal = payload.getModel();
			if (terminalService.existsByEquipmentCode(payload.getEquipmentCode())) {
				return ApiResponse.error("Terminal is already registered in the system.");
			}

			//terminalService.save(payload.getModel());
			terminalService.save(terminal, files);
		} catch (Exception e) {
			return ApiResponse.error("저장 중 오류 ");
		}
		return ApiResponse.success();
	}

	@GetMapping("{id}")
	public ResponseEntity<Terminal> getTerminal(@PathVariable("id") Long id) {

		try {
			Terminal terminal = terminalService.getTerminal(id);
			return new ResponseEntity<>(terminal, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@GetMapping("qr-scan/{code}")
	public ResponseEntity<Terminal> getTerminal(@PathVariable("code") String code) {
		try {
			Optional<Terminal> terminal = terminalService.findByEquipmentCode(code);
			if (terminal.isPresent()) {
				return new ResponseEntity<>(terminal.get(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(null, HttpStatus.OK);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@PutMapping("{id}")
	public ApiResponse editTerminal(@PathVariable("id") Long id, @RequestPart("payload") String json, @RequestParam("files") MultipartFile[] files) {

		try {
			TerminalPayload payload = objectMapper.readValue(json, TerminalPayload.class);
			Terminal terminal = terminalService.findById(id);

			if (terminal == null) {
				new ApiResponse(true, "ERROR");
			}
			Terminal oldToCheck= DataUtils.deepCopyJackSon(terminal,Terminal.class);
/*

			Terminal updated = payload.getModel();
			updated.setId(terminal.getId());
			DataUtils.mapCreateAndUpdateInfo(updated, terminal);
			boolean isIdentical = DataUtils.deepCompare(terminal, updated);
*/

			terminalService.save(payload.getModel(terminal), files);
			Terminal terminalFinal = terminalService.findById(id);
			List<String> fileNameList = new ArrayList<>();
			if (files != null) {
				for (MultipartFile multipartFile : files) {
					fileNameList.add(multipartFile.getOriginalFilename());
				}
			}

			Terminal newToCheck = DataUtils.deepCopyJackSon(terminal, Terminal.class);
			DataUtils.mapCreateAndUpdateInfo(oldToCheck, newToCheck);
			boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);
			if (!isIdentical) {
				versioningService.writeHistory(terminalFinal, fileNameList);
			}

			return new ApiResponse(true, "성공.");
		} catch (Exception e) {
			return ApiResponse.error("저장 중 오류 ");
		}
	}

	/**
	 * 장비 상태 변경
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/equipment-status")
	public ApiResponse equipmentStatus(@PathVariable(value = "id", required = true) Long id, @RequestBody TerminalPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			Terminal terminal = terminalService.findById(id);
			terminal.setEquipmentStatus(payload.getEquipmentStatus());
			terminalService.save(terminal);
			Terminal terminalFinal = terminalService.findById(id);
			versioningService.writeHistory(terminalFinal);
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteTerminal(@PathVariable("id") Long id) {

		Terminal terminal = terminalService.findById(id);

		if (terminal != null && OperatingStatus.WORKING == terminal.getOperatingStatus()) {
			return ApiResponse.error("You can not delete it because it has a terminal in use.");
		}

		terminalService.deleteById(id);
		return new ApiResponse(true, "Success.");
	}

	/**
	 * 금형 Disconnect
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@GetMapping("/disconnect")
	public ResponseEntity<Page<TerminalDisconnect>> getAllDisconnectTime(TerminalAlertPayload payload, Pageable pageable, Model model) {
		Page<TerminalDisconnect> pageContent = terminalService.findDisconnectAll(payload.getDisconnectPredicate(), pageable);
		terminalService.loadLastAlertAt(pageContent.getContent(), payload.getStatus());
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@PutMapping("/disconnect/{id}/confirm")
	public ApiResponse disconnectConfirm(@PathVariable(value = "id", required = true) Long id, @RequestBody TerminalAlertPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			terminalService.saveTerminalDisconnect(payload);
			logDisconnectionService.confirm(id, payload.getMessage());
		} catch (Exception e) {
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@PutMapping("/disconnect/confirm")
	public ApiResponse disconnectConfirm(@RequestBody List<TerminalAlertPayload> payloadList) {
		if (payloadList == null || payloadList.size() == 0) {
			return ApiResponse.error("No alert to confirm.");
		}
		terminalService.saveTerminalDisconnect(payloadList);
		List<Long> ids = payloadList.stream().map(x -> x.getId()).collect(Collectors.toList());
		String message = payloadList.get(0).getMessage();
		logDisconnectionService.confirm(ids, message);
		return ApiResponse.success();
	}

	@GetMapping("/disconnect/alert/{terminalId}")
	public ResponseEntity<LogDisconnection> getTerminalDisconnectHistoryById(@PathVariable(value = "terminalId") Long id, NotificationStatus notificationStatus, Pageable pageable,
			Model model) {
		Page<LogDisconnection> pageContent = terminalService.getDisconnectHistoryByTerminalId(id, notificationStatus, pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity(pageContent, HttpStatus.OK);
	}

	@GetMapping("/get-counters/{terminalId}")
	public ResponseEntity<?> getCounters(@PathVariable(value = "terminalId") Long id) {
		List<CounterToolingCode> counterToolingCodes = terminalService.getCounterToolingCodesById(id);
		return new ResponseEntity<>(counterToolingCodes, HttpStatus.OK);
	}

	@GetMapping("/standardize-data")
	public ResponseEntity standardizeData() {
		return ResponseEntity.ok(terminalService.standardizeData());
	}

	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody TerminalPayload payload) {

		if (!payload.getId().equals(id)) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Terminal terminal = terminalService.findById(id);
		terminal.setEnabled(payload.getEnabled());
		terminalService.save(terminal);
		Terminal finalObj = terminalService.findById(id);
		versioningService.writeHistory(finalObj);
		return new ApiResponse(true, "OK!!");
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return terminalService.changeStatusInBatch(dto);
	}
}
