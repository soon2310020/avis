package saleson.api.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.object.dto.CustomFieldGetIn;
import com.emoldino.api.common.resource.base.object.dto.CustomFieldItemIn;
import com.emoldino.api.common.resource.base.object.dto.CustomFieldItemOut;
import com.emoldino.api.common.resource.base.object.service.customfield.CustomFieldService;
import com.emoldino.framework.exception.BizException;

@RestController
@RequestMapping("/api/custom-field")
public class CustomFieldController {
	@Autowired
	private CustomFieldService service;

	@GetMapping
	public ResponseEntity<List<CustomFieldItemOut>> get(CustomFieldGetIn input) {
		return ResponseEntity.ok(service.get(input));
	}

	@PostMapping
	public ResponseEntity<?> post(@RequestBody CustomFieldItemIn input) {
		CustomFieldItemOut customField;
		try {
			customField = service.post(input);
		} catch (BizException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Fail!");
		}
		return ResponseEntity.ok(customField);
	}

	@PutMapping("{id}")
	public ResponseEntity<?> put(@PathVariable(value = "id", required = true) Long id, @RequestBody CustomFieldItemIn input) {
		try {
			service.put(id, input);
		} catch (BizException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Fail!");
		}
		return ResponseEntity.ok("Success!");
	}

	@DeleteMapping("{id}")
	public ResponseEntity<?> delete(@PathVariable(value = "id") Long id) {
		try {
			service.delete(id);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Fail!");
		}
		return ResponseEntity.ok("Success!");
	}

}
