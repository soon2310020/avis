package com.stg.service3rd.mbal.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RelationshipPolicyHolderType {
//    POLICY_HOLDER, INSURED_PERSON, COUPLE, CHILDREN, PARENT, MOTHER, FATHER,
//    OTHER /* => set lại = POLICY_HOLDER */

    POLICY_HOLDER("1"), INSURED_PERSON("1"), COUPLE("2"), CHILDREN("4"),
    PARENT("2"), MOTHER("2"), FATHER("2"),
    OTHER("6") /* => set lại = POLICY_HOLDER */
   ;

    public final String micRelationship;
}
