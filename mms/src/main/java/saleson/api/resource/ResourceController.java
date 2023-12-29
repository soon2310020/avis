package saleson.api.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

	@Autowired
	private ResourceHandler handler;

	@GetMapping("getAllMessagesOfCurrentUser")
	public @ResponseBody
		ResponseEntity<Map> getAllMessagesOfCurrentUser() {
		Map resObject = handler.getAllMessagesOfCurrentUser();
		return new ResponseEntity<>(resObject, HttpStatus.OK);
	}
}
