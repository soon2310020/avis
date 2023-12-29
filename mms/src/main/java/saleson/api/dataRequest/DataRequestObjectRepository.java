package saleson.api.dataRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.ObjectType;
import saleson.model.DataRequestObject;

import java.util.List;

public interface DataRequestObjectRepository extends JpaRepository<DataRequestObject, Long>, QuerydslPredicateExecutor<DataRequestObject>{
    void deleteAllByDataRequestId(Long requestDataId);

    List<DataRequestObject> findAllByDataRequestId(Long requestDataId);

    boolean existsByDataRequestIdAndObjectType(Long requestDataId, ObjectType objectType);

    long countAllByDataRequestIdAndObjectType(Long requestDataId, ObjectType objectType);
}
