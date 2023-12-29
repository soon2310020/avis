package com.stg.utils.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressMark {
    private AddressType type;
    private String value;

    /***/
    public enum AddressType {
        STREET,
        WARD,
        DISTRICT,
        PROVINCE,
        UNKNOWN
    }
}
