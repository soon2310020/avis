package saleson.model.clone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import saleson.common.enumeration.RevisionObjectType;
import saleson.model.support.DateAudit;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RevisionHistory extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;

    private Long editedBy;

    private Long revisionId;

//    private Long revisionHistoryId;

    @Enumerated(EnumType.STRING)
    private RevisionObjectType revisionObjectType;
}
