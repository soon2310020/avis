package saleson.api.batch;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import saleson.api.batch.payload.BatchPayload;
import saleson.common.payload.ApiResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/batch")
@Slf4j
public class BatchController {
	@Autowired
	BatchService batchService;

	@GetMapping("/all-ids")
	public ApiResponse getAllIds(BatchPayload payload) {
		return batchService.getIds(payload);
	}

	@GetMapping("/" +
			"all-ids-for-full-frame")
	public ApiResponse getAllIds(@RequestParam Map<String,Object> request) throws InvocationTargetException, IllegalAccessException {
		return batchService.getAllIdsForEachFrame(request);
	}

}
