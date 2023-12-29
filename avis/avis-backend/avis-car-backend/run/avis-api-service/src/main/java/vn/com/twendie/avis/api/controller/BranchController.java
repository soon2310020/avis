package vn.com.twendie.avis.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.BranchService;
import vn.com.twendie.avis.security.annotation.RequirePermission;

@RestController
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin", "Sale"})
    public ApiResponse<?> getBranches() {
        return ApiResponse.success(branchService.getBranchInfos());
    }
}
