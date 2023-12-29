package com.stg.service.impl;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.Campaign;
import com.stg.entity.user.User;
import com.stg.errors.ApplicationException;
import com.stg.errors.UserHasNoPermissionException;
import com.stg.errors.UserNotFoundException;
import com.stg.repository.CampaignRepository;
import com.stg.repository.UserRepository;
import com.stg.service.CampaignService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.campaign.CampaignCreateDto;
import com.stg.service.dto.campaign.CampaignDetailDto;
import com.stg.service.dto.campaign.CampaignListExportDTO;
import com.stg.service.dto.campaign.CampaignStatus;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.stg.utils.CommonMessageError.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CampaignImpl implements CampaignService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignImpl.class);
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;

    @Autowired
    private final ModelMapper mapper;

    @Override
    public PaginationResponse<CampaignDetailDto> filterList(Long user, int page, int size, String query, String status) {
        isSuperAdminAndAdmin(user);
        PaginationResponse<CampaignDetailDto> response = new PaginationResponse<>();
        String statusFilter = setStatusFilter(status);
        List<Campaign> campaignList = campaignRepository.listCampaignPaging(page, size, query, statusFilter);
        List<CampaignDetailDto> detailDtos = new ArrayList<>();
        for (Campaign campaign : campaignList) {
            detailDtos.add(getCampaignDetailDto(campaign));
        }
        long totalCampaign = campaignRepository.totalCampaign(query, statusFilter);
        response.setData(detailDtos);
        response.setTotalItem(totalCampaign);
        response.setPageSize(size);
        return response;
    }

    private String setStatusFilter(String status) {
        String statusFilter = "";
        switch (status) {
            case "Chưa bắt đầu":
                statusFilter = CampaignStatus.NOT_STARTED.name();
                break;
            case "Đang diễn ra":
                statusFilter = CampaignStatus.IN_PROGRESS.name();
                break;
            case "Đã kết thúc":
                statusFilter = CampaignStatus.ENDED.name();
                break;
            default:
                statusFilter = "";
                break;
        }
        return statusFilter;
    }

    @Override
    public CampaignDetailDto findById(Long idUser, Long idCampaign) {
        isSuperAdminAndAdmin(idUser);
        Campaign campaign = findCampaignById(idCampaign);
        CampaignDetailDto detailDto = getCampaignDetailDto(campaign);
        return detailDto;
    }

    private CampaignDetailDto getCampaignDetailDto(Campaign campaign) {
        CampaignDetailDto detailDto = mapper.map(campaign, CampaignDetailDto.class);
        setStatus(campaign, detailDto);
        return detailDto;
    }

    public CampaignDetailDto setStatus(Campaign campaign, CampaignDetailDto detailDto) {
        if (campaign.getEndTime().isBefore(LocalDateTime.now())) {
            detailDto.setStatus(CampaignStatus.ENDED.label);
        } else if (campaign.getStartTime().isAfter(LocalDateTime.now())) {
            detailDto.setStatus(CampaignStatus.NOT_STARTED.label);
        } else if (campaign.getStartTime().isBefore(LocalDateTime.now()) && campaign.getEndTime().isAfter(LocalDateTime.now())) {
            detailDto.setStatus(CampaignStatus.IN_PROGRESS.label);
        }
        return detailDto;
    }

    private Campaign findCampaignById(long id) {
        return campaignRepository.findById(id).orElseThrow(
                () -> new ApplicationException("Has not Campaign with id: " + id));
    }

    private void isSuperAdminAndAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }

    private User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }

    @Override
    public void exportList(Long idUser, String query, String status, String type, HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportListCampaign with user=" + idUser + ", query=" + query + ", status=" + status + ", type=" + type);

        String statusFilter = setStatusFilter(status);
        List<Campaign> campaignList = campaignRepository.listCampaignNoPaging(query, statusFilter);
        List<CampaignDetailDto> detailDtos = new ArrayList<>();
        for (Campaign campaign : campaignList) {
            detailDtos.add(getCampaignDetailDto(campaign));
        }

        List<CampaignListExportDTO> csvDTOList = mapListToCSV(detailDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)) {
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-chien-dich-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, CampaignListExportDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-chien-dich-excel-", response);
            List<String> headers = Arrays.asList("ID", "Tên chiến dịch", "Sự kiện", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái");
            Field[] fields = CampaignListExportDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_campaign.xlsx", response);
        }
    }

    public List<CampaignListExportDTO> mapListToCSV(List<CampaignDetailDto> dtoList) {
        List<CampaignListExportDTO> csvDTOList = new ArrayList<>();
        for (CampaignDetailDto detailDto : dtoList) {
            CampaignListExportDTO exportDTO = new CampaignListExportDTO();
            exportDTO.id = detailDto.getId();
            exportDTO.name = detailDto.getName();
            exportDTO.event = detailDto.getEvent();
            exportDTO.startTime = detailDto.getStartTime();
            exportDTO.endTime = detailDto.getEndTime();
            exportDTO.status = detailDto.getStatus();
            csvDTOList.add(exportDTO);
        }
        return csvDTOList;
    }

    public Campaign createCampaign(Long userId, CampaignCreateDto dto) {
        LOGGER.info("Starting createCampaign with name=" + dto.getName() + ", event=" + dto.getEvent());
        Campaign campaignToSave = mapper.map(dto, Campaign.class);
        validateNameEvent(campaignToSave);
        validateTime(campaignToSave);
        Campaign campaign = campaignRepository.save(campaignToSave);
        return campaign;
    }

    public void validateNameEvent(Campaign campaign) {
        if (campaign.getName().length() > 255) {
            campaign.setName(campaign.getName().substring(0, 254));
        }
        if (campaign.getEvent().length() > 255) {
            campaign.setEvent(campaign.getEvent().substring(0, 254));
        }
    }

    private static void validateTime(Campaign campaign) {
        if (campaign.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(MSG28);
        }
        if (campaign.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(MSG27);
        }
        if (campaign.getStartTime().isAfter(campaign.getEndTime())) {
            throw new ApplicationException(MSG26);
        }
    }

    public Campaign updateCampaign(Long userId, CampaignCreateDto dto, Long campaignId) {
        LOGGER.info("Starting createCampaign with name=" + dto.getName() + ", event=" + dto.getEvent());
        Optional<Campaign> campaignById = campaignRepository.findById(campaignId);
        if (campaignById.isEmpty()) {
            throw new ApplicationException("Campaign with Id " + campaignId + " is not exist!");
        }
        Campaign campaignToSave = mapper.map(dto, Campaign.class);
        validateNameEvent(campaignToSave);

        Campaign campaignDB = campaignById.get();
        if (!campaignToSave.getStartTime().equals(campaignDB.getStartTime())) {
            if (campaignToSave.getStartTime().isBefore(LocalDateTime.now())) {
                throw new ApplicationException(MSG28);
            }
            if (campaignToSave.getStartTime().isAfter(campaignToSave.getEndTime())) {
                throw new ApplicationException(MSG26);
            }
        }
        if (!campaignToSave.getEndTime().equals(campaignDB.getEndTime())) {
            if (campaignToSave.getEndTime().isBefore(LocalDateTime.now())) {
                throw new ApplicationException(MSG27);
            }
            if (campaignToSave.getStartTime().isAfter(campaignToSave.getEndTime())) {
                throw new ApplicationException(MSG26);
            }
        }

        campaignToSave.setId(campaignDB.getId());
        campaignToSave.setCreationTime(campaignDB.getCreationTime());

        return campaignRepository.save(campaignToSave);
    }
}
