package saleson.api.topic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import saleson.model.Correspondence;

import java.util.List;

@Repository
public interface CorrespondenceRepository extends JpaRepository<Correspondence, Long>, QuerydslPredicateExecutor<Correspondence>{
        Long countByTopicIdAndIsQuestion(Long topicId,Boolean isQuestion);
}
