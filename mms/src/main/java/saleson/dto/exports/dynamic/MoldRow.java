package saleson.dto.exports.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldRow {
    private Long moldId;
    private String moldCode;
    private String approvedCT;
    private String uptimeTarget;
    private List<MoldItem> moldItemList;
    private boolean isNewCounter=true;
    private String counterCode;
    private boolean isAvaliable=false;

}
