package com.stg.service3rd.toolcrm.dto.resp.quote;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

import static com.stg.utils.Constants.YES;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleSubmitQuestionInput {

    @NotNull(message = "Giá trị assuredId bắt buộc nhập")
    @Schema(name = "assuredId của người hưởng bảo hiểm", required = true)
    private String assuredId;

    @Schema(description = "Combo 3 cau hoi va cau hoi khac")
    @Valid
    private ComboSmallQuestion comboSmallQuestion;

    @Schema(description = "Combo 12 cau hoi")
    @Valid
    private ComboBigQuestion comboBigQuestion;

    private boolean from3To12;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class ComboSmallQuestion {
        @Schema(description = "question1")
        //@Pattern(regexp = "C|K|NO")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c1")
        private String c1;
        @Schema(description = "question2")
        //@Pattern(regexp = "C|K|NO")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c2")
        private String c2;
        @Schema(description = "question3")
        //@Pattern(regexp = "C|K|NO")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c3")
        private String c3;
        @Schema(description = "Câu hỏi khác")
        //@Pattern(regexp = "C|K|NO")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("oqs")
        private String oqs;

        public String getC1() {
            if("NO".equals(c1)) return "K";
            return c1;
        }

        public String getC2() {
            if("NO".equals(c2)) return "K";
            return c2;
        }

        public String getC3() {
            if("NO".equals(c3)) return "K";
            return c3;
        }

        public String getOqs() {
            if("NO".equals(oqs)) return "K";
            return oqs;
        }

        public void setC1(String c1) {
            if("NO".equals(c1)) {
                this.c1 = "K";
            }
        }

        public void setC2(String c2) {
            if("NO".equals(c2)) {
                this.c2 = "K";
            }
        }

        public void setC3(String c3) {
            if("NO".equals(c3)) {
                this.c3 = "K";
            }
        }

        public void setOqs(String oqs) {
            if("NO".equals(oqs)) {
                this.oqs = "K";
            }
        }
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
//        @Min(0)
//        @Max(99)
        private String c10a;
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
        private DefaultQuestionEleven c11a; // otherDeclinedContracts
        @Valid
        private DefaultQuestionEleven c11b; // otherInsuranceClaims
        @Valid
        private OtherEleven c11c; //otherActiveContracts

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
        @Pattern(regexp = "INCREASE|DECREASE|K", message = "Chi chọn INCREASE (tăng) hoặc DECREASE(Giảm) hoặc K (Không)")
        @NotNull(message = "Thông tin bắt buộc nhập")
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
        @Pattern(regexp = "[CKNO]", message = "Chỉ nhập C hoặc K")
        private String response;
        @Schema(description = "Chi tiết nếu chọn có")
        private String detail;

        public String getResponse() {
            if ("NO".equals(response)) return "K";
            else if (YES.equals(response)) return "C";
            return response;
        }

        public void setResponse(String response) {
            if ("NO".equals(response)) {
                this.response = "K";
            }
            if (YES.equals(response)) {
                this.response = "C";
            }
        }
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
        private List<FlexibleCommon.FamilyMembersInput> members;
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
        @NotNull(message = "Thông tin bắt buộc nhập")
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
        @NotNull(message = "Thông tin bắt buộc nhập")
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
        private List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherActiveContracts;
        private List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> insuranceData;
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
        private List<FlexibleCommon.OtherActiveContracts> otherActiveContracts;
        private List<FlexibleCommon.OtherActiveContracts> insuranceData;
    }

}
