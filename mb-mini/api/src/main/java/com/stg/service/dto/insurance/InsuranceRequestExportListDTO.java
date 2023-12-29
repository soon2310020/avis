package com.stg.service.dto.insurance;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
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
public class InsuranceRequestExportListDTO {
    @CsvBindByName(column = "MB_ID")
    @CsvBindByPosition(position = 0)
    public String mbId;

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 1)
    public String fullName;

    @CsvBindByName(column = "Gói bảo hiểm")
    @CsvBindByPosition(position = 2)
    public String packageName;

    @CsvBindByName(column = "Thời gian đóng phí")
    @CsvBindByPosition(position = 3)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Ngày tham gia")
    @CsvBindByPosition(position = 4)
    public String creationTime;

    @CsvBindByName(column = "Trạng thái")
    @CsvBindByPosition(position = 5)
    public String status;

    @CsvBindByName(column = "Phân khúc")
    @CsvBindByPosition(position = 6)
    public String segment;

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, creationTime);
    }
}
