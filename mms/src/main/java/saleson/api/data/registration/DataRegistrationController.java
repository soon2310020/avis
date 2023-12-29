package saleson.api.data.registration;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.model.DataRegistration;

@RestController
@RequestMapping("/api/data-registration")
@Slf4j
public class DataRegistrationController {
    @Autowired
    DataRegistrationService dataRegistrationService;

    @PutMapping("/create-request")
    public ResponseEntity<?> createRequest(@RequestBody DataRegistration dataRegistration){
        return ResponseEntity.ok(dataRegistrationService.save(dataRegistration));
    }

    @GetMapping("/generate-request_id")
    public String generateRequestID(){
        return dataRegistrationService.generateRequestID();
    }
}
