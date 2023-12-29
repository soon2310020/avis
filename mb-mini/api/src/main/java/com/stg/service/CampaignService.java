package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.Campaign;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.campaign.CampaignCreateDto;
import com.stg.service.dto.campaign.CampaignDetailDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface CampaignService {

    PaginationResponse<CampaignDetailDto> filterList(Long user, int page, int size, String query, String status);

    CampaignDetailDto findById(Long idUser, Long idCampaign);

    void exportList(Long idUser, String query, String status, String type, HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    Campaign createCampaign(Long userId, CampaignCreateDto dto);

    Campaign updateCampaign(Long userId, CampaignCreateDto dto, Long campaignId);
}
