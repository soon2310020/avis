package com.stg.constant.quotation;

public enum QuotationState {
	CREATED, CONFIRMED, SUBMITTED, COMPLETED,
	UN_MATCH,
	RE_CREATED, RE_CONFIRMED, RE_SUBMITTED, RE_COMPLETED // from customer edit or info not-match
	;

	public static String toTextCode(QuotationState state) {
		if (state == null) return "1001 - None";

		switch (state) {
			case CREATED:
				return "0001 - Đã tạo";
			case RE_CREATED:
				return "0002 - Đã tạo mới";
			case CONFIRMED:
				return "0003 - Đã xác nhận";
			case RE_CONFIRMED:
				return "0004 - Đã xác nhận";
			case SUBMITTED:
				return "0005 - Đã gửi";
			case RE_SUBMITTED:
				return "0006 - Đã gửi";
			case COMPLETED:
				return "0007 - Đã hoàn thành";
			case RE_COMPLETED:
				return "0008 - Đã hoàn thành";
			case UN_MATCH: return "1002 - Không trùng khớp thông tin";
			default: return "1003 - Không xác định";
		}
	}
}
