package com.stg.service.dto.insurance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.stg.utils.InstallmentPopup;
import com.stg.utils.InstallmentMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InstallmentPaymentPopupDTO {

    private Long id;

    private InstallmentPopup installmentPopup;

    private String installmentErrorCode; // Ma loi dang ky tra gop

    private String installmentStatus; //  Trạng thái đăng ký trả góp

    private String mbalAppNo; //  Mã AF.xxx

    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime paymentTime; //  Ngay dky

    private InstallmentMessageType messageType; //  type of message

}
