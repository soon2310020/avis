package com.stg.service;

import com.stg.service.dto.lead.LeadSyncDto;

public interface LeadService {

    void syncLead(LeadSyncDto reqDto);

}
