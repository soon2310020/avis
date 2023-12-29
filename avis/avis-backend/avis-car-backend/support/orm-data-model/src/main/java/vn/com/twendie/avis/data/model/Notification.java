package vn.com.twendie.avis.data.model;

import lombok.*;
import vn.com.twendie.avis.data.enumtype.NotificationSettingTypeEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "notification_content_id", referencedColumnName = "id")
    private NotificationContent notificationContent;

    @Column(name = "status")
    private String status;

    @Column(name = "type")
    private String type;

    @Column(name = "spec_id")
    private Long specId;

    @Column(name = "params")
    private String params;

    @Column(name = "type_setting")
    @Enumerated(EnumType.STRING)
    private NotificationSettingTypeEnum typeSetting;

    @Column(name = "from_date")
    private Timestamp fromDate;

    @Column(name = "to_date")
    private Timestamp toDate;
}
