package saleson.api.preset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.rststp.service.RstStpService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.TranUtils;

import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.common.enumeration.PresetStatus;
import saleson.common.payload.ApiResponse;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.Preset;

@RestController
@RequestMapping("/api/presets")
public class PresetController {

	/**
	 * 금형 전체 목록
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@GetMapping
	public ResponseEntity<Page<Mold>> getAllMolds(MoldPayload payload, Pageable pageable, Model model) {
		Page<Mold> pageContent = TranUtils.doTran(() -> BeanUtils.get(MoldService.class).findAll(payload.getPredicate(), pageable));
		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@GetMapping("/{ci}")
	public ResponseEntity<Preset> get(@PathVariable("ci") String ci) {
		try {
			Preset preset = null;
			if (ci.startsWith("NCM")) {
				preset = TranUtils.doTran(() -> BeanUtils.get(PresetRepository.class).findFirstByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.READY));
			} else if (ci.startsWith("EMA")) {
				preset = TranUtils.doTran(() -> BeanUtils.get(PresetRepository.class).findFirstByCiAndPresetStatusOrderByIdDesc(ci, PresetStatus.APPLIED));
			}

			if (preset == null) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
			return new ResponseEntity<>(preset, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Preset(), HttpStatus.OK);
		}
	}

	/**
	 * preset 등록
	 * @param preset
	 * @return
	 */
	@PostMapping
	public ApiResponse post(@RequestBody Preset input) {
//		try {
		TranUtils.doTran(() -> {
			Preset preset = BeanUtils.get(PresetService.class).save(input);
			Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi()).orElse(null);

			if (counter != null) {
				BeanUtils.get(RstStpService.class).apply(preset);
			}
		});
//		} catch (Exception e) {
//			return ApiResponse.error("저장 중 오류 ");
//		}
		return ApiResponse.success();
	}

// TO-DO Delete 
//	@PostMapping
//	public ApiResponse post(@RequestBody Preset preset) {
//		try {			
//			TranUtils.doTran(() -> {
//				BeanUtils.get(PresetService.class).save(preset);												
//				Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(preset.getCi()).orElse(null);
//				if (counter != null) {
//					if (counter.getEquipmentCode() != null && counter.getEquipmentCode().startsWith("EM")) {
//						BeanUtils.get(PresetService.class).apply(preset);
//					} else {
//						BeanUtils.get(TransferService.class).makePresetMisconfigured(counter.getMold(), counter);
//					}
//				}
//			});
//		} catch (Exception e) {
//			return ApiResponse.error("저장 중 오류 ");
//		}
//		return ApiResponse.success();
//	}

	@PutMapping("/{id}")
	public ApiResponse cancel(@PathVariable Integer id, @RequestBody Preset preset) {
		if (id == null || !id.equals(preset.getId())) {
			return ApiResponse.error("Data Error.");
		}
		try {
			TranUtils.doTran(() -> BeanUtils.get(PresetService.class).cancel(preset));
		} catch (Exception e) {
			return ApiResponse.error("Preset Cancel Error.");
		}
		return ApiResponse.success();
	}

}
