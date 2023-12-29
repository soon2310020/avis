package com.stg.service.impl.password;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ValidationRules {
    private Integer minimumLength;
    private Integer maximumLength;
    private Integer minimumLowerCaseCount;
    private Integer minimumUpperCaseCount;
    private Integer minimumNumberCount;
    private Integer minimumSymbolCount;
    private Integer maximumRepeatingCharactersCount;
    private Boolean mustNotContainUsernameEnabled;
}
