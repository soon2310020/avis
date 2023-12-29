package com.stg.service.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AllCategoryRespDto {

    private List<Occupation> occupations;
    private List<Nationality> nationalities;
    private List<Question> questions;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Occupation {
        private String id;
        private String accupationNameVn;
        private String accupationNameEn;
        private int groupAccupationClass;
        private int orderNo;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Nationality {
        private String id;
        private String activeStatus;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Question {
        private String id;
        private String activeStatus;
        private String name;
    }
}
