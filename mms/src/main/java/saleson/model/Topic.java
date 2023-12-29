package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.CustomerSupportStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.TopicType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Topic extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String topicId;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerSupportStatus status;

    private String subject;

    @Lob
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATOR_ID")
    private User creator;

//    @JsonIgnore
//    @Column(name = "CREATOR_ID", insertable = false, updatable = false)
//    private Long creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LAST_REPLY_BY_ID")
    private User lastReplyBy;

//    @JsonIgnore
//    @Column(name = "LAST_REPLY_BY_ID", insertable = false, updatable = false)
//    private Long lastReplyId;

    private Instant lastReplyAt;

    private Integer numberOfReply;

    @Column(length = 1, name = "LAST_REPLY_IS_QUESTION")
    @Convert(converter = BooleanYnConverter.class)
    private Boolean lastReplyIsQuestion = false;

    //    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "TOPIC_ID")
//    @JsonIgnore
//    private List<Correspondence> correspondences = new ArrayList<>();
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TopicType topicType = TopicType.HELP_CENTER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECIPIENT_ID")
    private User recipient;

    @Enumerated(EnumType.STRING)
    private PageType systemNoteFunction;

    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    private Long objectId;
    private String objectCode;

}
