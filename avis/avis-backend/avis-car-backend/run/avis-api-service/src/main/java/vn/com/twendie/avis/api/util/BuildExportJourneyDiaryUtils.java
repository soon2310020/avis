package vn.com.twendie.avis.api.util;

import org.apache.commons.lang.StringUtils;
import vn.com.twendie.avis.data.model.CodeValueModel;

import java.util.Collection;
import java.util.List;

public class BuildExportJourneyDiaryUtils {

    public static String convert(Collection<String> s) {
        if (s == null || s.size() == 0) return "";
        StringBuilder response = new StringBuilder();
        for (String data : s) {
            if (StringUtils.isNotEmpty(data)) {
                response.append("<br>")
                        .append(data)
                        .append("</br>");
            }
        }
        return response.toString();
    }

    public static Integer getCostByType(String code, List<CodeValueModel> codeValueModels) {
        for (CodeValueModel codeValueModel : codeValueModels) {
            if (codeValueModel != null && code.equals(codeValueModel.getCode())) {
                return codeValueModel.getValue() != null ? codeValueModel.getValue().intValue() : null;
            }
        }
        return null;
    }
}
