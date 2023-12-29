package com.stg.service.dto.potentialcustomer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.stg.entity.lead.LeadInfo;
import com.stg.entity.lead.LeadStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class LeadStatusDto {

    private String leadId;

    private String leadStatus;
    private List<String> leadStatusDetail;
    private String leadStatusNum;
    private List<String> leadStatusNumDetail;

    public void setLeadInfo(LeadInfo entity) {
        if (entity == null) {
            return;
        }
        setLeadStatus(entity.getLeadStatus());
        setLeadStatusNum(entity.getStatus());
        if (StringUtils.hasText(entity.getStatusDetail())) {
            setLeadStatusNumDetail(Arrays.asList(entity.getStatusDetail().split(",")));
            setLeadStatusDetail(
                    getLeadStatusNumDetail().stream().map(s -> {
                        LeadStatus status = LeadStatus.codeOf(s);
                        return status != null ? status.getDescription() : null;
                    }).collect(Collectors.toList()));
        }

    }

}
