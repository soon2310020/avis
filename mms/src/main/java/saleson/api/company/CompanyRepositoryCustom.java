package saleson.api.company;

import java.util.*;

import org.springframework.data.domain.*;

import com.querydsl.core.types.*;

import saleson.api.dataCompletionRate.payload.*;
import saleson.api.filter.payload.DashboardGeneralFilterPayload;
import saleson.model.*;
import saleson.model.data.*;
import saleson.model.data.completionRate.*;

public interface CompanyRepositoryCustom {
	List<MiniGeneralData> findCompanyByMoldIdIn(List<Long> moldIds);

	Page<Company> findAllOrderByName(Predicate predicate, Pageable pageable);

	Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);

	CompletionRateData getCompanyCompletionRate(Long companyId);

	Page<Company> findAllWithTotalMoldCount(Predicate predicate, Pageable pageable);

	List<Company> getListCompany(List<String> companyCodeList,Pageable pageable,String searchText);

	Long countByProduct(Long productId, Long partId, Long moldId);

	Long countByBrand(Long brandId, Long partId, Long moldId);

	Page<Company> findByProject(Long projectId, Long partId, Long moldId, Pageable pageable);

	Page<Company> findByBrand(Long brandId, Long partId, Long moldId, Pageable pageable);

	List<Company> findAllByGeneralFilter(boolean isToolmaker, DashboardGeneralFilter filter, boolean isAll);

	List<Long> findAllIdByPredicate(Predicate predicate);

	Long countByProductIdIn(List<Long> productIdList);

	Long countAllIncompleteData();

    List<Company> findAllIncompleteData();

}
