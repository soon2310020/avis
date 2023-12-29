package saleson.api.imageupload.dto;

import lombok.Data;

@Data
public class ImageUploadIn {
    private Long companyId;
    private Long plantId;
    private String equipmentCode;
    private String ToolingOrSenSorCode;
    private String companyName;
    private String plantName;
    private String partName;
}
