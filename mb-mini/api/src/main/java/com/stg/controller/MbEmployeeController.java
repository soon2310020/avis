package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.BaasService;
import com.stg.service.MbEmployeeService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.mb_employee.*;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "Mb Employee Apis")
public class MbEmployeeController {
    private final BaasService baasService;
    private final MbEmployeeService mbEmployeeService;

    @GetMapping(Endpoints.MBEMPLOYEE.MB_EMPLOYEES_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<? extends IMbEmployeeDataDTO> listMbEmployees(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                  @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                                  @RequestParam(name = "query", required = false) String query,
                                                                  @RequestParam(name = "system_type") MbEmployeeSearchType searchType,
                                                                  @RequestParam(name = "managingUnit", required = false )   MbManagerUnit mbManagerUnit  ) {
        return mbEmployeeService.listEmployees(size, page, query, searchType,mbManagerUnit );
    }

    @GetMapping(Endpoints.MBEMPLOYEE.MB_EMPLOYEES_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public MbEmployeeDTO mbEmployeeDetail(@PathVariable("mb_id") String mbId) {
        return mbEmployeeService.employeeDetail(mbId);
    }

    @PostMapping(Endpoints.MBEMPLOYEE.MB_EMPLOYEES_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public MbEmployeeDTO mbEmployeeEdit(@PathVariable("mb_id") String mbId, @RequestBody MbEmployeeReq req,
                                        @AuthenticationPrincipal CustomUserDetails user) {
        baasService.checkPermission(Long.valueOf(user.getUserId()));

        return mbEmployeeService.editMbEmployee(req, mbId);
    }

    @GetMapping(Endpoints.MBEMPLOYEE.MB_EMPLOYEE_HISTORY)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<MbEmployeeHIsDTO> mbEmployeeHIsDTOPage(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                     @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        return mbEmployeeService.listEmployeeHis(size, page);
    }

    @PostMapping(value = Endpoints.MBEMPLOYEE.IMPORT_MB_EMPLOYEES, produces = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public void importEmployeeData(MultipartFile file,
                                   @AuthenticationPrincipal CustomUserDetails user) {
        baasService.checkPermission(Long.valueOf(user.getUserId()));

        mbEmployeeService.importMbEmployeeData(file, user);
    }
}
