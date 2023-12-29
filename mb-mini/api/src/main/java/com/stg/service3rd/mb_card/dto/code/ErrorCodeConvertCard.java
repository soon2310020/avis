package com.stg.service3rd.mb_card.dto.code;

public enum ErrorCodeConvertCard {
    SUCCESS("000", "Success"),
    PAN_NOT_CARD_NUMBER("001", "Pan is not card number"),
    GENERATE_TOKEN_FAILED("002", "Generate token failed"),
    INPUT_INVALID("009", "Input is invalid"),
    ;
    private final String code;
    private final String message;

    ErrorCodeConvertCard(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

