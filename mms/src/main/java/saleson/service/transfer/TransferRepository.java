package saleson.service.transfer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import saleson.model.Transfer;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>, QuerydslPredicateExecutor<Transfer>, TransferRepositoryCustom {

	Long countByAtAndCiAndSn(String at, String ci, Integer sn);

	Long countByAtAndCiAndSnAndTff(String at, String ci, Integer sn, String tff);

	Optional<Transfer> findFirstByCiOrderBySnDesc(String ci);

	Optional<Transfer> findFirstByCiAndSnOrderByTffDesc(String ci, Long sn);

	List<Transfer> findByCiAndRtGreaterThanOrderByRt(String ci, String rt);
}
