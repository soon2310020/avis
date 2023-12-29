package com.stg.repository.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class SearchCriterias {
    private List<SearchCriteria> criterias;
}
