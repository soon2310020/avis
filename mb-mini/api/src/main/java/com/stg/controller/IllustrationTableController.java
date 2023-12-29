package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.user.CustomerIdentifier;
import com.stg.service.IllustrationTableService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.CreateIllustrationTableDto;
import com.stg.service.dto.insurance.IllustrationTableDto;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "IllustrationTable APIs")
public class IllustrationTableController {

    private final IllustrationTableService illustrationTableService;

    @GetMapping(Endpoints.URL_ILLUSTRATION_TABLE_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<IllustrationTableDto> list(@AuthenticationPrincipal CustomUserDetails user,
                                                         @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                         @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                         @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                                         @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                                         @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                                         @RequestParam(name = "category", defaultValue = "", required = false) String category) {
        return illustrationTableService.list(Long.valueOf(user.getUserId()), page, size, query, packageName, segment, category);
    }

    @GetMapping(Endpoints.URL_ILLUSTRATION_TABLE_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public IllustrationTableDto detail(@AuthenticationPrincipal CustomUserDetails user,
                                       @Valid @PathVariable("id") String illustrationId,
                                       @RequestParam(name = "packageName", required = false) String packageName) {
        return illustrationTableService.illustrationTableDetail(Long.valueOf(user.getUserId()), illustrationId, packageName);
    }

    @GetMapping(Endpoints.URL_ILLUSTRATION_TABLE_LIST_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportListIllustrationTable(@AuthenticationPrincipal CustomUserDetails user,
                                            @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                            @RequestParam("type") String type,
                                            @RequestParam(name = "packageName", required = false, defaultValue = "") String packageName,
                                            @RequestParam(name = "segment", required = false, defaultValue = "") String segment,
                                            @RequestParam(name = "category", defaultValue = "", required = false) String category,
                                            HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        illustrationTableService.exportList(Long.valueOf(user.getUserId()), query, type, packageName, response, segment, category);
    }

    @PostMapping(Endpoints.MINI_ILLUSTRATION_TABLE_CREATE)
    @ResponseStatus(HttpStatus.OK)
    public void create(@AuthenticationPrincipal CustomerIdentifier identifier,
                       @Valid @RequestBody CreateIllustrationTableDto illustrationTableDto,
                       @PathVariable(value = "processId") Integer processId) {
        illustrationTableService.createIllustrationTable(identifier.getMbId(), illustrationTableDto, processId);
    }

}
