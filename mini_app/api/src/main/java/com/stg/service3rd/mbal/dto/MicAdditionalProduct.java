package com.stg.service3rd.mbal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static com.stg.constant.MicGroup.getMicGroup;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
@Valid
public class MicAdditionalProduct {
    @ApiModelProperty(notes = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
    @NotNull(message = "Nhóm gói bảo hiểm tính phí bên MIC bắt buộc nhập")
    @Max(value = 5, message = "Giá trị lớn nhất là 5")
    @Min(value = 1, message = "Giá trị nhỏ nhất là 1")
    private int nhom;
    @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
    @ApiModelProperty(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String bs1;
    @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
    @ApiModelProperty(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String bs2;
    @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
    @ApiModelProperty(required = true)
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String bs3;
    @ApiModelProperty(required = true)
    @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String bs4;

    @ApiModelProperty(notes = "ttin_hd_bme")
    @Valid
    private FlexibleCommon.ParentContract parentContract;

    @Transient
    @JsonProperty("ma_sp")
    private String maSp;

    public MicAdditionalProduct(MicSandboxContractInfoRespDto.ContractInfo parentContractInfo) {
        setNhom(getMicGroup(parentContractInfo.getNhom()));
        setBs1(parentContractInfo.getBs1());
        setBs2(parentContractInfo.getBs2());
        setBs3(parentContractInfo.getBs3());
        setBs4(parentContractInfo.getBs4());
    }

}
