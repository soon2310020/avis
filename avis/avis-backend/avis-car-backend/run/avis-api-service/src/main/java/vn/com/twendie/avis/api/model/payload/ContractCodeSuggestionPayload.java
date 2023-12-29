package vn.com.twendie.avis.api.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.com.twendie.avis.api.constant.AvisApiConstant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractCodeSuggestionPayload {

    @JsonProperty("code_prefix")
    private String codePrefix;

    @JsonProperty("deleted")
    private Boolean deleted;

    @JsonProperty("page")
    private int page = AvisApiConstant.DEFAULT_STARTER_PAGE;

    @JsonProperty("size")
    private int size = AvisApiConstant.DEFAULT_PAGE_SIZE;

}
