package saleson.api.versioning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.versioning.service.PartVersionService;
import saleson.api.versioning.service.VersioningService;
import saleson.model.clone.dto.RevisionHistoryDto;

@RestController
@RequestMapping("/api/version")
public class VersioningController {
    @Autowired
    VersioningService versioningService;

    @Autowired
    PartVersionService partVersionService;

    @GetMapping("/test-user")
    public ResponseEntity<?> testUser(){
        return ResponseEntity.ok(versioningService.test());
    }


    @GetMapping("/test")
    public ResponseEntity<?>updateUser(){
        return ResponseEntity.ok(partVersionService.test());
    }

    @GetMapping("reversion")
    public ResponseEntity<?> getReversion(@RequestParam("originId") Long originId, @RequestParam("revisionObjectType") String objectType,
                                          @RequestParam("page") int page){
        return ResponseEntity.ok(versioningService.getRevisionHistoriesAndTotalPageApi(originId, objectType, page));
    }

    @PostMapping("restore")
//    @RequestParam(value = "revisionId", required = false) Long revisionId, @RequestParam(value = "revisionObjectType", required = false) String revisionObjectType
    public ResponseEntity<?> restore(@RequestBody RevisionHistoryDto revisionHistoryDto){
        return versioningService.restore(revisionHistoryDto.getId(), revisionHistoryDto.getRevisionObjectType());
    }

    @GetMapping("revision-history/top2")
    public ResponseEntity<?> getRevisionAfterAndBefore(@RequestParam("id") Long id, @RequestParam("revisionObjectType") String revisionObjectType,
                                                       @RequestParam("originId") Long originId){
        return ResponseEntity.ok(versioningService.getRevisionAfterAndBefore(id, revisionObjectType, originId));
    }


}
