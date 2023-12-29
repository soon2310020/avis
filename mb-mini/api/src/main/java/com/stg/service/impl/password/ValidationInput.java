package com.stg.service.impl.password;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ValidationInput {
    private String password;
    private String email;

    public ValidationInput(String password, String email) {
        this.password = password;
        this.email = email;
    }
}
