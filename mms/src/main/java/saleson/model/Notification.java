package saleson.model;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.config.Const;
import saleson.common.enumeration.NotificationType;
import saleson.common.enumeration.PageType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DataUtils;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.Properties;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @DataLeakDetector(disabled = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATOR_ID")
    private User creator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_TARGET_ID")
    private User userTarget;

    @Column(name = "USER_TARGET_ID", insertable = false, updatable = false)
    private Long userTargetId;

    @Convert(converter = BooleanYnConverter.class)
    private boolean isRead;
    @Convert(converter = BooleanYnConverter.class)
    private boolean isReply;
    private Instant notificationCreatedAt;


    @Enumerated(EnumType.STRING)
    private PageType systemNoteFunction;
    private Long objectFunctionId;//for object function or topic
    private String topicId;// id code

    //for system note
    @JoinColumn(name = "SYSTEM_NOTE_ID")
    private Long systemNoteId;// or Correspondence id

    @Convert(converter = BooleanYnConverter.class)
    private boolean deletedParent;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    public Notification(User creator, User userTarget, Instant notificationCreatedAt, PageType function, Long objectFunctionId, Long systemNoteId) {
        this.creator = creator;
        this.userTarget = userTarget;
        this.notificationCreatedAt = notificationCreatedAt;
            this.systemNoteFunction = function;
        this.objectFunctionId = objectFunctionId;
        this.systemNoteId = systemNoteId;
    }

    public Notification(User creator, User userTarget, Instant notificationCreatedAt, PageType function, Long objectFunctionId, Long systemNoteId, String message) {
        this.creator = creator;
        this.userTarget = userTarget;
        this.notificationCreatedAt = notificationCreatedAt;
        this.systemNoteFunction = function;
        this.objectFunctionId = objectFunctionId;
        this.systemNoteId = systemNoteId;
        this.message = message;
    }

    public Notification(User creator, User userTarget, Instant notificationCreatedAt, PageType function,
                        Long objectFunctionId, Long systemNoteId, String message, NotificationType notificationType) {
        this.creator = creator;
        this.userTarget = userTarget;
        this.notificationCreatedAt = notificationCreatedAt;
        this.systemNoteFunction = function;
        this.objectFunctionId = objectFunctionId;
        this.systemNoteId = systemNoteId;
        this.message = message;
        this.notificationType = notificationType;
    }

    public String getFunctionTitle(){
        if(systemNoteFunction!=null){
            return systemNoteFunction.getTitle();
        }
        return null;
    }

    public String getMenuKey() {
        if (systemNoteFunction != null) {
            String menuKey = Const.menuSystemNoteMap.entrySet().stream().filter(map -> systemNoteFunction.equals(map.getValue())).map(m -> m.getKey()).findFirst().orElse(null);
            return menuKey;
        }
        return null;
    }
    public Boolean getIsAlertCenter(){
        return Const.ALERT_KEYS.contains(getMenuKey());
    }
}
