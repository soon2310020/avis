package com.stg.service.dto.external.request;

import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Data
@Accessors(chain = true)
public class MicGenerateInsuranceCertReqDto {

    @Schema(hidden = true)
    private String ma_dvi;
    @Schema(hidden = true)
    private String nsd;
    @Schema(hidden = true)
    private String pas;
    @Schema(hidden = true)
    private String id_tras;
    @Schema(hidden = true)
    private String checksum;
    @Schema(hidden = true)
    private String nv;
    @Schema(hidden = true)
    private String kieu_hd;
    @Schema(description = "Tổng phí thanh toán trả ra ở api tính phí", required = true, example = "821000")
    private Double ttoan;
    @Schema(description = "Thông tin hợp đồng", required = true)
    private GcnMicCareTtinhd gcn_miccare_ttin_hd;
    @Schema(description = "Thông tin khách hàng", required = true, example = "821000")
    private GcnMicCareTtinkh gcn_miccare_ttin_kh;
    @Schema(description = "Thông tin tình trạng sức khỏe", required = true)
    private GcnMicCareTtsk gcn_miccare_ttsk;

    @Schema(description = "Số hợp đồng gốc khi loại hợp đồng là Bổ sung", required = true)
    private String so_hd_g;
    @Schema(description = "Mã sản phẩm theo chương trình: NG_SKC", required = true)
    private String ma_sp;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GcnMicCareTtinhd {
        @NotNull
        @Schema(description = "ngay_hieu_luc", required = true, example = "20/01/2024")
        private String ngay_hl;
        @Schema(description = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
        private String nhom;
        private String so_hd_bm;
        private String ma_sp;
        @Schema(description = "Người thụ hưởng")
        private String ng_huong;
        @Schema(description = "Quyền lợi bổ sung", required = true)
        private Common.GcnMicCareDkbs gcn_miccare_dkbs;

        @Schema(description = "Xác nhận thông tin C- CÓ, K- Không", required = true)
        private String dongy;
        @Schema(description = "ttin_hd_bme", required = true)
        private Common.ParentContract ttin_hd_bme;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GcnMicCareTtinkh {
        @Pattern(regexp = "C|T", message = "Chi chọn C (Cá nhân) hoặc T (Tổ chức)")
        @Schema(required = true)
        private String lkh;
        @Schema(required = true)
        private String ten;
        @Schema(required = true)
        private String cmt;
        @Pattern(regexp = "1|2", message = "1 (Nam) hoặc 2 (Nữ)")
        @Schema(required = true)
        private String gioi;
        @Schema(required = true)
        private String mobi;
        @Schema(required = true)
        private String email;
        @Schema(required = true)
        private String ng_sinh;
        @Schema(description = "Địa chỉ", required = true)
        private String dchi;
        @Pattern(regexp = "1|2|3|4|5|6", message = "Quan hệ với người mua bảo hiểm ( Bố/Mẹ,Vợ/Chồng,Con cái,Bản thân,Anh/Chị/Em ruột,Khác - 2,3,4,1,5,6)")
        @Schema(required = true)
        private String qhe;
        @Pattern(regexp = "C|K", message = "Người được bảo hiểm khác người mua bảo hiểm (C- Có, K - Không)")
        @Schema(required = true)
        private String dbhm;
        @Pattern(regexp = "C|T", message = "Chi chọn C (Cá nhân) hoặc T (Tổ chức)")
        @Schema(description = "Loại khách hàng mua ( C:Cá nhân, T: tổ chức) (Với trường hợp dbhm = C)")
        private String lkhm;
        @Schema(description = "Tên khách hàng mua (Với trường hợp dbhm = C)")
        private String tenm;
        @Schema(description = "Số CMND/ mã số thuế mua (Với trường hợp dbhm = C)")
        private String cmtm;
        @Schema(description = "Số mobi khách hàng mua (Với trường hợp dbhm = C)")
        private String mobim;
        @Schema(description = "Email khách hàng mua (Với trường hợp dbhm = C)")
        private String emailm;
        @Schema(description = "Ngày sinh khách hàng mua. Theo định dạng dd/mm/yyyy (Với trường hợp dbhm = C)")
        private String ng_sinhm;
        @Schema(description = "Địa chỉ khách hàng mua ( mặc định là số nhà) (Với trường hợp dbhm = C)")
        private String dchim;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GcnMicCareTtsk {
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String ck;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String cc;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String cu;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(hidden = true)
        private String tt1C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt2C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt3C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt4C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt5C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt6C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt7C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt8C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt9C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt10C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt11C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt12C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt13C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt14C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt15C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt16C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt17C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt18C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt19C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt20C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt21C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt22C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt23C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt24C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt25C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt26C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt27C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt28C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt29C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt30C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt31C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt32C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt33C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt34C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt35C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt36C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt37C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt38C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt39C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt40C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt41C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt42C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt43C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt44C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt45C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt46C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt47C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt48C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt49C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt50C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt51C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt52C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt53C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt54C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt55C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt56C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt57C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt58C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt59C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt60C;
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt61C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt62C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt63C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt64C;
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt65C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt66C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt67C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt68C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt69C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt70C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt71C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt72C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt73C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt74C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt75C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt76C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt77C = "K";
        @Schema(hidden = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String tt78C = "K";
        @Schema(hidden = true)
        private String bkhactxt = "";
    }

}
