package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.common.util.DataUtils;
import saleson.common.util.StringUtils;
import saleson.model.Part;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartDTO {

    @JsonProperty("category_id")
    private Long categoryId;//parent of category
    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("project_id")
    private Long projectId;        // categoryId
    @JsonProperty("project_name")
    private String projectName;		// categoryName

    private Long id;

    @JsonProperty("part_code")
    private String partCode;
    private String name;

	@JsonProperty("resin_code")
	private String resinCode;
	@JsonProperty("resin_grade")
	private String resinGrade;
	@JsonProperty("design_revision_level")
	private String designRevision;

    @JsonProperty("volume")
    private String size;

    @JsonProperty("volume_width")
    private Double sizeWidth;
    @JsonProperty("volume_length")
    private Double sizeLength;
    @JsonProperty("volume_height")
    private Double sizeHeight;

    @JsonProperty("volume_unit")
    @Enumerated(EnumType.STRING)
    private SizeUnit sizeUnit;

    private String weight;
    @JsonProperty("weight_unit")
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;
    private boolean enabled;


public static PartDTO convertToDTO(Part part){
    PartDTO partDTO = DataUtils.mapper.map(part,PartDTO.class);
    partDTO.setCategoryId(null);
    partDTO.setProjectId(part.getCategoryId());
/*
    if(part.getCategory()!=null && part.getCategory().getParent()!=null){
        partDTO.setCategoryId(part.getCategory().getParentId());
    }
*/
    if (part.getCategory() != null) {
        partDTO.setCategoryId(part.getCategory().getGrandParentId());
    }
//		if(moldPart.getPart()!=null){
//			if(moldPart.getPart().getCategory()!=null)
////			moldPartDTO.setCategoryId(moldPart.getPart().getCategory().getId());
//		}

    try{
        if(!StringUtils.isEmpty(part.getSize())){
            String[] sizeArr=part.getSize().toLowerCase().split("x");
            if(sizeArr.length>=3){
                partDTO.setSizeWidth(Double.valueOf(sizeArr[0].trim()));
                partDTO.setSizeLength(Double.valueOf(sizeArr[1].trim()));
                partDTO.setSizeHeight(Double.valueOf(sizeArr[2].trim()));
            }
        }
    }catch (Exception e){
        e.printStackTrace();
    }
    return partDTO;
}
}
