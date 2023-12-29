package com.stg.repository.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Join {
    private String table;
    private String alias;
    private String refer;
}
