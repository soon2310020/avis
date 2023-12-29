package com.stg.service3rd.mbal.dto.req;

import com.stg.service.dto.quotation.QuotationAssuredDto;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * quotation ULRP 3.0
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleProcessReq {

    @Valid
    @NotNull(message = "Thông tin bắt buộc nhập")
    @ApiModelProperty(name = "Thông tin người mua bảo hiểm", required = true)
    private FlexibleCommon.CustomerInfo customer;

    @ApiModelProperty(name = "Id gói bảo hiểm", required = true)
    private Integer productPackageId;

    @ApiModelProperty(name = "Mã code gói bảo hiểm")
    private String productPackageCode;

    @ApiModelProperty(name = "Người mua bảo hiểm cũng là người hưởng bảo hiểm chính", required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private boolean customerIsAssured = true;
    
    private Long directId;

    //@NotNull /* when customerIsAssured = true */
    @Valid
    @ApiModelProperty(name = "Thông tin người bảo hiểm chính nếu customerIsAssured = false", required = false)
    private QuotationAssuredDto assured;

}
