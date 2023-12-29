package saleson.api.topic.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import saleson.model.Correspondence;
import saleson.model.QCorrespondence;
import saleson.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorrespondencePayload {
    private Long id;
    private String message;
    private Long userId;
    private Long topicId;
    private MultipartFile[] files;
    private Boolean isQuestion;

    public Correspondence getModel(){
        Correspondence correspondence = new Correspondence();
        bindData(correspondence);
        return correspondence;
    }
    public void bindData(Correspondence correspondence){
        correspondence.setId(id);
        correspondence.setMessage(message);
        correspondence.setTopicId(topicId);
        if(userId!=null){
            User user = new User();
            user.setId(userId);
            correspondence.setUser(user);
        }
    }

    public Predicate getPredicate(){
        QCorrespondence correspondence = QCorrespondence.correspondence;

        BooleanBuilder builder = new BooleanBuilder();

        if(topicId != null){
            builder.and(correspondence.topicId.eq(topicId));
        }
        if(isQuestion!=null){
            builder.and(correspondence.isQuestion.eq(isQuestion));
        }
        return builder;
    }
}
