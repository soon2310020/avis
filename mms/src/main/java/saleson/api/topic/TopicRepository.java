package saleson.api.topic;

import org.opensaml.ws.wsaddressing.To;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import saleson.model.Topic;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long>, QuerydslPredicateExecutor<Topic>, TopicRepositoryCustom {
    Optional<Topic> findFirstByTopicIdStartingWithOrderByTopicIdDesc(String perTopicId);

}
