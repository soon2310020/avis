package saleson.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import saleson.model.customField.CustomField;

import java.util.List;

@Data
@NoArgsConstructor
public class HeaderDTO {
    String code;
    String name;
    boolean required=false;
    boolean isCustomField=false;
    int columnWith = 4000;
    private String defaultValue;

    private List<HeaderDTO> subHeader = Lists.newArrayList();

    public HeaderDTO(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public HeaderDTO(String code, String name, boolean required) {
        this.code = code;
        this.name = name;
        this.required = required;
    }
    public HeaderDTO(String code, String name, int columnWith) {
        this.code = code;
        this.name = name;
        this.columnWith = columnWith;
    }

    public HeaderDTO(String code, String name, boolean required, List<HeaderDTO> subHeader) {
        this.code = code;
        this.name = name;
        this.required = required;
        this.subHeader = subHeader;
    }


    public HeaderDTO(CustomField customField){
        this.code = customField.getId().toString();
        this.name = customField.getFieldName();
        this.required = customField.getRequired();
        this.isCustomField=true;
        if(customField.getDefaultInput() != null && customField.getDefaultInput()) {
            this.defaultValue = customField.getDefaultInputValue();
        }

    }
    public Long getCustomFieldId(){
        Long id=null;
        try{
            if(isCustomField)
            id = Long.valueOf(code);
        }catch (Exception e){}
        return id;
    }
}
