package saleson.api.dataRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DataRequest;

import java.util.Optional;

public interface DataRequestRepository extends JpaRepository<DataRequest, Long>, QuerydslPredicateExecutor<DataRequest>, DataRequestRepositoryCustom{

}
