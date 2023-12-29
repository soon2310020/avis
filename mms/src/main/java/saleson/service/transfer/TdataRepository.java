package saleson.service.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import saleson.model.Tdata;

@Repository
public interface TdataRepository extends JpaRepository<Tdata, Long>, QuerydslPredicateExecutor<Tdata> {

}
