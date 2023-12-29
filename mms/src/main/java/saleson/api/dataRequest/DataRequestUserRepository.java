package saleson.api.dataRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.DataRequestUser;

import java.util.List;

public interface DataRequestUserRepository extends JpaRepository<DataRequestUser, Long>, QuerydslPredicateExecutor<DataRequestUser>{
    void deleteAllByDataRequestId(Long requestDataId);

    List<DataRequestUser> getAllByDataRequestId(Long requestDataId);
}
