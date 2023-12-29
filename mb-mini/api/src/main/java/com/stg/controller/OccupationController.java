package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.OccupationService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.occupation.ImportOccupationHistoryDto;
import com.stg.service.dto.occupation.OccupationDTO;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "APIs Nghề Nghiệp")
public class OccupationController {

    private final OccupationService occupationService;

    @GetMapping(Endpoints.URL_OCCUPATION_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<OccupationDTO> list(@AuthenticationPrincipal CustomUserDetails user) {
        return occupationService.list(Long.valueOf(user.getUserId()));
    }

    @PostMapping(Endpoints.URL_OCCUPATION_IMPORT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> importList(@RequestParam("file") MultipartFile parts) {
        return occupationService.importList(parts);
    }

    @GetMapping(Endpoints.URL_OCCUPATION_HISTORY_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<ImportOccupationHistoryDto> listHistory(@AuthenticationPrincipal CustomUserDetails user,
                                                                      @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                      @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        return occupationService.listHistory(Long.valueOf(user.getUserId()), page, size);
    }

}
