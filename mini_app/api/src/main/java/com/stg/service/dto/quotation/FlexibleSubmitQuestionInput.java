package com.stg.service.dto.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

import static com.stg.service3rd.mic.utils.MicUtil.YES;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleSubmitQuestionInput {

    @NotNull(message = "Giá trị assuredId bắt buộc nhập")
    @ApiModelProperty(name = "assuredId của người hưởng bảo hiểm", required = true)
    private String assuredId;

    @ApiModelProperty(notes = "Combo 3 cau hoi va cau hoi khac")
    @Valid
    private ComboSmallQuestion comboSmallQuestion;

    @ApiModelProperty(notes = "Combo 12 cau hoi")
    @Valid
    private ComboBigQuestion comboBigQuestion;

    private boolean from3To12;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class ComboSmallQuestion {
        @ApiModelProperty(notes = "question1")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c1")
        private String c1;
        @ApiModelProperty(notes = "question2")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c2")
        private String c2;
        @ApiModelProperty(notes = "question3")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c3")
        private String c3;
        @ApiModelProperty(notes = "Câu hỏi khác")
        @Pattern(regexp = "C|K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("oqs")
        private String oqs;

//        public String getC1() {
//            if("NO".equals(c1)) return "K";
//            return c1;
//        }
//
//        public String getC2() {
//            if("NO".equals(c2)) return "K";
//            return c2;
//        }
//
//        public String getC3() {
//            if("NO".equals(c3)) return "K";
//            return c3;
//        }
//
//        public String getOqs() {
//            if("NO".equals(oqs)) return "K";
//            return oqs;
//        }
//
//        public void setC1(String c1) {
//            if("NO".equals(c1)) {
//                this.c1 = "K";
//            }
//        }
//
//        public void setC2(String c2) {
//            if("NO".equals(c2)) {
//                this.c2 = "K";
//            }
//        }
//
//        public void setC3(String c3) {
//            if("NO".equals(c3)) {
//                this.c3 = "K";
//            }
//        }
//
//        public void setOqs(String oqs) {
//            if("NO".equals(oqs)) {
//                this.oqs = "K";
//            }
//        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class ComboBigQuestion {
        @ApiModelProperty(notes = "cau 1")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @JsonProperty("c1")
        @Valid
        private QuestionOne c1;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(notes = "cau 2_a")
        @Valid
        private DefaultQuestion c2a;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(notes = "cau 2_b")
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

        @ApiModelProperty(notes = "Dành cho nữ. Số con hiện tại")
//        @Min(0)
//        @Max(99)
        private String c10a;
        @ApiModelProperty(notes = "Dành cho nữ")
        @Valid
        private QuestionTenB c10b;
        @ApiModelProperty(notes = "Dành cho nữ")
        @Valid
        private DefaultQuestion c10c;
        @ApiModelProperty(notes = "Dành cho nữ")
        @Valid
        private DefaultQuestion c10d;
        @ApiModelProperty(notes = "Dành cho nữ")
        @Valid
        private DefaultQuestion c10e;
        @Valid
        private DefaultQuestionEleven c11a; // otherDeclinedContracts
        @Valid
        private DefaultQuestionEleven c11b; // otherInsuranceClaims
        @Valid
        private OtherEleven c11c; //otherActiveContracts

        @ApiModelProperty(notes = "Câu hỏi khác")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String oqs;

        @ApiModelProperty(notes = "Danh sách URL 15 files max")
        private List<String> files;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionOne {
        @ApiModelProperty(notes = "Cân nặng")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Min(1)
        private Double weight;
        @ApiModelProperty(notes = "Chiều cao")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Min(1)
        private Double height;
        @ApiModelProperty(notes = "Tăng hoặc giảm hoặc không thay đổi cân nặng")
        @Pattern(regexp = "INCREASE|DECREASE|K", message = "Chi chọn INCREASE (tăng) hoặc DECREASE(Giảm) hoặc K (Không)")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String change;
        @ApiModelProperty(notes = "Giá trị tăng/giảm")
        private String changeValue;
        @ApiModelProperty(notes = "Lý do tăng / giảm")
        private String reasonChange;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class DefaultQuestion {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        //@Pattern(regexp = "[CKNO]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "Chi tiết nếu chọn có")
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
    @ApiModel
    @Valid
    public static class QuestionTwoC {
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String response;
        @ApiModelProperty(notes = "Quốc gia")
        private String national;
        @ApiModelProperty(notes = "Chi tiết nếu chọn có")
        private String detail;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionThree {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "Loại thuốc")
        private String type;
        @ApiModelProperty(notes = "Số lượng sử dụng hàng ngày (điếu/ngày)*")
        private String numInDay;
        @ApiModelProperty(notes = "Số năm sử dụng tính đến hiện tại (năm)*")
        private String numOfYears;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionFour {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "Uống rượu hoặc bia", example = "rượu")
        private String type;
        @ApiModelProperty(notes = "Số lượng uống mỗi lần (ml)*")
        private String count;
        @ApiModelProperty(notes = "Tần xuất uống Hàng ngày/Hàng tuần/Thỉnh thoảng")
        private String frequency;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionSix {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "Danh sách thành viên trong gia đình")
        private List<FlexibleCommon.FamilyMembersInput> members;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionSeven {
        @ApiModelProperty(notes = "C hoặc K")
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
    @ApiModel
    @Valid
    public static class QuestionEight {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "Loại thuốc", example = "true")
        private String medicineType;
        @ApiModelProperty(notes = "Lý do sử dụng")
        private String reason;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionNine {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "chi tiết kết quả bình thường")
        private DefaultQuestion c9a;
        @ApiModelProperty(notes = "chi tiết kết quả bất thường")
        private DefaultQuestion c9b;
        @ApiModelProperty(notes = "chi tiết bệnh lý, triệu chứng bất thường")
        private DefaultQuestion c9c;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class QuestionTenB {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        @ApiModelProperty(notes = "Lần mang thai thứ mấy", example = "1, 2, 3")
        private String version;
        @ApiModelProperty(notes = "Ngày dự sinh format dd/mm/yyyy", example = "22/12/2023")
        private String duSing;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class DefaultQuestionEleven {
        @ApiModelProperty(notes = "C hoặc K")
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
    @ApiModel
    @Valid
    public static class OtherEleven {
        @ApiModelProperty(notes = "C hoặc K")
        @NotNull
        @Pattern(regexp = "[CK]", message = "Chỉ nhập C hoặc K")
        private String response;
        private List<FlexibleCommon.OtherActiveContracts> otherActiveContracts;
        private List<FlexibleCommon.OtherActiveContracts> insuranceData;
    }

}
