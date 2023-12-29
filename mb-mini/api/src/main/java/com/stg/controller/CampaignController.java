package com.stg.controller;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.utils.Endpoints;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.Campaign;
import com.stg.service.CampaignService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.campaign.CampaignCreateDto;
import com.stg.service.dto.campaign.CampaignDetailDto;
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


@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "Campaign Apis")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping(Endpoints.URL_CAMPAIGN)
    @ResponseStatus(HttpStatus.OK)
    public Campaign createCampaign(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CampaignCreateDto dto) {
        return campaignService.createCampaign(Long.valueOf(user.getUserId()), dto);
    }

    @GetMapping(Endpoints.URL_CAMPAIGN)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<CampaignDetailDto> filterList(@AuthenticationPrincipal CustomUserDetails user,
                                                          @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                          @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                          @RequestParam(name = "query", required = false) String query,
                                                            @RequestParam(name = "status", required = false) String status) {
        return campaignService.filterList(Long.valueOf(user.getUserId()), page, size, query, status);
    }

    @GetMapping(Endpoints.URL_CAMPAIGN_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public CampaignDetailDto campaignDetail(@AuthenticationPrincipal CustomUserDetails user,
                                            @Valid @PathVariable("id") Long id) {
        return campaignService.findById(Long.valueOf(user.getUserId()), id);
    }

    @GetMapping(Endpoints.URL_CAMPAIGN_LIST_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    public void exportList(@AuthenticationPrincipal CustomUserDetails user,
                           @RequestParam(name = "query", required = false) String query,
                           @RequestParam(name = "status", required = false) String status,
                           @RequestParam("type") String type,
                           HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        campaignService.exportList(Long.valueOf(user.getUserId()), query, status, type, response);
    }

    @PostMapping(Endpoints.URL_CAMPAIGN_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public Campaign updateCampaign(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CampaignCreateDto dto,
            @Valid @PathVariable("id") Long campaignId) {
        return campaignService.updateCampaign(Long.valueOf(user.getUserId()), dto, campaignId);
    }

}
