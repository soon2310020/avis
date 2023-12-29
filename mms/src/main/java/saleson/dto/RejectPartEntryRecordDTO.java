package saleson.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
public class RejectPartEntryRecordDTO {
    private Long machineId;
    private String machineCode;
    private String start;
    private String end;
    private Long numberOfShift;
    private String date;

    private Page<RejectPartEntryRecordItemDTO> rejectPartEntryRecordPage;
}
