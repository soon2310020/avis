package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Correspondence extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "TOPIC_ID")
    private Long topicId;

/*
    @JsonIgnore
    @ManyToOne
    private Topic topic;
*/

    @Lob
    private String message;

    @Column(length = 1, nullable = false)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean isQuestion;

//    @Transient
//    private Boolean isAnswer=false;
    @Transient
    private Iterable<FileStorage> files;
}
