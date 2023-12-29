package vn.com.twendie.avis.api.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportConfig {

    public static final ExportConfig DEFAULT_EXPORT_CONFIG = ExportConfig.builder().columnIndex(0).build();

    private int keyRowIndex = 0;

    @Builder.Default
    private int sampleRowIndex = 2;

    private Integer columnIndex;

    private boolean copyRowHeight;

    private String sheetName;

}
