package vn.com.twendie.avis.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import vn.com.twendie.avis.data.enumtype.NotificationSettingTypeEnum;

import javax.persistence.*;

//@Audited
@Data
@Entity
@Table(name = "notification_setting")
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSetting extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "day")
    private boolean day;

    @Column(name = "week")
    private boolean week;

    @Column(name = "month")
    private boolean month;

}
