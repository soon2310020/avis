package com.stg.service.impl.password;

import org.passay.CharacterData;

public class SpecialCharacterData implements CharacterData {

    public static final SpecialCharacterData INSTANCE = new SpecialCharacterData();

    @Override
    public String getErrorCode() {
        return "INSUFFICIENT_SPECIAL";
    }

    @Override
    public String getCharacters() {
        return "!“#$%&‘()*+,-./:;<=>?@[]^_`{|}~";
    }
}
