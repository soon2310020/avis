package saleson.service.util;

import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.Property;
import saleson.common.util.StringUtils;
import saleson.model.Mold;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;

public class NumberUtils {
    public static Double roundOffNumber(Double number) {
        DecimalFormat df = new DecimalFormat(".00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(number));
    }
    public static Double roundToOneDecimalDigit(Double number) {
        DecimalFormat df = new DecimalFormat(".0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(number));
    }

    public static Integer convertToInteger(Object object) {
        if (object != null) {
            if (object instanceof BigInteger)
                return ((BigInteger) object).intValue();
            if (object instanceof BigDecimal)
                return Long.valueOf(Math.round(((BigDecimal) object).doubleValue())).intValue();
        }
        return null;
    }
    public static Long convertToLong(Object object) {
        if (object != null) {
            if (object instanceof BigInteger)
                return ((BigInteger) object).longValue();
            if (object instanceof BigDecimal)
                return Math.round(((BigDecimal) object).doubleValue());
        }
        return null;
    }

    public static Double convertToDouble(Object object) {
        if (object != null) {
            if (object instanceof BigInteger)
                return ((BigInteger) object).doubleValue();
            if (object instanceof BigDecimal)
                return ((BigDecimal) object).doubleValue();
        }
        return null;
    }

    public static Instant convertToInstant(Object object) {
        if (object != null) {
            if (object instanceof Timestamp) {
                return Instant.ofEpochMilli(((Timestamp) object).getTime());
            }
        }
        return null;
    }

    public static boolean isNumberOrNull(String str) {
        try {
            if (str == null || StringUtils.isEmpty(str.trim())) {
                return true; // String is null
            }
            str = str.trim(); // Trim the spaces from the string
            Integer.parseInt(str); // Attempt to parse String into an Integer
            return true; // String is an Integer
        } catch (NumberFormatException e) {
            try {
                Long.parseLong(str); // Attempt to parse String into a Long
                return true; // String is a Long
            } catch (NumberFormatException e2) {
                try {
                    Float.parseFloat(str); // Attempt to parse String into a Float
                    return true; // String is a Float
                } catch (NumberFormatException e3) {
                    try {
                        Double.parseDouble(str); // Attempt to parse String into a Double
                        return true; // String is a Double
                    } catch (NumberFormatException e4) {
                        return false; // String is not a number
                    }
                }
            }
        }
    }
    public static boolean isIntegerNumberOrNull(String str) {
        try {
            if (str == null || StringUtils.isEmpty(str.trim())) {
                return true; // String is null
            }
            str = str.trim(); // Trim the spaces from the string
            Integer.parseInt(str); // Attempt to parse String into an Integer
            return true; // String is an Integer
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int compareNumbers(Number num1, Number num2) {
        double val1 = num1.doubleValue();
        double val2 = num2.doubleValue();

        if (val1 < val2) {
            return -1;
        } else if (val1 > val2) {
            return 1;
        } else {
            return 0;
        }
    }
    public static void assertGtValue(Number value, String field,Number minValue) {
        assertGtValue(value, field,minValue, minValue.toString());
/*
        if(saleson.service.util.NumberUtils.compareNumbers(value,minValue)<=0){
            throw new BizException("greater_than", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", minValue.toString()));
        }
*/
    }
    public static void assertGtValue(Number value, String field,Number minValue, String field2) {
        if(compareNumbers(value,minValue)<=0){
            throw new BizException("greater_than", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", MessageUtils.get(field2, null)));
        }
    }
    public static void assertGteValue(Number value, String field,Number minValue) {
        assertGteValue(value, field,minValue, minValue.toString());
/*
        if(compareNumbers(value,minValue)<0){
            throw new BizException("greater_than_or_equal_to", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", minValue.toString()));
        }
*/
    }
    public static void assertGteValue(Number value, String field,Number minValue, String field2) {
        if(compareNumbers(value,minValue)<0){
            throw new BizException("greater_than_or_equal_to", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", MessageUtils.get(field2, null)));
        }
    }
    public static void assertLtValue(Number value, String field,Number maxValue) {
        assertLtValue(value, field,maxValue, maxValue.toString());
/*
        if(compareNumbers(value,maxValue)>=0){
            throw new BizException("less_than", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", maxValue.toString()));
        }
*/
    }
    public static void assertLtValue(Number value, String field,Number maxValue, String field2) {
        if(compareNumbers(value,maxValue)>=0){
            throw new BizException("less_than", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", MessageUtils.get(field2, null)));
        }
    }
    public static void assertLteValue(Number value, String field,Number maxValue) {
        assertLteValue(value, field,maxValue,maxValue.toString());
    }
    public static void assertLteValue(Number value, String field,Number maxValue, String field2) {
        if(compareNumbers(value,maxValue)>0){
            throw new BizException("less_than_or_equal_to", new Property("field1", MessageUtils.get(field, null)),
                new Property("field2", MessageUtils.get(field2, null)));
        }
    }
    public static void assertNumberOrNull(String model,String value, String field) {
            if (!isNumberOrNull(value))
                throw DataUtils.newDataValueInvalidException(model, field, value);
    }
    public static void assertIntegerNumberOrNull(String model,String value, String field) {
            if (!isIntegerNumberOrNull(value))
                throw DataUtils.newDataValueInvalidException(model, field, value);
    }

}
