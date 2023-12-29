package com.stg.service.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentDto {
    @JsonProperty("segment")
    private String segment; // phân khúc

    @JsonProperty("gain")
    private String gain; // phân lợi
}