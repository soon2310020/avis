package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.MbalPackage;
import com.stg.service.MbalPackageService;
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
@Tag(name = "Mbal Package Apis")
public class MbalPackageController {
    private final MbalPackageService mbalPackageService;

    @GetMapping(Endpoints.URL_MBAL_PACKAGE_LIST_NO_PAGING)
    @ResponseStatus(HttpStatus.OK)
    public List<MbalPackage> mbalPackages(@AuthenticationPrincipal CustomUserDetails user) {
        return mbalPackageService.list();
    }
}
