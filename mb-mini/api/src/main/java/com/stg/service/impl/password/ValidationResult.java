package com.stg.service.impl.password;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ValidationResult {
    private boolean valid;
    private List<String> validationMessages;
}
