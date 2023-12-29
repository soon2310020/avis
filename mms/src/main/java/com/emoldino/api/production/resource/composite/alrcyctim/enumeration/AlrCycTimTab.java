package com.emoldino.api.production.resource.composite.alrcyctim.enumeration;

import lombok.Getter;

@Getter
public enum AlrCycTimTab {
    OUTSIDE_L1("Outside L1"), //
    OUTSIDE_L2("Outside L2");

    private final String title;

    AlrCycTimTab(String title) {
        this.title = title;
    }
}
