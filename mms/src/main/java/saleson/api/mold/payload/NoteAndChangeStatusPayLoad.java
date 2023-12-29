package saleson.api.mold.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import saleson.api.systemNote.payload.SystemNotePayload;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoteAndChangeStatusPayLoad {
    private List<MoldPayload> moldPayloadList;
    private SystemNotePayload systemNotePayload;

}
