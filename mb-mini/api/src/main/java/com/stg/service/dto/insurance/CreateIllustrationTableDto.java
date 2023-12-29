package com.stg.service.dto.insurance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateIllustrationTableDto {

    @Schema(description = "ID của khách hàng", required = true, example = "1")
    private Long customerId;

    @Schema(description = "Type gói bảo hiểm. Nếu FIX thì micPackage va mbalPackage null, con CUSTOM thi insurancePackage", required = true)
    @Pattern(regexp = "FIX_COMBO|FREE_STYLE", message = "Type must be FIX_COMBO or FREE_STYLE")
    @NotNull(message = "Thông tin type bắt buộc nhập")
    private String type;

    @Schema(description = "ID gói bảo hiểm", defaultValue = "1-16", required = true)
    @NotNull(message = "ID của gói bảo hiểm bắt buộc nhập")
    private Integer insurancePackageId;

    @Schema(description = "ID Gói bảo hiểm MIC cho CUSTOM type")
    private Integer micPackageId;

    @Schema(description = "ID Gói bảo hiểm MBAL cho CUSTOM type")
    private Integer mbalPackageId;

    @Schema(description = "Combo 3 cau hoi va cau hoi khac")
    @Valid
    private ComboSmallQuestion comboSmallQuestion;

    @Schema(description = "Combo 12 cau hoi")
    @Valid
    private ComboBigQuestion comboBigQuestion;

    @NotNull(message = "Giá trị Package Code bắt buộc nhập")
    private String packageCode;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String deathNoAccidentFrom;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String strDeathNoAccidentFrom;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String deathNoAccidentTo;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String strDeathNoAccidentTo;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String deathAccident;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String strDeathAccident;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String deathAccidentYesTraffic;
    @NotNull(message = "Thông tin bắt buộc nhập")
    private String deathAccidentNoTraffic;

    private String supInpatientHospitalFee;

    @NotNull(message = "Giá trị phí bảo hiển bắt buộc nhập")
    private String insuranceFee;

    @NotNull(message = "Giá trị phí cơ bản MBAL bắt buộc nhập")
    private String baseInsuranceFee;

    private String topupInsuranceFee;

    private String payFrequency;

    private String timeFrequency;

    @NotNull(message = "Giá trị Amount bắt buộc nhập")
    private long amount;

    private List<PavTableDto> pavTables = new ArrayList<>();

    // Tổng phí bảo hiểm MBAL + MIC
    private String mixInsuranceFee;

    private Common.GcnMicCareDkbs micCareDkbs;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class ComboSmallQuestion {
        @Schema(description = "question1")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c1;
        @Schema(description = "question2")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c2;
        @Schema(description = "question3")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String c3;
        @Schema(description = "Câu hỏi khác")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String oqs;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class ComboBigQuestion {
        @Schema(description = "cau 1")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c1")
        @Valid
        private QuestionOne c1;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "cau 2_a")
        @Valid
        private DefaultQuestion c2a;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "cau 2_b")
        @Valid
        private DefaultQuestion c2b;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private QuestionTwoC c2c;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private QuestionThree c3;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private QuestionFour c4;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private DefaultQuestion c5;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private QuestionSix c6;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private QuestionSeven c7;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private QuestionEight c8;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Valid
        private QuestionNine c9;

        @Schema(description = "Dành cho nữ. Số con hiện tại")
        @Min(0)
        @Max(99)
        private int c10a;
        @Schema(description = "Dành cho nữ")
        @Valid
        private QuestionTenB c10b;
        @Schema(description = "Dành cho nữ")
        @Valid
        private DefaultQuestion c10c;
        @Schema(description = "Dành cho nữ")
        @Valid
        private DefaultQuestion c10d;
        @Schema(description = "Dành cho nữ")
        @Valid
        private DefaultQuestion c10e;
        @Valid
        private DefaultQuestionEleven c11a;
        @Valid
        private DefaultQuestionEleven c11b;
        @Valid
        private OtherEleven c11c;

        @Schema(description = "Câu hỏi khác")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String oqs;
        @Schema(description = "Danh sách URL 15 files max")
        private List<String> files;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionOne {
        @Schema(description = "Cân nặng")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Min(1)
        private Double weight;
        @Schema(description = "Chiều cao")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Min(1)
        private Double height;
        @Schema(description = "Tăng hoặc giảm hoặc không thay đổi cân nặng")
        private String change;
        @Schema(description = "Giá trị tăng/giảm")
        private String changeValue;
        @Schema(description = "Lý do tăng / giảm")
        private String reasonChange;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class DefaultQuestion {
        @Schema(description = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Chi tiết nếu chọn có")
        private String detail;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionTwoC {
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String response;
        @Schema(description = "Quốc gia")
        private String national;
        @Schema(description = "Chi tiết nếu chọn có")
        private String detail;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionThree {
        @Schema(description = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Loại thuốc")
        private String type;
        @Schema(description = "Số lượng sử dụng hàng ngày (điếu/ngày)*")
        private String numInDay;
        @Schema(description = "Số năm sử dụng tính đến hiện tại (năm)*")
        private String numOfYears;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionFour {
        @Schema(description = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Uống rượu hoặc bia", example = "rượu")
        private String type;
        @Schema(description = "Số lượng uống mỗi lần (ml)*")
        private String count;
        @Schema(description = "Tần xuất uống Hàng ngày/Hàng tuần/Thỉnh thoảng")
        private String frequency;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionSix {
        @Schema(description = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Danh sách thành viên trong gia đình")
        private List<Member> members;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionSeven {
        @Schema(description = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        private DefaultQuestion c7a;
        private DefaultQuestion c7b;
        private DefaultQuestion c7c;
        private DefaultQuestion c7d;
        private DefaultQuestion c7e;
        private DefaultQuestion c7f;
        private DefaultQuestion c7g;
        private DefaultQuestion c7h;
        private DefaultQuestion c7i;
        private DefaultQuestion c7j;
        private DefaultQuestion c7k;
        private DefaultQuestion c7l;
        private DefaultQuestion c7m;
        private DefaultQuestion c7n;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class Member {
        @Schema(description = "Họ và tên")
        @NotNull
        private String fullName;
        @Schema(description = "Mối quan hệ với BMBH*")
        private String relation;
        @Schema(description = "Tuổi còn sống/đã mất*")
        private int age;
        @Schema(description = "Tình trạng sức khoẻ hoặc Nguyên nhân tử vong (nếu đã chết)*")
        private String status;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionEight {
        @Schema(description = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Loại thuốc", example = "true")
        private String medicineType;
        @Schema(description = "Lý do sử dụng")
        private String reason;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionNine {
        @Schema(description = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "chi tiết kết quả bình thường")
        private DefaultQuestion c9a;
        @Schema(description = "chi tiết kết quả bất thường")
        private DefaultQuestion c9b;
        @Schema(description = "chi tiết bệnh lý, triệu chứng bất thường")
        private DefaultQuestion c9c;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class QuestionTenB {
        @Schema(description = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Lần mang thai thứ mấy", example = "1, 2, 3")
        private String version;
        @Schema(description = "Ngày dự sinh format dd/mm/yyyy", example = "22/12/2023")
        private String duSing;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class DefaultQuestionEleven {
        @Schema(description = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        private List<InsuranceData> insuranceData;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class OtherEleven {
        @Schema(description = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        private List<InsuranceRequest> insuranceRequests;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class InsuranceData {
        @Schema(description = "Tên công ty bảo hiểm*")
        private String companyName;
        @Schema(description = "Số hợp đồng*")
        private String insuranceNo;
        @Schema(description = "Số tiền bảo hiểm*")
        private String insuranceFee;
        @Schema(description = "Lý do ...*")
        private String reason;
        @Schema(description = "Ngày/tháng/năm *, Format: dd/mm/yyyy")
        private String dateTime;
        @Schema(description = "Tình trạng*")
        private String status;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class InsuranceRequest {
        @Schema(description = "Tên công ty bảo hiểm*")
        private String companyName;
        @Schema(description = "Số hợp đồng*")
        private String insuranceNo;
        @Schema(description = "Số tiền bảo hiểm*")
        private String insuranceFee;
        @Schema(description = "Ngày/tháng/năm *, Format: dd/mm/yyyy")
        private String dateTime;
    }

}
