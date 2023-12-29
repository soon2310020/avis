package saleson.api.configuration;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.BizException;

import saleson.common.enumeration.ObjectType;
import saleson.dto.CustomField.CustomFieldListDTO;

@RestController
@RequestMapping("/api/custom-field-value")
public class CustomFieldValueController {
	@Autowired
	private CustomFieldValueService service;

	@GetMapping("list-by-object")
	public ResponseEntity<?> get(@RequestParam @Named("objectId") Long objectId, @RequestParam @Named("objectType") ObjectType objectType) {
		return ResponseEntity.ok(service.get(objectId, objectType));
	}

	@PostMapping("edit-list/{objectId}")
	public ResponseEntity<?> post(@PathVariable(value = "objectId") Long objectId, @RequestBody CustomFieldListDTO input) {
		try {
			CustomFieldListDTO listResult = service.editListCustomField(objectId, input);
			return ResponseEntity.ok(listResult);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			return ResponseEntity.badRequest().body(e.getCodeMessage());
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			return ResponseEntity.badRequest().body("Fail!");
		}
	}

}
