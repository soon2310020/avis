package com.stg.service3rd.mic.dto.req;

import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
@Getter
@Setter
@Accessors(chain = true)
public class MicGenerateInsuranceCertReqDto {

    @ApiModelProperty(hidden = true)
    private String ma_dvi;
    @ApiModelProperty(hidden = true)
    private String nsd;
    @ApiModelProperty(hidden = true)
    private String pas;
    @ApiModelProperty(hidden = true)
    private String id_tras;
    @ApiModelProperty(hidden = true)
    private String checksum;
    @ApiModelProperty(hidden = true)
    private String nv;
    @ApiModelProperty(hidden = true)
    private String kieu_hd;
    @ApiModelProperty(notes = "Tổng phí thanh toán trả ra ở api tính phí", required = true, example = "821000")
    private Double ttoan;
    @ApiModelProperty(notes = "Thông tin hợp đồng", required = true)
    private GcnMicCareTtinhd gcn_miccare_ttin_hd;
    @ApiModelProperty(notes = "Thông tin khách hàng", required = true, example = "821000")
    private GcnMicCareTtinkh gcn_miccare_ttin_kh;
    @ApiModelProperty(notes = "Thông tin tình trạng sức khỏe", required = true)
    private GcnMicCareTtsk gcn_miccare_ttsk;

    @ApiModelProperty(notes = "Số hợp đồng gốc khi loại hợp đồng là Bổ sung", required = true)
    private String so_hd_g;
    @ApiModelProperty(notes = "Mã sản phẩm theo chương trình: NG_SKC", required = true)
    private String ma_sp;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GcnMicCareTtinhd {
        @NotNull
        @ApiModelProperty(notes = "ngay_hieu_luc", required = true, example = "20/01/2024")
        private String ngay_hl;
        @ApiModelProperty(notes = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
        private String nhom;
        private String so_hd_bm;
        private String ma_sp;
        @ApiModelProperty(notes = "Người thụ hưởng")
        private String ng_huong;
        @ApiModelProperty(notes = "Quyền lợi bổ sung", required = true)
        private FlexibleCommon.GcnMicCareDkbs gcn_miccare_dkbs;

        @ApiModelProperty(notes = "Xác nhận thông tin C- CÓ, K- Không", required = true)
        private String dongy;
        @ApiModelProperty(notes = "ttin_hd_bme", required = true)
        private FlexibleCommon.ParentContract ttin_hd_bme;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GcnMicCareTtinkh {
        @Pattern(regexp = "C|T", message = "Chi chọn C (Cá nhân) hoặc T (Tổ chức)")
        @ApiModelProperty(required = true)
        private String lkh;
        @ApiModelProperty(required = true)
        private String ten;
        @ApiModelProperty(required = true)
        private String cmt;
        @Pattern(regexp = "1|2", message = "1 (Nam) hoặc 2 (Nữ)")
        @ApiModelProperty(required = true)
        private String gioi;
        @ApiModelProperty(required = true)
        private String mobi;
        @ApiModelProperty(required = true)
        private String email;
        @ApiModelProperty(required = true)
        private String ng_sinh;
        @ApiModelProperty(notes = "Địa chỉ", required = true)
        private String dchi;

        @Pattern(regexp = "1|2|3|4|5|6", message = "Quan hệ với người mua bảo hiểm ( Bố/Mẹ,Vợ/Chồng,Con cái,Bản thân,Anh/Chị/Em ruột,Khác - 2,3,4,1,5,6)")
        @ApiModelProperty(required = true)
        private String qhe;
        @Pattern(regexp = "C|K", message = "Người được bảo hiểm khác người mua bảo hiểm (C- Có, K - Không)")
        @ApiModelProperty(required = true)
        private String dbhm;
        @Pattern(regexp = "C|T", message = "Chi chọn C (Cá nhân) hoặc T (Tổ chức)")
        @ApiModelProperty(notes = "Loại khách hàng mua ( C:Cá nhân, T: tổ chức) (Với trường hợp dbhm = C)")
        private String lkhm;
        @ApiModelProperty(notes = "Tên khách hàng mua (Với trường hợp dbhm = C)")
        private String tenm;
        @ApiModelProperty(notes = "Số CMND/ mã số thuế mua (Với trường hợp dbhm = C)")
        private String cmtm;
        @ApiModelProperty(notes = "Số mobi khách hàng mua (Với trường hợp dbhm = C)")
        private String mobim;
        @ApiModelProperty(notes = "Email khách hàng mua (Với trường hợp dbhm = C)")
        private String emailm;
        @ApiModelProperty(notes = "Ngày sinh khách hàng mua. Theo định dạng dd/mm/yyyy (Với trường hợp dbhm = C)")
        private String ng_sinhm;
        @ApiModelProperty(notes = "Địa chỉ khách hàng mua ( mặc định là số nhà) (Với trường hợp dbhm = C)")
        private String dchim;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GcnMicCareTtsk {
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(required = true)
        private String ck;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(required = true)
        private String cc;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(required = true)
        private String cu;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(hidden = true)
        private String tt1C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt2C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt3C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt4C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt5C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt6C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt7C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt8C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt9C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt10C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt11C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt12C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt13C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt14C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt15C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt16C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt17C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt18C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt19C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt20C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt21C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt22C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt23C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt24C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt25C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt26C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt27C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt28C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt29C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt30C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt31C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt32C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt33C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt34C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt35C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt36C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt37C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt38C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt39C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt40C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt41C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt42C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt43C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt44C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt45C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt46C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt47C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt48C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt49C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt50C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt51C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt52C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt53C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt54C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt55C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt56C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt57C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt58C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt59C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt60C;
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt61C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt62C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt63C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt64C;
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt65C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt66C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt67C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt68C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt69C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt70C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt71C = "K";
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt72C = "K";
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt73C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt74C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt75C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt76C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt77C = "K";
        @ApiModelProperty(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt78C = "K";
        @ApiModelProperty(hidden = true)
        private String bkhactxt = "";
    }

}
