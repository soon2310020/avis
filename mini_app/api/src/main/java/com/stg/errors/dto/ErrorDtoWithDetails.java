package com.stg.errors.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@NoArgsConstructor
public class ErrorDtoWithDetails extends ErrorDto {
    public static final String DETAILED_TYPE = "detailed";

    private List<ErrorDetailDto> details = new ArrayList<>();

    public ErrorDtoWithDetails(HttpStatus httpStatus, Exception e, List<ErrorDetailDto> details) {
        super(httpStatus, e);
        setDetails(details);
    }

    public ErrorDtoWithDetails(HttpStatus httpStatus, String message, List<ErrorDetailDto> details) {
        super(httpStatus, message);
        setDetails(details);
    }

    public void addFieldError(ErrorDetailDto detail) {
        getDetails().add(detail);
    }
}
