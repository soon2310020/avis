package saleson.api.configuration;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saleson.common.enumeration.CurrencyType;
import saleson.dto.CurrencyConfigDTO;

@RestController
@RequestMapping("/api/currency-config")
public class CurrencyConfigController {
	@Autowired
	CurrencyConfigService service;

	@GetMapping
	public ResponseEntity<?> get() {
		List<CurrencyConfigDTO> currencyConfigList = service.get();
		return ResponseEntity.ok(currencyConfigList);
	}

	@PostMapping
	public ResponseEntity<?> post(@RequestParam("currencyType") CurrencyType currencyType) {
		CurrencyConfigDTO dto = CurrencyConfigDTO.convertToDTO(service.postByCurrencyType(currencyType));
		return ResponseEntity.ok(dto);
	}

	@PostMapping("set-as-main/{currencyType}")
	public ResponseEntity<?> setAsMain(@PathVariable("currencyType") CurrencyType currencyType) {
		CurrencyConfigDTO dto = CurrencyConfigDTO.convertToDTO(service.setAsMain(currencyType));
		return ResponseEntity.ok(dto);
	}

	@DeleteMapping("{currencyType}")
	public ResponseEntity<?> delete(@PathVariable("currencyType") CurrencyType currencyType) {
		try {
			service.deleteByCurrencyType(currencyType);
			return ResponseEntity.ok("Success!");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Fail!");
		}
	}

	@GetMapping("update-rate")
	public ResponseEntity<?> updateRate() {
		service.syncAndGetRate(true);
		return ResponseEntity.ok("Success!");
	}
}
