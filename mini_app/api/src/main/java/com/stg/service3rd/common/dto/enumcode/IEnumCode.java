package com.stg.service3rd.common.dto.enumcode;

public interface IEnumCode {
    String getCode();

    String getMessageVn();

    /**
     * Override when necessary
     */
    default String getMessageOrigin() {
        return this.getMessageVn();
    }

    default boolean equalCode(String code) {
        return this.getCode().equals(code);
    }
}
