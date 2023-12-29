package saleson.api.code;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import saleson.common.enumeration.mapper.Code;
import saleson.common.enumeration.mapper.CodeMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/codes")
public class CodeController {
	@Autowired
	CodeMapper codeMapper;


	@GetMapping
	public Map<String, List<Code>> getAllCodes() {
		return codeMapper.getAll();
	}

	@GetMapping("/{key}")
	public List<Code> getCode(@PathVariable("key") String key) {
		return codeMapper.get(key);
	}
}
