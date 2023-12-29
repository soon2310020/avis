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
public class IllustrationTableExportListDTO {
    @CsvBindByName(column = "Số BMH/GCN")
    @CsvBindByPosition(position = 0)
    public String illustrationNumber;

    @CsvBindByName(column = "Họ và tên")
    @CsvBindByPosition(position = 1)
    public String fullName;

    @CsvBindByName(column = "Ngày sinh")
    @CsvBindByPosition(position = 2)
    public String birthday;

    @CsvBindByName(column = "Tham gia gói bảo hiểm")
    @CsvBindByPosition(position = 3)
    public String packageName;

    @CsvBindByName(column = "Thời hạn đóng phí")
    @CsvBindByPosition(position = 4)
    public String mbalFeePaymentTime;

    @CsvBindByName(column = "Đơn vị phát hành")
    @CsvBindByPosition(position = 5)
    public String issuer;

    @CsvBindByName(column = "Phân khúc")
    @CsvBindByPosition(position = 6)
    public String segment;

    public void setBirthday(LocalDateTime dateTime) {
        this.birthday = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, dateTime);
    }
}
