package com.stg.utils;

import java.math.BigDecimal;

public final class Constants {

    public static final String MERCHANT_CODE_HEADER = "MERCHANT_CODE";

    public static final String MERCHANT_SECRET_HEADER = "MERCHANT_SECRET";

    public static final String MIC_CODE_01 = "01"; // Quan hệ với người mua bảo hiểm bắt buộc nhập

    public static final String MIC_CODE_00 = "00";

    public static final String MIC_CODE_02 = "02";

    public static final String FILE_EXTENSION = ".xlsx";

    public static final String FILE_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String MS_WRONG_EXTENSION_EXCEL = "File wrong extension or excel sheet type";

    public static final String IMPORT_ERROR = "Error when import";

    public enum PackageType {
        FIX_COMBO, FREE_STYLE, FLEXIBLE, MBAL, MIC, ONEID
    }

    public static final String MIC = "MIC";
    public static final String MBAL = "MBAL";
    public static final String BEARER = "Bearer ";
    public static final String MBAL_ACCESS_TOKEN_HEADER = "mbalAccessToken";

    public static final String STATUS_OK = "200";
    public static final Long MIN_AMOUNT = 0L;
    public static final Long MAX_AMOUNT = 10000000000L;

    public static final String ULL_V2 = "ULL_V2";
    public static final String ULL_V3 = "ULL_V3";
    public static final String ULL_VERSION = "ULL_VERSION";
    public static final String PAID = "PAID";
    public static final String PENDING = "PENDING";
    public static final String WAITING = "WAITING";
    public static final String MBAL_PRODUCT_NAME_V2 = "Vững Tương Lai";
    public static final String MBAL_PRODUCT_NAME_V3 = "Sống Trọn Ước Mơ";

    public enum HOST_PARTY {
        MBAL, MIC, MB, MB_CARD, BAAS, CRM, TOOL_CRM, ONE_ID,HCM
    }

    public enum Status {
        SUCCESS, FAIL
    }

    public enum Source {
        MBAL
    }

    public static final String FIX_COMBO = "FIX_COMBO";
    public static final String FREE_STYLE = "FREE_STYLE";
    public static final String HAPPY_TYPE = "HAPPY";
    public static final String HEALTHY_TYPE = "HEALTHY";

    public static final String TEN_YEARS = "10 năm";
    public static final String FIVE_YEARS = "5 năm";

    public static final String PHONE_PATTERN = "(84|0[3|5|7|8|9])+([0-9]{8})\\b|(84|0[3|5|7|8|9])+([0-9]{9})\\b";
    public static final String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public static final String MIC_AMOUNT = "MIC_AMOUNT";
    public static final String MBAL_AMOUNT = "MBAL_AMOUNT";
    public static final String TOTAL_AMOUNT = "TOTAL_AMOUNT";
    public static final String IC_CODE = "IC_CODE";
    public static final String IC_NAME = "IC_NAME";

    public static final String EMAIL_SUCCESS = "Success";

    public static final String MB_SESSION_ID = "MB_SESSION_ID";
    public static final String MBAL_HDBH = "MBAL_HDBH";
    public static final String MBAL_APP_NO = "MBAL_APP_NO";
    public static final String MIC_GCNBH = "MIC_GCNBH";
    public static final String CIF = "CIF";
    public static final String CREATE_ORDER_ID = "CREATE_ORDER_ID";
    public static final String MIC_ID = "MIC_ID";
    public static final String MBAL_ID = "MBAL_ID";
    public static final String INSURANCE_PACKAGE_ID = "INSURANCE_PACKAGE_ID";
    public static final String MIX_PACKAGE_NAME = "MIX_PACKAGE_NAME";
    public static final String HOOK_TYPE_COE = "HOOK_TYPE_COE";

    // Flexible
    public static final String TRANSACTION_TYPE_KEY = "TRANSACTION_TYPE";
    public static final String MIC_ADDITIONAL_PRODUCT = "MIC_ADDITIONAL_PRODUCT";
    public static final String TRANS_FLEXIBLE_TYPE = "TRANS_FLEXIBLE";
    public static final String PROCESS_ID_KEY = "PROCESS_ID";
    public static final String YES = "C";

    public static final String EVERY_YEAR = "Hàng năm";

    public static final String ZERO_FEE = "0 VND";

    public static final String INSTALLMENT_KEY = "IS_INSTALLMENT";
    public static final String BAAS_SUCCESS_CODE = "000";

    public static final BigDecimal TWO_BILL_MAX_AMOUNT = BigDecimal.valueOf(2000000000);
    public static final String MALE_NUM = "0,2,4,6,8";
    public static final String FEMALE_NUM = "1,3,5,7,9";

    public static final Integer MAX_PAGE_SIZE = 50;

    public static final String COLON = ":";

    public static final String NO = "K";

}
