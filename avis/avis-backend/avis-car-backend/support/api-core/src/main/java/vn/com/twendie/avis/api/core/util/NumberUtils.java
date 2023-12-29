package vn.com.twendie.avis.api.core.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

@Component
public class NumberUtils {

    private final static String PATTERN_DOT = "###.###.###";
    private final static String PATTERN_COMMA = "###,###,###";

    public String format(Object number){
        return format(PATTERN_COMMA, number);
    }

    public String format(String pattern, Object number){
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        decimalFormat.applyPattern(PATTERN_COMMA);
        String response = decimalFormat.format(number);
        if(PATTERN_DOT.equals(pattern)){
            response = response.replaceAll(",", ".");
        }
        return response;
    }

    public String removeBeforeDotIfGt0(BigDecimal bigDecimal){
        String input = String.valueOf(bigDecimal);
        String [] splitCount = input.split("\\.");
        if(splitCount.length == 2 && Integer.parseInt(splitCount[1]) == 0){
            input = splitCount[0];
        }
        return input;
    }


    public static void main(String[] args) {
        System.out.println(new NumberUtils().format(PATTERN_DOT, 100000000000l));
    }
}
