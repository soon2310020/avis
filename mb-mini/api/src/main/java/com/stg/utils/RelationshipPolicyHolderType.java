package com.stg.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RelationshipPolicyHolderType {
    POLICY_HOLDER("1"), INSURED_PERSON("1"), COUPLE("2"), CHILDREN("4"),
    PARENT("2"), MOTHER("2"), FATHER("2");

    public final String micRelationship;
}
