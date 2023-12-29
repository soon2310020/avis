package com.stg.service.impl;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.stg.entity.lead.LeadActivity;
import com.stg.entity.lead.LeadInfo;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.potentialcustomer.PotentialCustomerRefer;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.LeadActivityRepository;
import com.stg.repository.LeadInfoRepository;
import com.stg.repository.PotentialCustomerDirectRepository;
import com.stg.repository.PotentialCustomerReferRepository;
import com.stg.service.LeadService;
import com.stg.service.dto.lead.LeadSyncDto;
import com.stg.service.dto.quotation.code.ErrorCodeSyncLead;

@Service
public class LeadServiceImpl implements LeadService {

    @Autowired
    private LeadInfoRepository leadInfoRepository;
    @Autowired
    private LeadActivityRepository leadActivityRepository;
    @Autowired
    private PotentialCustomerReferRepository potentialCustomerReferRepository;
    @Autowired
    private PotentialCustomerDirectRepository potentialCustomerDirectRepository;

    @Override
    @Transactional
    public void syncLead(LeadSyncDto reqDto) {
        LeadInfo leadInfo = leadInfoRepository.findByLeadId(reqDto.getLeadId()).orElse(new LeadInfo());

        leadInfo.setLeadId(reqDto.getLeadId());
        leadInfo.setStatus(reqDto.getLeadStatus());

        PotentialCustomerRefer refer = null;
        PotentialCustomerDirect direct = null;

        if (leadInfo.getId() == null) {
            if (leadInfo.getLeadId().startsWith(PotentialCustomerRefer.LEAD_PREFIX)) {
                String referId = leadInfo.getLeadId().substring(PotentialCustomerRefer.LEAD_PREFIX.length());
                refer = potentialCustomerReferRepository.findById(Long.parseLong(referId))
                        .orElseThrow(() -> new ApplicationException("",
                                new ErrorDto(HttpStatus.BAD_REQUEST.value(), ErrorCodeSyncLead.ID_NOT_FOUND.getCode(),
                                        ErrorCodeSyncLead.ID_NOT_FOUND.getMessage())));
            }
            if (leadInfo.getLeadId().startsWith(PotentialCustomerDirect.LEAD_PREFIX)) {
                String directId = leadInfo.getLeadId().substring(PotentialCustomerDirect.LEAD_PREFIX.length());
                direct = potentialCustomerDirectRepository.findById(Long.parseLong(directId))
                        .orElseThrow(() -> new ApplicationException("",
                                new ErrorDto(HttpStatus.BAD_REQUEST.value(), ErrorCodeSyncLead.ID_NOT_FOUND.getCode(),
                                        ErrorCodeSyncLead.ID_NOT_FOUND.getMessage())));
            }
        }

        if (StringUtils.hasText(leadInfo.getStatusDetail())) {
            leadInfo.setStatusDetail(leadInfo.getStatusDetail() + "," + leadInfo.getStatus());
        } else {
            leadInfo.setStatusDetail(leadInfo.getStatus());
        }

        LeadInfo leadInfoDb = leadInfoRepository.save(leadInfo);

        if (refer != null) {
            refer.setLeadInfo(leadInfoDb);
            potentialCustomerReferRepository.save(refer);
        }

        if (direct != null) {
            direct.setLeadInfo(leadInfoDb);
            potentialCustomerDirectRepository.save(direct);
        }

        List<LeadActivity> activities = reqDto.getActivities().stream().map(activity -> {
            LeadActivity leadActivity = new LeadActivity();
            leadActivity.setAction(activity.getAction());
            leadActivity.setTitle(activity.getTitle());
            leadActivity.setLeadName(activity.getLeadName());
            leadActivity.setCustomerName(activity.getCustomerName());
            if (activity.getStartDate() != null) {
                leadActivity.setStartDate(activity.getStartDate().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            if (activity.getEndDate() != null) {
                leadActivity.setEndDate(activity.getEndDate().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            leadActivity.setNote(activity.getNote());
            leadActivity.setLeadInfo(leadInfoDb);
            return leadActivity;
        }).collect(Collectors.toList());

        leadActivityRepository.saveAll(activities);

    }

}
