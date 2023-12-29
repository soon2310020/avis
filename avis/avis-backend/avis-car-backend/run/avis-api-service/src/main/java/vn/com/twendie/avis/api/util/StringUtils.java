package vn.com.twendie.avis.api.util;

import java.util.Collection;

public class StringUtils {
    public static String convertListToString(Collection<String> s){
        StringBuilder response = new StringBuilder();
        if(s == null || s.size() == 0) {
            return response.toString();
        }

        s.forEach(e -> {
            response.append(e)
                    .append(", ");
        });

        if(response.length() > 0){
            response.delete(response.length() - 2, response.length() -1);
        }
        return response.toString();
    }
}
