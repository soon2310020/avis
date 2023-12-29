package vn.com.twendie.avis.api.model.response;

import lombok.Builder;
import lombok.Data;
import org.apache.poi.ss.usermodel.Workbook;

@Data
@Builder
public class WorkbookWrapper {

    private Workbook workbook;
    private int lastRowIndexWithData;
}
