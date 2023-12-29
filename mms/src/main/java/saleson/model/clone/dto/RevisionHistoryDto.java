package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevisionHistoryDto {

    private Long id;

    private Long editedBy;

    private String editedByName;

    private Long revisionId;

    private Long originId;

    private String createdDateTime;

    private String revisionObjectType;

    private boolean currentVersion;

    private Long createdAtValue;

}
