package saleson.api.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import saleson.common.enumeration.GraphType;
import saleson.model.RoleGraph;

@Deprecated
@Service
public class RoleGraphService {
    @Autowired
    RoleGraphRepository roleGraphRepository;

    public List<RoleGraph> findByRoleId(Long roleId){
        return roleGraphRepository.findByRoleId(roleId);
    }

    public List<RoleGraph> findByRoleIdIn(List<Long> roleIds){
        return roleGraphRepository.findByRoleIdIn(roleIds);
    }

    public void save(List<RoleGraph> roleGraphs){
        roleGraphRepository.saveAll(roleGraphs);
    }

    public void save(Long roleId, List<GraphType> graphTypes){
        if(roleId == null || graphTypes == null || graphTypes.isEmpty()) return;
        List<RoleGraph> roleGraphs = findByRoleId(roleId);
        List<GraphType> currentGrapTypes = roleGraphs.stream().map(x -> x.getGraphType()).collect(Collectors.toList());
        List<RoleGraph> unusedRoleGraphs = roleGraphs.stream().filter(x -> !graphTypes.contains(x.getGraphType())).collect(Collectors.toList());
        roleGraphRepository.deleteAll(unusedRoleGraphs);

        List<RoleGraph> newRoleGraphs = new ArrayList<>();
        graphTypes.forEach(graphType -> {
            if(!currentGrapTypes.contains(graphType)){
                newRoleGraphs.add(new RoleGraph(roleId, graphType));
            }
        });
        roleGraphRepository.saveAll(newRoleGraphs);
    }

    public void deleteByRoleId(Long roleId){
        List<RoleGraph> roleGraphs = findByRoleId(roleId);
        roleGraphRepository.deleteAll(roleGraphs);
    }

    public List<GraphType> findGraphTypeByRoleIdIn(List<Long> roleIds){
        List<GraphType> result = roleGraphRepository.findGraphTypeByRoleIdIn(roleIds);

		Comparator<GraphType> compareByName = Comparator.comparing(GraphType::getPosition);
        Collections.sort(result, compareByName);
        return result;
    }

    public List<RoleGraph> findByIdIn(List<Long> roleGraphIds){
        return roleGraphRepository.findByIdIn(roleGraphIds);
    }

    public RoleGraph findById(Long id){
        return roleGraphRepository.findById(id).orElse(null);
    }
}
