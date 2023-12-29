package saleson.api.topic;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.common.enumeration.CustomerSupportStatus;
import saleson.common.util.StringUtils;
import saleson.model.QTopic;
import saleson.model.Topic;

import java.util.List;

public class TopicRepositoryImpl extends QuerydslRepositorySupport implements TopicRepositoryCustom {
    public TopicRepositoryImpl() {
        super(Topic.class);
    }

}
