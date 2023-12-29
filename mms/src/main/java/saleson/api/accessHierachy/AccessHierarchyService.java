package saleson.api.accessHierachy;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.accessHierachy.payload.AccessHierarchyPayload;
import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.mold.MoldRepository;
import saleson.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessHierarchyService {
    @Autowired
    AccessHierarchyRepository accessHierarchyRepository;

    @Autowired
    AccessMoldRepository accessMoldRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    AccessCompanyRelationRepository accessCompanyRelationRepository;
    @Autowired
    MoldRepository moldRepository;
    @Autowired
    CompanyService companyService;


    public Page<AccessHierarchy> findAll(Predicate predicate, Pageable pageable) {
        return accessHierarchyRepository.findAll(predicate, pageable);
    }

    public Page<AccessMold> findAllAccessMold(Predicate predicate, Pageable pageable) {
        return accessMoldRepository.findAll(predicate, pageable);
    }

    @Transactional
    public AccessHierarchy save(AccessHierarchy accessHierarchy, AccessHierarchyPayload payload) {
        if (payload == null) {
            accessHierarchyRepository.save(accessHierarchy);
            return accessHierarchy;
        }
        if (payload.getCompanyId() != null) {
            Company company = companyRepository.findById(payload.getCompanyId()).orElse(null);
            accessHierarchy.setCompany(company);
        }
//        if (accessHierarchy.getParentIdList().isEmpty() && !accessHierarchy.isMultipleParent()) {
//            accessHierarchy.setLevel(0L);
//        }
        //update level
        List<Long> parendIdList = new ArrayList<>();
        if (accessHierarchy.getParentIdListTemp() != null) parendIdList = accessHierarchy.getParentIdListTemp();
        List<AccessHierarchy> accessHierarchyParentList = accessHierarchyRepository.findAllByCompanyIdIn(parendIdList);
        Long level = 0l;
        for (int i = 0; i < accessHierarchyParentList.size(); i++) {
            AccessHierarchy acc = accessHierarchyParentList.get(i);
            if (acc.getLevel() != null && acc.getLevel() >= level) {
                level = acc.getLevel() + 1;
            }
        }
        accessHierarchy.setLevel(level);


        accessHierarchyRepository.save(accessHierarchy);

        if (parendIdList != null) {
            List<AccessCompanyRelation> accessCompanyRelationList = accessHierarchy.generationAccessCompanyParentRelation(parendIdList);
            accessCompanyRelationRepository.saveAll(accessCompanyRelationList);
        }

        return accessHierarchy;
    }

    @Transactional
    public void deleteAccessCompanyRelation(Long id) {

        List<AccessHierarchy> accessHierarchyListDel = new ArrayList<>();
        List<AccessHierarchy> accessHierarchyListUpdate = new ArrayList<>();
        List<AccessCompanyRelation> accessCompanyRelationList = new ArrayList<>();

        AccessCompanyRelation acr = accessCompanyRelationRepository.findById(id).orElse(null);
        if (acr == null) return;
        AccessHierarchy ah = acr.getAccessHierarchy();
        if (ah.getAccessCompanyParentRelations().isEmpty() || ah.getAccessCompanyParentRelations().size() == 1) {
            accessHierarchyListDel.add(acr.getAccessHierarchy());
            //update children
            loadDeleteChild(ah, accessHierarchyListDel
                    , accessHierarchyListUpdate, accessCompanyRelationList, true);
        } else {
            if (ah.getAccessCompanyParentRelations().size() == 2) {
                ah.setMultipleParent(false);
            }
            //update level child when remove parent
            Long levelParentOfChild = ah.getAccessCompanyParentRelations().stream()
                    .filter(a -> !a.getId().equals(acr.getId()) && a.getAccessHierarchy() != null)
                    .map(l -> l.getAccessHierarchy().getLevel()).mapToLong(v -> v).max().orElse(-1L);
            if (levelParentOfChild != null && levelParentOfChild >= 0)
                ah.setLevel(levelParentOfChild + 1);

            accessHierarchyListUpdate.add(ah);
        }
        accessCompanyRelationList.add(acr);


        List<AccessMold> accessMolds = accessMoldRepository.findAllByAccessCompanyRelationIdIn(accessCompanyRelationList
                .stream().map(a -> a.getId()).collect(Collectors.toList()));

        accessMoldRepository.deleteAll(accessMolds);
        accessCompanyRelationRepository.deleteAll(accessCompanyRelationList);
        accessHierarchyRepository.deleteAll(accessHierarchyListDel);
        accessHierarchyRepository.saveAll(accessHierarchyListUpdate);

    }

    @Transactional
    public void deleteAccessHierarchy(Long companyId) {
        List<AccessHierarchy> accessHierarchyListDel = new ArrayList<>();
        List<AccessHierarchy> accessHierarchyListUpdate = new ArrayList<>();
        List<AccessCompanyRelation> accessCompanyRelationList = new ArrayList<>();
        AccessHierarchy accessHierarchy = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
        accessHierarchyListDel.add(accessHierarchy);
        accessCompanyRelationList.addAll(accessHierarchy.getAccessCompanyParentRelations());
        //update children
        loadDeleteChild(accessHierarchy, accessHierarchyListDel
                , accessHierarchyListUpdate, accessCompanyRelationList, true);

        List<AccessMold> accessMolds = accessMoldRepository.findAllByAccessCompanyRelationIdIn(accessCompanyRelationList
                .stream().map(a -> a.getId()).collect(Collectors.toList()));

        accessHierarchyRepository.saveAll(accessHierarchyListUpdate);
        accessMoldRepository.deleteAll(accessMolds);
        accessCompanyRelationRepository.deleteAll(accessCompanyRelationList);
        accessHierarchyRepository.deleteAll(accessHierarchyListDel);
    }

    private void loadDeleteChild(final AccessHierarchy accessHierarchy
            , final List<AccessHierarchy> accessHierarchyListDel
            , final List<AccessHierarchy> accessHierarchyListUpdate
            , final List<AccessCompanyRelation> accessCompanyRelationListOut, boolean loadForDel) {
        List<AccessCompanyRelation> accessCompanyChildRelationList = accessHierarchy.getAccessCompanyChildRelations();
        if (accessCompanyChildRelationList != null) {
            accessCompanyRelationListOut.addAll(accessCompanyChildRelationList);
            for (AccessCompanyRelation acrChild : accessCompanyChildRelationList) {
                AccessHierarchy accHierChild = acrChild.getAccessHierarchy();
                List<AccessCompanyRelation> otherAccessCompanyParentRelationListOfHierChild = accHierChild.getAccessCompanyParentRelations();
                otherAccessCompanyParentRelationListOfHierChild = otherAccessCompanyParentRelationListOfHierChild.stream().filter(a ->
                        !accessCompanyRelationListOut.stream().anyMatch(ac -> ac.getId().equals(a.getId()))
                ).collect(Collectors.toList());

                if (accHierChild.isMultipleParent() && otherAccessCompanyParentRelationListOfHierChild.size() >= 1) {
                    accessHierarchyListUpdate.add(accHierChild);
                    if (loadForDel) {
                        if (otherAccessCompanyParentRelationListOfHierChild.size() == 1) {
                            accHierChild.setMultipleParent(false);
                        }
                        //update level child when remove parent
                        Long levelParentOfChild = otherAccessCompanyParentRelationListOfHierChild.stream()
                                .filter(a -> !a.getId().equals(acrChild.getId()) && a.getAccessHierarchy() != null)
                                .map(l -> l.getAccessHierarchy().getLevel()).mapToLong(v -> v).max().orElse(-1L);
                        if (levelParentOfChild != null && levelParentOfChild >= 0)
                            accHierChild.setLevel(levelParentOfChild + 1);
                    }

                } else
                    accessHierarchyListDel.add(accHierChild);

                if (accessHierarchy.getLevel() >= accHierChild.getLevel()) {
                    throw new RuntimeException("ERROR data Level parent " + accessHierarchy.getCompany().getName()
                            + " less or equal children " + accHierChild.getCompany().getName());
                }
                loadDeleteChild(accHierChild, accessHierarchyListDel
                        , accessHierarchyListUpdate, accessCompanyRelationListOut, loadForDel);
            }
        }
    }

    @Transactional
    public List<AccessMold> assignedTooling(AccessHierarchyPayload payload,Long accessHierarchyId) {
        List<AccessMold> accessMoldList = new ArrayList<>();
        List<AccessMold> accessMoldListDel = new ArrayList<>();
        if (payload.getAccessMoldPayloadList() != null) {
            AccessHierarchy accessHierarchy=accessHierarchyRepository.findById(accessHierarchyId).orElse(null);
            assert accessHierarchy!=null;

            payload.getAccessMoldPayloadList().stream().forEach(accessMoldPayload -> {
                if (accessMoldPayload.getMoldIdList() != null && accessMoldPayload.getCompanyId() != null) {
                    AccessCompanyRelation acr =
                            accessCompanyRelationRepository.findFirstByCompanyIdAndCompanyParentId(accessHierarchy.getCompanyId(), accessMoldPayload.getCompanyId()).orElse(null);
                    if (acr == null) return;
                    List<AccessMold> accessMoldListOld = accessMoldRepository.findAllByCompanyIdAndAccessCompanyRelationId(accessMoldPayload.getCompanyId(), acr.getId());
                    accessMoldListDel.addAll(accessMoldListOld);
                    //create new
                    accessMoldPayload.getMoldIdList().stream().forEach(moldId -> {
                        AccessMold accessMold = new AccessMold(accessMoldPayload.getCompanyId(), moldId, acr.getId());
                        accessMoldList.add(accessMold);
                    });
                }
            });
        }
        accessMoldRepository.deleteAll(accessMoldListDel);
        accessMoldRepository.saveAll(accessMoldList);
        return accessMoldList;
    }

    public Set<Long> getFullCompanyParentId(Long companyId) {
        Set<Long> res = new HashSet<>();
        AccessHierarchy accessHierarchy = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
        if (accessHierarchy == null) return res;
        loadParentCompany(accessHierarchy, res);
        return res;
    }

    private void loadParentCompany(AccessHierarchy accessHierarchy, Set<Long> res) {
        if (accessHierarchy.getParentIdList().isEmpty()) return;
        res.addAll(accessHierarchy.getParentIdList());
        accessHierarchy.getAccessCompanyParentRelations().stream().forEach(acp -> {
            //valid
            if (accessHierarchy.getLevel() <= acp.getAccessHierarchyParent().getLevel()) {
                throw new RuntimeException("ERROR data Level children " + accessHierarchy.getCompany().getName() + " lager or equal parent " + acp.getAccessHierarchyParent().getCompany().getName());
            }

            loadParentCompany(acp.getAccessHierarchyParent(), res);
        });

    }

    public Page<Company> getAllCompaniesAbilityToBeChild(CompanyPayload payload, Long companyId, Pageable pageable) {
        if (companyId != null) {
            List<Long> companyIdList = getFullCompanyParentId(companyId).stream().collect(Collectors.toList());
            companyIdList.add(companyId);
            //remove company same and low level because FontEnd not support
            List<Long> companyLowLevelList = getCompanyLowLevelList(companyId);
            companyIdList.addAll(companyLowLevelList);
            //remove company high two levels because FontEnd not support
            List<Long> companyHighTwoLevelList = getCompanyHighLevelList(companyId, 2);
            companyIdList.addAll(companyHighTwoLevelList);

            payload.setCompanyIdNotInList(companyIdList);
        }
        Page<Company> pageContent = companyService.findAll(payload.getPredicate(), pageable);
        return pageContent;
    }

    public List<Long> getCompanyLowLevelList(Long companyId) {
        List<Long> companyList = new ArrayList<>();
        AccessHierarchy accessHierarchyExist = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
        if (accessHierarchyExist != null) {
            List<AccessHierarchy> accessHierarchyList = accessHierarchyRepository.findAllByLevelLessThanEqual(accessHierarchyExist.getLevel());
            return accessHierarchyList.stream().map(accessHierarchy -> accessHierarchy.getCompanyId()).collect(Collectors.toList());
        }
        return companyList;
    }

    public List<Long> getCompanyHighLevelList(Long companyId, int diffLevel) {
        List<Long> companyList = new ArrayList<>();
        AccessHierarchy accessHierarchyExist = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
        if (accessHierarchyExist != null) {
            List<AccessHierarchy> accessHierarchyList = accessHierarchyRepository.findAllByLevelGreaterThanEqual(accessHierarchyExist.getLevel() + diffLevel);
            return accessHierarchyList.stream().map(accessHierarchy -> accessHierarchy.getCompanyId()).collect(Collectors.toList());
        }
        return companyList;
    }

    @Deprecated
    /**
     *
     * @param companyId
     * @param otherMoldIdListOut null if without get moldId
     * @return
     */
    public Set<Long> getFullCompanyChildrenId(Long companyId, final List<Long> otherMoldIdListOut) {
        List<AccessHierarchy> accessHierarchyListDel = new ArrayList<>();
        List<AccessHierarchy> accessHierarchyListUpdate = new ArrayList<>();
        List<AccessCompanyRelation> accessCompanyRelationList = new ArrayList<>();
        AccessHierarchy accessHierarchy = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
        if (accessHierarchy == null) return new HashSet<>();
        accessHierarchyListDel.add(accessHierarchy);
        accessCompanyRelationList.addAll(accessHierarchy.getAccessCompanyParentRelations());
        //load children
        loadDeleteChild(accessHierarchy, accessHierarchyListDel
                , accessHierarchyListUpdate, accessCompanyRelationList, false);

        if(otherMoldIdListOut!=null){

        List<AccessMold> accessMolds = accessMoldRepository.findAllByAccessCompanyRelationIdIn(accessCompanyRelationList
                .stream().map(a -> a.getId()).collect(Collectors.toList()));

        otherMoldIdListOut.addAll(accessMolds.stream().map(am -> am.getMoldId()).collect(Collectors.toList()));
        }

        Set<Long> companySet = accessHierarchyListDel.stream().map(ah -> ah.getCompanyId()).collect(Collectors.toSet());
        companySet.add(companyId);
        return companySet;
    }

	public boolean isRoot(Long companyId) {
		AccessHierarchy accessHierarchy = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
		return accessHierarchy.getLevel() != null && accessHierarchy.getLevel().equals(0L);
	}

    public List<Long> getFullCompanyIdAccessMold(Mold mold) {
        Set<Long> companyIds = getFullCompanyParentId(mold.getCompanyId());
        List<AccessMold> accessMoldList = accessMoldRepository.findAllByMoldId(mold.getId());
        Set<Long> cpnSet=accessMoldList.stream().map(a->a.getCompanyId()).collect(Collectors.toSet());
        cpnSet.stream().forEach(cpnId -> {
            if (!companyIds.contains(cpnId)) {
                Set<Long> companyIdNew = getFullCompanyParentId(cpnId);
                companyIds.addAll(companyIdNew);
            }
        });
        companyIds.add(mold.getCompanyId());
        return companyIds.stream().collect(Collectors.toList());
    }
}
