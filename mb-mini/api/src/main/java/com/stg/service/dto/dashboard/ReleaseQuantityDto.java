package com.stg.service.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ReleaseQuantityDto {
    private Quantity illustration;
    private Quantity contract;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Quantity {
        private Long monday;
        private Long tuesday;
        private Long wednesday;
        private Long thursday;
        private Long friday;
        private Long saturday;
        private Long sunday;
    }

}
