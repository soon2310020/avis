package saleson.api.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.Event;
import saleson.service.transfer.LogDisconnectionService;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    @Autowired
    private LogDisconnectionService logDisconnectionService;

    @PutMapping("/{type}/{id}/{event}")
    public ResponseEntity<?> addLog(@PathVariable(value = "type") EquipmentType type,
                                    @PathVariable(value = "id") Long id,
                                    @PathVariable(value = "event") Event event){
        logDisconnectionService.save(type, id, event);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<?> getLog(@PathVariable(value = "type") EquipmentType type,
                                    @PathVariable(value = "id") Long id){
        return new ResponseEntity<>(logDisconnectionService.getLog(type, id), HttpStatus.OK);
    }

    @PostMapping("/recover-event-time")
    public ResponseEntity<?> recoverEventTime(){
        return new ResponseEntity<>(logDisconnectionService.recoverEventTime(), HttpStatus.OK);
    }
}
