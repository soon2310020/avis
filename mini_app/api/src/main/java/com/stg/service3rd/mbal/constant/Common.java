package com.stg.service3rd.mbal.constant;

import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.UUID;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Common {

    private static final String MONEY_PATTERN = "###,###.###";
    private static final String VND = " VND";
    public static final String PHONE_PATTERN = "(84|0[3|5|7|8|9])+([0-9]{8})\\b|(84|0[3|5|7|8|9])+([0-9]{9})\\b";
    public static final String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public static final String YES = "C";

    public static final String ZERO_FEE = "0 VND";

    public static String generateUUIDId(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }

    public static String formatCurrency(BigDecimal money) {
        if (money == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        DecimalFormat decimalFormat = new DecimalFormat(MONEY_PATTERN, symbols);
        return (decimalFormat.format(money) + VND).replace(",", ".");
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Quizzes {
        private UUID id;
        @Size(max = 50)
        private String label;
        private List<Answers> answers;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Answers {
        private UUID id;
        @Size(max = 255)
        private String value;
        @Min(1)
        @Max(10)
        private Integer ordinal;

        public String getValue() {
            if ("K".equals(this.value)) {
                return "NO";
            }
            if ("C".equals(this.value)) {
                return "YES";
            }
            return value;
        }
    }
}
