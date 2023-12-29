package saleson.api.topic.payload;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.CustomerSupportStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.TopicType;
import saleson.common.util.StringUtils;
import saleson.model.QTopic;
import saleson.model.Topic;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopicPayload extends SearchParam {
	private CustomerSupportStatus topicStatus;
	private String topicId;
	private String subject;
	private String message;
//    private String creatorId;
	private MultipartFile[] files;
	private TopicType topicType;

	private Long recipientId;
	private PageType systemNoteFunction;
	private ObjectType objectType;
	private Long objectId;
	private String objectCode;

	public Topic getModel() {
		Topic topic = new Topic();
		bindData(topic);
		return topic;
	}

	public Topic getModel(Topic topic) {
		bindData(topic);
		return topic;
	}

	public void bindData(Topic topic) {
		topic.setStatus(topicStatus);
		topic.setTopicId(topicId);
		topic.setSubject(subject);
		topic.setMessage(message);
		topic.setSystemNoteFunction(systemNoteFunction);
		topic.setObjectType(objectType);
		topic.setObjectId(objectId);
		topic.setObjectCode(objectCode);
	}

	public Predicate getPredicate() {
		QTopic topic = QTopic.topic;

		BooleanBuilder builder = new BooleanBuilder();
		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(topic.subject.like('%' + getQuery() + '%').or(topic.creator.email.like('%' + getQuery() + '%')).or(topic.topicId.like('%' + getQuery() + '%'))
					.or(topic.creator.company.name.like('%' + getQuery() + '%')));
		}
		if (getId() != null) {
			builder.and(topic.id.eq(getId()));
		}
		if (topicStatus != null) {
			builder.and(topic.status.eq(topicStatus));
		}
		if (topicType != null) {
			if (topicType == TopicType.HELP_CENTER) {
				builder.and(topic.topicType.eq(topicType).or(topic.topicType.isNull()));
			} else {
				builder.and(topic.topicType.eq(topicType));
			}
		}
		return builder;
	}
}
