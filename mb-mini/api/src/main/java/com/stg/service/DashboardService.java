package com.stg.service;

import com.stg.service.dto.dashboard.DashboardCustomerListDto;
import com.stg.service.dto.dashboard.DashboardOverviewDto;
import com.stg.service.dto.dashboard.DashboardPackageDto;
import com.stg.service.dto.dashboard.ReleaseQuantityDto;

import java.util.List;

public interface DashboardService {

//    PaginationResponse<IllustrationTableDto> illustrationBoards(Long user, int page, int size, String query, String packageName);

    DashboardOverviewDto dashboardOverview(Long userId);
    ReleaseQuantityDto releaseQuantity(Long userId);
    List<DashboardPackageDto> insurancePackages(Long userId, String category);
    List<DashboardCustomerListDto> customerHealthy(Long userId);

}
