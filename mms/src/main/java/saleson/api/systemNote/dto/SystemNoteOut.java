package saleson.api.systemNote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import saleson.model.SystemNote;

import java.util.List;

@Data
@AllArgsConstructor
public class SystemNoteOut {
    private List<SystemNote> dataList;
    private long total;
    private Long totalUnread;
}
