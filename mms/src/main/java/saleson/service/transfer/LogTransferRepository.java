package saleson.service.transfer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import saleson.model.LogTransfer;

@Repository
public interface LogTransferRepository extends JpaRepository<LogTransfer, Long>, QuerydslPredicateExecutor<LogTransfer> {
	List<LogTransfer> findAllByCiOrderById(String ci);
}
