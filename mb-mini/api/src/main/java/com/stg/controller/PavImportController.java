package com.stg.controller;

import com.stg.entity.PavImport;
import com.stg.service.PavService;
import com.stg.service.dto.external.requestV2.MbalIllustrationBoardV2ReqDto;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "APIs Nghề Nghiệp")
public class PavImportController {

    private final PavService pavService;

    @PostMapping(Endpoints.URL_PAV_IMPORT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> importPav(@RequestParam("file") MultipartFile parts) {
        return pavService.importPav(parts);
    }

    @PostMapping(Endpoints.URL_LIST_PAV)
    @ResponseStatus(HttpStatus.OK)
    public List<PavImport> filterList(@Valid @RequestBody MbalIllustrationBoardV2ReqDto illustrationBoardDto) {
        return pavService.filterList(illustrationBoardDto);
    }

}
