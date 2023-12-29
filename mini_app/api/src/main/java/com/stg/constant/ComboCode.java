package com.stg.constant;

public enum ComboCode {
	SIGNATURE, SPECIAL_OFFER, MUST_TRY;

	public static String getComboName(ComboCode comboCode){
		if(ComboCode.SIGNATURE.equals(comboCode)){
			return "Gói Sống vui khỏe";
		} else if (ComboCode.SPECIAL_OFFER.equals(comboCode)) {
			return "Gói Sống cân bằng";
		} else if (ComboCode.MUST_TRY.equals(comboCode)) {
			return "Gói Sống thảnh thơi";
		} else {
			return "";
		}
	}
}
