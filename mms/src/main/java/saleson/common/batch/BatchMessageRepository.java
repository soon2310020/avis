package saleson.common.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.BatchStatus;
import saleson.common.enumeration.MessageType;
import saleson.model.BatchMessage;

import java.util.List;

public interface BatchMessageRepository extends JpaRepository<BatchMessage, Long>, QuerydslPredicateExecutor<BatchMessage> {
	List<BatchMessage> findAllByBatchStatusAndMessageType(BatchStatus batchStatus, MessageType messageType);
}
