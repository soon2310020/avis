package com.stg.service.dto;

import com.stg.service.dto.quotation.code.ErrorCodeSyncLead;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncResp {

    private String errorCode;
    private String errorMessage;

    public SyncResp(ErrorCodeSyncLead codeSyncLead) {
        this.errorCode = codeSyncLead.getCode();
        this.errorMessage = codeSyncLead.getMessage();
    }

    public static SyncResp success() {
        return new SyncResp(ErrorCodeSyncLead.SUCCESS);
    }

    public static SyncResp error() {
        return new SyncResp(ErrorCodeSyncLead.INTERNAL_SERVER_ERROR);
    }
}
