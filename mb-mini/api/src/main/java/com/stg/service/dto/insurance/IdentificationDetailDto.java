package com.stg.service.dto.insurance;

import com.stg.entity.Identification;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class IdentificationDetailDto {

    private Long id;

    // giấy tờ tùy thân
    private String identification;

    private String type;

    private String issuePlace;

    private String issueDate;

    private String expiryDate;

    public void setIssueDate(LocalDateTime issueDate) {
        if (issueDate != null) {
            this.issueDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, issueDate);
        }
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        if (expiryDate != null) {
            this.expiryDate = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, expiryDate);
        }
    }

    public IdentificationDetailDto(Identification identificationEntity) {
        this.id = identificationEntity.getId();
        this.identification = identificationEntity.getIdentification();
        this.type = identificationEntity.getType();
        this.issuePlace = identificationEntity.getIssuePlace();
        setIssueDate(identificationEntity.getIssueDate());
        setExpiryDate(identificationEntity.getExpiryDate());
    }
}
