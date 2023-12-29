package vn.com.twendie.avis.data.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContractTimeUserPolicyEnum {

    NOT_USING(0, "Không áp dụng"),
    USING(1, "Có áp dụng");

    private final Integer id;

    private final String name;

}
