package vn.com.twendie.avis.data.revision;

import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "revinfo")
@RevisionEntity(UserRevisionListener.class)
public class UserRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private long timestamp;

    @Column(name = "action_type")
    private Long actionType;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private Timestamp createdAt;

}
