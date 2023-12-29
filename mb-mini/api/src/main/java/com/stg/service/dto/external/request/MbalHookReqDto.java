package com.stg.service.dto.external.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MbalHookReqDto {
    @NotNull
    @Schema(description = "id. Kiểu String, tối đa 30 ký tự. ID của giao dịch", required = true)
    @JsonProperty("id")
    private String id;
    @Schema(description = "merchant")
    @JsonProperty("merchant")
    private Merchant merchant;
    @Schema(description = "cif. Kiểu String, tối đa 45 ký tự. Mã khách hàng MB", required = true)
    @JsonProperty("cif")
    private String cif;
    @Schema(description = "Số tiền thực hiện giao dịch", required = true)
    @JsonProperty("amount")
    private Long amount;
    @Schema(description = "Kiểu String, tối đa 200 ký tự. Nội dung giao dịch", required = true)
    @JsonProperty("description")
    private String description;
    @Schema(description = "type", required = true)
    @JsonProperty("type")
    private Type type;
    @Schema(description = "successMessage")
    @JsonProperty("successMessage")
    private String successMessage;
    @Schema(description = "metadata")
    @JsonProperty("metadata")
    private String metadata;
    @Schema(description = "Kiểu Date. Thời điểm tạo giao dịch. Theo định dạng yyyy-MM-dd'T'HH:mm:ss (ISO 8601)")
    @JsonProperty("createdTime")
    private String createdTime;
    @Schema(description = "Kiểu Date. Thời điểm thanh toán. Theo định dạng yyyy-MM-dd'T'HH:mm:ss (ISO 8601)")
    @JsonProperty("paidTime")
    private String paidTime;
    @Schema(description = "Kiểu String, tối đa 45 ký tự. Trạng thái của giao dịch")
//    @Pattern(regexp = "PAID|PENDING")
    private String status;

    @Schema(description = "Kiểu String, tối đa 2000 ký tự. Nguồn tiền được khách hàng sử dụng để thanh toán")
//    @Pattern(regexp = "ACCOUNT|CARD")
    private String fundingSource;

    @Schema(description = "Kiểu String, tối đa 100 ký tự. Loại thẻ được khách hàng sử dụng để thanh toán")
    private String cardType;

    @Schema(description = "Kiểu String. Chuỗi mã hóa tạo ra dựa trên các tham số dưới đây:merchantCode+transactionId+typeCode+cif+totalAmount+status")
    private String checksum;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Merchant {
        @Schema(description = "Kiểu String, tối đa 100 ký tự. Mã đối tác", required = true)
        private String code;
        @Schema(description = "Kiểu String, tối đa 256 ký tự. Tên đối tác", required = true)
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Type {
        @Schema(description = "Kiểu String, tối đa 100 ký tự. Mã loại giao dịch", required = true)
        private String code;
        @Schema(description = "Kiểu String, tối đa 256 ký tự. Tên loại giao dịch", required = true)
        private String name;
        @Schema(description = "Kiểu Boolean. Cho phép thanh toán bằng thẻ hay không", required = true)
        private Boolean allowCard;
    }

    @Override
    public String toString() {
        return "MBAL Hook data {" +
                "id='" + id + '\'' +
                ", merchant=" + merchant +
                ", cif='" + cif + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", successMessage='" + successMessage + '\'' +
                ", metadata='" + metadata + '\'' +
                ", createdTime=" + createdTime +
                ", paidTime=" + paidTime +
                ", status='" + status + '\'' +
                ", fundingSource='" + fundingSource + '\'' +
                ", cardType='" + cardType + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}