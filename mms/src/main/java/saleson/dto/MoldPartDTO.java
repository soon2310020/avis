package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.DataUtils;
import saleson.model.MoldPart;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldPartDTO {

/*
	@JsonProperty("category_id")
	private Long categoryId;
*/
	@JsonProperty("category_name")
	private String categoryName;

/*
	@JsonProperty("category_id")
	private Long projectId; 		// categoryId
*/
	@JsonProperty("project_name")
	private String projectName;		// categoryName

	@JsonProperty("part_id")
	private Long partId;

	@JsonProperty("part_code")
	private String partCode;
	@JsonProperty("name")
	private String partName;

	private Integer cavity;
//
//	@JsonProperty("")
//	private String resinCode;
//	@JsonProperty("")
//	private String resinGrade;
//	@JsonProperty("")
//	private String partVolume;
//	@JsonProperty("")
//	private String partWeight;
//	@JsonProperty("")
//	private String designRevision;
	public static MoldPartDTO convertToDTO(MoldPart moldPart){
		MoldPartDTO moldPartDTO = DataUtils.mapper.map(moldPart,MoldPartDTO.class);
//		if(moldPart.getPart()!=null){
//			if(moldPart.getPart().getCategory()!=null)
////			moldPartDTO.setCategoryId(moldPart.getPart().getCategory().getId());
//		}
		return moldPartDTO;
	}
}
