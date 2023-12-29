package saleson.api.role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saleson.api.role.payload.RoleParam;
import saleson.api.role.payload.RolePayload;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.GraphType;
import saleson.common.payload.ApiResponse;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Role;
import saleson.model.RoleGraph;
import saleson.model.data.RoleWithGraph;

@Deprecated
@RestController
@RequestMapping("/api/roles")
public class RoleController {
	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	RoleService roleService;

	@Autowired
	RoleGraphService roleGraphService;

	@Autowired
	VersioningService versioningService;

	/**
	 * 롤 목록
	 * @param param
	 * @param pageable
	 * @return
	 */
	@GetMapping
	public ResponseEntity<Page<RoleWithGraph>> list(RoleParam param, Pageable pageable) {
		Page<Role> pageContent = roleService.findAll(param.getPredicate(), pageable);

		List<Long> roleIds = pageContent.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<RoleGraph> roleGraphs = roleGraphService.findByRoleIdIn(roleIds);
		List<RoleWithGraph> roleWithGraphs = new ArrayList<>();
		pageContent.forEach(aRole -> {
			List<RoleGraph> aRoleGraphs = roleGraphs.stream().filter(x -> x.getRoleId().equals(aRole.getId())).collect(Collectors.toList());
			List<GraphType> graphTypes = aRoleGraphs.stream().map(RoleGraph::getGraphType).filter(GraphType::isEnabled).collect(Collectors.toList());
			roleWithGraphs.add(new RoleWithGraph(aRole, graphTypes));
		});
		Page<RoleWithGraph> result = new PageImpl<>(roleWithGraphs, pageable, pageContent.getTotalElements());

//		pageContent.getContent().stream().forEach(role -> {
//			System.out.println(role.getName());
//		});
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * 나의 Mold Group Role (관리자는 전부)
	 * @return
	 */
	@GetMapping("/my")
	public List<Role> myRoles() {
		return roleService.myRoles();
	}

	/**
	 * 롤 등록
	 * @param payload
	 * @return
	 */
	@PostMapping
	public ApiResponse create(@RequestBody RolePayload payload) {
		ApiResponse valid = roleService.validRole(payload, null);
		if (valid != null)
			return valid;
		Role role = payload.getModel();

		try {
			role = roleService.save(role);
			roleGraphService.save(role.getId(), payload.getGraphTypes());
			versioningService.writeHistory(role);
		} catch (Exception e) {
			//return new ApiResponse(false, "권한 등록 중 오류 발생!");
		}
		return new ApiResponse(true, "OK!!");
	}

	/**
	 * 롤 정보 조회
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ResponseEntity<RoleWithGraph> user(@PathVariable(value = "id", required = true) Long id) {
		RoleWithGraph roleWithGraph = new RoleWithGraph();
		try {
			Role role = roleService.findById(id);
			List<RoleGraph> roleGraphs = roleGraphService.findByRoleId(role.getId());
			List<GraphType> graphTypes = roleGraphs.stream().map(x -> x.getGraphType()).collect(Collectors.toList());
			roleWithGraph.setRole(role);
			roleWithGraph.setGraphTypes(graphTypes);
		} catch (Exception e) {

		}
		return new ResponseEntity<>(roleWithGraph, HttpStatus.OK);
	}

	@PutMapping("{id}")
	public ApiResponse editLocation(@PathVariable("id") Long id, @RequestBody RolePayload payload) {
		Role role = roleService.findById(id);

		if (role == null) {
			new ApiResponse(true, "ERROR");
		}
		ApiResponse valid = roleService.validRole(payload, id);
		if (valid != null)
			return valid;

		Role finalRole = roleService.save(payload.getModel(role));
		roleGraphService.save(role.getId(), payload.getGraphTypes());
		versioningService.writeHistory(finalRole);
		return new ApiResponse(true, "성공.");
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteRole(@PathVariable("id") Long id) {
		try {
			roleService.deleteById(id);
		} catch (RuntimeException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "Success.");
	}

	@GetMapping("/dashboard")
	public List<GraphType> dashboard() {
		return roleService.getDashboardByRole_Old();
	}

	@PostMapping("/update-old-data")
	public String update() {
		roleService.updateOldData();
		return "OK";
	}

	@PostMapping("/delete-in-batch")
	public ApiResponse deleteInBatch(@RequestBody BatchUpdateDTO dto) {
		return roleService.deleteInBatch(dto.getIds());
	}

	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody RolePayload payload) {
		Role role = roleService.findById(id);
		role.setEnabled(payload.getEnabled());
		roleService.save(role);
		Role finalObj = roleService.findById(id);
		versioningService.writeHistory(finalObj);
		return new ApiResponse(true, "OK!!");
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return roleService.changeStatusInBatch(dto);
	}

}
