package com.stg.service;

import javax.validation.Valid;

import com.stg.service.dto.DeleteIdsReq;

@Valid
public interface DeleteService {

    void delete(Long id);

    void delete(DeleteIdsReq req);

}
