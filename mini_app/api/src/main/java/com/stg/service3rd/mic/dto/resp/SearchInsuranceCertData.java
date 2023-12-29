package com.stg.service3rd.mic.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SearchInsuranceCertData {

    @ApiModelProperty("Phí trả bảo hiểm")
    @JsonProperty("phi")
    private Double phi;
    @ApiModelProperty("Số hợp đồng/ số GCN")
    @JsonProperty("so_hd")
    private String so_hd;
    @ApiModelProperty("Số id của hợp đồng/GCN trên hệ thống MIC")
    @JsonProperty("so_id")
    private String so_id;
    @ApiModelProperty("links file GCN (Trường hợp hệ thống đang tạo GCN sẽ có nội dung là : \"Đang tạo GCN\")")
    @JsonProperty("gcn")
    private String gcn;
    @ApiModelProperty("Tên khách hàng")
    @JsonProperty("ten")
    private String ten;
    @ApiModelProperty("Số CMND/CCCD khách hàng")
    @JsonProperty("cmt")
    private String cmt;
    @ApiModelProperty("Địa chỉ khách hàng")
    @JsonProperty("dchi")
    private String dchi;
    @ApiModelProperty("Số mobi khách hàng")
    @JsonProperty("mobi")
    private String mobi;
    @ApiModelProperty("Ngày hiệu lực")
    @JsonProperty("ngay_hl")
    private String ngay_hl;
    @ApiModelProperty("Ngày kết thúc")
    @JsonProperty("ngay_kt")
    private String ngay_kt;
    @ApiModelProperty("Links chụp ảnh/ link upload đính kèm")
    @JsonProperty("file")
    private String file;
    @ApiModelProperty("Tình trạng GCN:\n" +
            "2 - Chưa thanh toán\n" +
            "3 - Chưa có file, ảnh đính kèm (Trường hợp mua vật chất xe)\n" +
            "4 - Thành công\n" +
            "5 - Đơn hàng đã hủy")
    @JsonProperty("ttrang")
    private String ttrang;

}
