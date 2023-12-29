package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.MicPackage;
import com.stg.service.MicPackageService;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "MIc Package Apis")
public class MicPackageController {
    private final MicPackageService micPackageService;

    @GetMapping(Endpoints.URL_MIC_PACKAGE_LIST_NO_PAGING)
    @ResponseStatus(HttpStatus.OK)
    public List<MicPackage> micPackages(@AuthenticationPrincipal CustomUserDetails user) {
        return micPackageService.list();
    }
}
