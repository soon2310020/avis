package saleson.api.dataCompletionRate;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DataCompletionOrderRepository extends JpaRepository<DataCompletionOrder, Long>, QuerydslPredicateExecutor<DataCompletionOrder>, DataCompletionOrderRepositoryCustom {
    @Query(value = "SELECT max(orderIndex) FROM DataCompletionOrder")
    Optional<BigDecimal> findMaxIndex();

    List<DataCompletionOrder> findByAssignedUsersContains(User user);

//    Optional<List<DataCompletionOrder>> findByCreatedBy(Long userId);

    Page<DataCompletionOrder> findByCreatedBy(Long userId, Pageable pageable);

    Optional<DataCompletionOrder> findByCompaniesContainsAndAssignedUsersContainsAndCompletedCompanyIsFalseAndCompletedIsFalse(Company company, User user);
    Optional<DataCompletionOrder> findByLocationsContainsAndAssignedUsersContainsAndCompletedLocationIsFalseAndCompletedIsFalse(Location location, User user);
    Optional<DataCompletionOrder> findByCategoriesContainsAndAssignedUsersContainsAndCompletedCategoryIsFalseAndCompletedIsFalse(Category category, User user);
    Optional<DataCompletionOrder> findByPartsContainsAndAssignedUsersContainsAndCompletedPartIsFalseAndCompletedIsFalse(Part part, User user);
    Optional<DataCompletionOrder> findByMoldsContainsAndAssignedUsersContainsAndCompletedMoldIsFalseAndCompletedIsFalse(Mold mold, User user);
    Optional<DataCompletionOrder> findByMachinesContainsAndAssignedUsersContainsAndCompletedMachineIsFalseAndCompletedIsFalse(Machine machine, User user);

    Optional<List<DataCompletionOrder>> findByDueDayAndCompletedIsFalseAndLastRemindedIsFalse(String dueDay);

    Optional<List<DataCompletionOrder>> findByCompaniesContainsAndCompletedCompanyIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(Company company);
    Optional<List<DataCompletionOrder>> findByLocationsContainsAndCompletedLocationIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(Location location);
    Optional<List<DataCompletionOrder>> findByCategoriesContainsAndCompletedCategoryIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(Category category);
    Optional<List<DataCompletionOrder>> findByPartsContainsAndCompletedPartIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(Part part);
    Optional<List<DataCompletionOrder>> findByMoldsContainsAndCompletedMoldIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(Mold mold);
    Optional<List<DataCompletionOrder>> findByMachinesContainsAndCompletedMachineIsFalseAndCompletedIsFalseOrderByCreatedAtDesc(Machine machine);
}
