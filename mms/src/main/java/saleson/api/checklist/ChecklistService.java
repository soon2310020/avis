package saleson.api.checklist;

import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.MapExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.checklist.payload.ChecklistPayload;
import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.user.UserRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.payload.ApiResponse;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Company;
import saleson.model.Machine;
import saleson.model.TabTableData;
import saleson.model.User;
import saleson.model.checklist.*;
import saleson.service.version.VersionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChecklistService {
    @Autowired
    ChecklistRepository checklistRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    ChecklistUserRepository checklistUserRepository;
    @Autowired
    ChecklistCompanyRepository checklistCompanyRepository;
//    @Autowired
//    ChecklistItemRepository checklistItemRepository;
    @Autowired
    VersioningService versioningService;
    @Autowired
    CompanyService companyService;

    public Page<Checklist> findAll(Predicate predicate, Pageable pageable) {
        String[] properties = { "" };
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
        });
        Page<Checklist> page;
        if (SpecialSortProperty.checklistSpecialFields.contains(properties[0])) {
            page = checklistRepository.getAllOrderBySpecialField(predicate, pageable);
        } else {
            page = checklistRepository.findAll(predicate, pageable);
        }

        loadCreatorInfo(page.getContent());
        return page;
    }

    private void loadCreatorInfo(List<Checklist> checklists) {
        checklists.forEach(checklist -> {
            if (checklist.getCreatedBy() != null) {
                User user = userRepository.getOne(checklist.getCreatedBy());
                checklist.setCreator(user);
            }
        });
    }

    @Transactional
    public Checklist save(Checklist checklist, ChecklistPayload checklistPayload){
        if (checklistPayload == null) {
            return checklistRepository.save(checklist);
        }

        Checklist checklistSaved = checklistRepository.save(checklist);

        //save checklist user
        if (CollectionUtils.isNotEmpty(checklistPayload.getUserIds())) {
            List<ChecklistUser> checklistUsers = new ArrayList<>();
            checklistPayload.getUserIds().forEach(id -> {
                ChecklistUser checklistUser = ChecklistUser.builder()
                        .checklistId(checklistSaved.getId())
                        .checklist(checklistSaved)
                        .userId(id)
                        .user(userRepository.getOne(id))
                        .build();

                checklistUsers.add(checklistUser);
            });
            checklistUserRepository.deleteAllByChecklistId(checklistSaved.getId());
            checklistUserRepository.saveAll(checklistUsers);
        } else {
            checklistUserRepository.deleteAllByChecklistId(checklistSaved.getId());
        }

        //save checklist Company
        if (CollectionUtils.isNotEmpty(checklistPayload.getCompanyIds())) {
            List<ChecklistCompany> checklistCompanies = new ArrayList<>();
            checklistPayload.getCompanyIds().forEach(id -> {
                ChecklistCompany checklistCompany = ChecklistCompany.builder()
                        .checklistId(checklistSaved.getId())
                        .checklist(checklistSaved)
                        .companyId(id)
                        .company(companyRepository.getOne(id))
                        .build();

                checklistCompanies.add(checklistCompany);
            });
            checklistCompanyRepository.deleteAllByChecklistId(checklistSaved.getId());
            checklistCompanyRepository.saveAll(checklistCompanies);
        } else {
            checklistCompanyRepository.deleteAllByChecklistId(checklistSaved.getId());
        }

        return checklist;
    }
    public Checklist newChecklist(ChecklistPayload checklistPayload){
        Checklist checklist = checklistPayload.getModel();
        save(checklist,checklistPayload);
        versioningService.writeHistory(checklist);

        return checklist;
    }
    public void deleteChecklist(Long id){
        Checklist checklist = checklistRepository.findById(id).orElse(null);
        if(checklist!=null){
/*            if(checklist.getChecklistItems()!=null)
                checklistItemRepository.deleteAll(checklist.getChecklistItems());*/
            checklistRepository.delete(checklist);
        }
    }


    public ApiResponse changeStatusInBatch(BatchUpdateDTO dto){
        try
        {
            List<Checklist> checklists = checklistRepository.findAllById(dto.getIds());
            checklists.forEach(checklist -> {
                checklist.setEnabled(dto.isEnabled());
                checklistRepository.save(checklist);
                versioningService.writeHistory(checklist);
            });
            return ApiResponse.success(CommonMessage.OK, checklists);
        }
        catch (Exception e){
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse listCheckList(CheckListObjectType objectType, ChecklistType checklistType) {
        if (checklistType == null) {
            return ApiResponse.success(CommonMessage.OK, checklistRepository.findAllByObjectTypeAndEnabledTrue(objectType));
        }
        return ApiResponse.success(CommonMessage.OK, checklistRepository.findAllByObjectTypeAndChecklistTypeAndEnabledTrue(objectType, checklistType));
    }

    public Map<ChecklistType, Long> countCheckListByType() {
        QChecklist qChecklist = QChecklist.checklist;
        BooleanBuilder builder = new BooleanBuilder();
        JPQLQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)
                .from(qChecklist)
                .where(qChecklist.enabled.isTrue().and(qChecklist.checklistType.isNotNull()))
                .groupBy(qChecklist.checklistType)
                .select(qChecklist.checklistType, qChecklist.count());

        List<Tuple> results = query.fetch();

        Map<ChecklistType, Long> resultMap = new HashMap<>();
        for (Tuple tuple : results) {
            ChecklistType checklistType = tuple.get(qChecklist.checklistType);
            Long count = tuple.get(qChecklist.count());
            resultMap.put(checklistType, count);
        }
        return resultMap;
    }
}
