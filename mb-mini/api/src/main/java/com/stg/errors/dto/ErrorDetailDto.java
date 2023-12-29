package com.stg.errors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a specific root cause of an error.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetailDto {
    private String target;
    private String message;
}
