package saleson.api.counter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.emoldino.framework.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import saleson.api.counter.payload.BatchUpdateTermPayload;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.machine.payload.MachineMoldData;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.preset.PresetService;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.PageType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.TabTable;
import saleson.model.TabTableData;
import saleson.model.data.CdataCounter;
import saleson.service.transfer.TransferService;

@RestController
@RequestMapping("/api/counters")
public class CounterController {

	@Autowired
	private CounterService counterService;

	@Autowired
	private MoldRepository moldRepository;

	@Lazy
	@Autowired
	private MoldService moldService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private VersioningService versioningService;

	@Autowired
	private PresetService presetService;

	@Lazy
	@Autowired
	private TransferService transferService;

	@GetMapping
	public ResponseEntity<Page<Counter>> getAllCounters(CounterPayload payload, Pageable pageable, Model model) {
		counterService.changeTabPayload(payload);
		Page<Counter> pageContent = counterService.findAll(payload.getPredicate(), pageable);
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);

	}

	@PostMapping
	public ApiResponse newCounter(@RequestPart("payload") String json, @RequestParam("files") MultipartFile[] files) {
		try {
			CounterPayload payload = objectMapper.readValue(json, CounterPayload.class);

			Counter counter = payload.getModel();

			Optional<Counter> optionalCounter = counterService.findByEquipmentCode(payload.getEquipmentCode());
			if (optionalCounter.isPresent()) {

				counter = optionalCounter.get();

				if (EquipmentStatus.INSTALLED == counter.getEquipmentStatus() || EquipmentStatus.AVAILABLE == counter.getEquipmentStatus()
						|| EquipmentStatus.FAILURE == counter.getEquipmentStatus() || EquipmentStatus.DISCARDED == counter.getEquipmentStatus()) {
					return ApiResponse.error("Counter is already registered in the system.");
				}

				counter = payload.getModel(counter);
			}

			Mold mold = moldService.findById(payload.getMoldId());

			if (mold != null) {
				counter.setMold(mold);
				counter.setEquipmentStatus(EquipmentStatus.INSTALLED);
			} else {
				counter.setEquipmentStatus(EquipmentStatus.AVAILABLE);
			}

			counterService.save(counter, files);

		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(MessageUtils.get("SYS_ERROR",null));
		}
		return ApiResponse.success();
	}

	@GetMapping("{id}")
	public ResponseEntity<Counter> getCounter(@PathVariable("id") Long id) {
		try {
			Counter counter = counterService.findById(id);
			if (counter != null)
				counterService.loadWorkOrderDetail(counter);

			return new ResponseEntity<>(counter, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@GetMapping("qr-scan/{code}")
	public ResponseEntity<Counter> getCounter(@PathVariable("code") String code) {
		try {
			Optional<Counter> counter = counterService.findByEquipmentCode(code);
			if (counter.isPresent())
				return new ResponseEntity<>(counter.get(), HttpStatus.OK);
			else
				return new ResponseEntity<>(null, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@GetMapping("{id}/files")
	public ResponseEntity<Counter> getCounterFiles(@PathVariable("id") Long id) {
		try {
			Counter counter = counterService.findById(id);
			return new ResponseEntity<>(counter, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@PutMapping("{id}")
	public ApiResponse editCounter(@PathVariable("id") Long id, @RequestPart("payload") String json, @RequestParam("files") MultipartFile[] files) {
		try {
			CounterPayload payload = objectMapper.readValue(json, CounterPayload.class);
			Counter counter = counterService.findById(id);
			if (counter == null) {
				new ApiResponse(true, "ERROR");
			}

			Long oldMoldId = counter.getMold() != null ? counter.getMold().getId() : null;
			if (payload.getMoldId() != null) {
				Mold mold = moldService.findById(payload.getMoldId());

				if (counter.getMold() != null && !counter.getMold().getId().equals(payload.getMoldId())
						&& (EquipmentStatus.AVAILABLE == counter.getEquipmentStatus() || EquipmentStatus.DISCARDED == counter.getEquipmentStatus())) {
					return ApiResponse.error("Can not install counter.");
				}
				if (mold != null) {
					counter.setMold(mold);
				}
				//valid
				if (mold.getLastShot() != null && mold.getLastShot() > (payload.getPresetCount() != null ? payload.getPresetCount() : 0)) {
					return ApiResponse.error("Preset value must be equal or larger than the current last short.");
				}
			}

			Counter updated = payload.getModel();
			updated.setId(counter.getId());
			DataUtils.mapCreateAndUpdateInfo(updated, counter);
			boolean isIdentical = DataUtils.deepCompare(counter, updated);

			counterService.save(payload.getModel(counter), files);
			Counter counterFinal = counterService.findById(id);

			if (!isIdentical) {
				versioningService.writeHistory(counterFinal);
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
	public ApiResponse equipmentStatus(@PathVariable(value = "id", required = true) Long id, @RequestBody CounterPayload payload) {

		if (!payload.getId().equals(id)) {
			return ApiResponse.error("요청 데이터 오류");
		}

		try {
			Counter counter = counterService.findById(id);
			counter.setEquipmentStatus(payload.getEquipmentStatus());
			counterService.save(counter);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error("상태 변경 중 오류 발생.");
		}
		return ApiResponse.success();
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteCounter(@PathVariable("id") Long id) {
		counterService.deleteById(id);
		return new ApiResponse(true, "성공.");
	}

	/**
	 * 금형과 연결이 안된 채 동작하거나 등록이 안된채 동작하는 카운터 목록
	 * @param payload
	 * @param pageable
	 * @return
	 */
	@GetMapping("cdata-counters")
	public ResponseEntity<Page<CdataCounter>> getCdataCounters(CounterPayload payload, Pageable pageable) {
		Page<CdataCounter> pageContent = moldRepository.findCdataCounters(payload, pageable);
		return new ResponseEntity(pageContent, HttpStatus.OK);
	}

	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody CounterPayload payload) {

		if (!payload.getId().equals(id)) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Counter counter = counterService.findById(id);
		counter.setEnabled(payload.getEnabled());
		counterService.save(counter);
		Counter finalObj = counterService.findById(id);
		versioningService.writeHistory(finalObj);
		return new ApiResponse(true, "OK!!");
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return counterService.changeStatusInBatch(dto);
	}

	@GetMapping("/get-list-counter")
	public ApiResponse getListCounter(@RequestParam(value = "counterCodeList", required = false) List<String> counterCodeList, Pageable pageable,
			@RequestParam(value = "searchText", required = false) String searchText, @RequestParam(value = "isUnmatched", required = false) Boolean isUnmatched) {
		return counterService.getListCounter(counterCodeList, pageable, searchText, isUnmatched);
	}

	@GetMapping("/check-existed")
	public ResponseEntity<MachineMoldData> checkExistedMold(@RequestParam("code") String code) {
		Optional<Counter> optional = counterService.findByEquipmentCode(code);
		if (optional.isPresent() && optional.get().getMold() != null) {
			Counter counter = optional.get();
			Mold mold = counter.getMold();
			MachineMoldData data = new MachineMoldData();
			data.setId(mold.getId());
			data.setName(mold.getEquipmentCode());
			if (mold.getMachine() != null) {
				data.setMatchedId(mold.getMachineId());
				data.setMatchedName(mold.getMachine().getMachineCode());
			}
			return new ResponseEntity<>(data, HttpStatus.OK);
		} else {
			throw new RuntimeException(CommonMessage.OBJECT_NOT_FOUND);
		}
	}

	@GetMapping("/migration-OP-miss-match")
	public void migrationOPMissMatch() {
		counterService.migrationOPMoldAndCounterMissMatch();
	}

	@PostMapping("/subscription-term")
	public ApiResponse subscriptionTerm(@RequestBody BatchUpdateTermPayload dto) {
		return counterService.subscriptionTerm(dto);
	}

}
