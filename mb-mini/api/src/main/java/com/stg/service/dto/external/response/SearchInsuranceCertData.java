package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SearchInsuranceCertData {

    @Schema(name = "Phí trả bảo hiểm")
    @JsonProperty("phi")
    private Double phi;
    @Schema(name = "Số hợp đồng/ số GCN")
    @JsonProperty("so_hd")
    private String so_hd;
    @Schema(name = "Số id của hợp đồng/GCN trên hệ thống MIC")
    @JsonProperty("so_id")
    private String so_id;
    @Schema(name = "links file GCN (Trường hợp hệ thống đang tạo GCN sẽ có nội dung là : \"Đang tạo GCN\")")
    @JsonProperty("gcn")
    private String gcn;
    @Schema(name = "Tên khách hàng")
    @JsonProperty("ten")
    private String ten;
    @Schema(name = "Số CMND/CCCD khách hàng")
    @JsonProperty("cmt")
    private String cmt;
    @Schema(name = "Địa chỉ khách hàng")
    @JsonProperty("dchi")
    private String dchi;
    @Schema(name = "Số mobi khách hàng")
    @JsonProperty("mobi")
    private String mobi;
    @Schema(name = "Ngày hiệu lực")
    @JsonProperty("ngay_hl")
    private String ngay_hl;
    @Schema(name = "Ngày kết thúc")
    @JsonProperty("ngay_kt")
    private String ngay_kt;
    @Schema(name = "Links chụp ảnh/ link upload đính kèm")
    @JsonProperty("file")
    private String file;
    @Schema(name = "Tình trạng GCN:\n" +
            "2 - Chưa thanh toán\n" +
            "3 - Chưa có file, ảnh đính kèm (Trường hợp mua vật chất xe)\n" +
            "4 - Thành công\n" +
            "5 - Đơn hàng đã hủy")
    @JsonProperty("ttrang")
    private String ttrang;

}
