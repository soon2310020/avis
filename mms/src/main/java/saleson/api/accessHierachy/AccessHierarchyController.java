package saleson.api.accessHierachy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.accessHierachy.payload.AccessHierarchyPayload;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.mold.MoldService;
import saleson.model.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-hierarchy")
public class AccessHierarchyController {
    @Autowired
    AccessHierarchyService accessHierarchyService;
    @Autowired
    AccessHierarchyRepository accessHierarchyRepository;

    @Lazy
    @Autowired
    MoldService moldService;

    @Autowired
    AccessCompanyRelationRepository accessCompanyRelationRepository;

    @GetMapping
    public ResponseEntity<Page<AccessHierarchy>> getAllAccessHierarchy(AccessHierarchyPayload payload) {

        Page<AccessHierarchy> pageContent = accessHierarchyService.findAll(payload.getPredicate(), PageRequest.of(0,
                Integer.MAX_VALUE));

        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    /**
     * Get Companies that have the ability to be the child
     *
     * @param payload
     * @param companyId
     * @param pageable
     * @return
     */
    @GetMapping("companies")
    public ResponseEntity<Page<Company>> getAllCompaniesAbilityToBeChild(CompanyPayload payload, @RequestParam(value = "companyId", required = false) Long companyId, Pageable pageable) {
        Page<Company> pageContent = accessHierarchyService.getAllCompaniesAbilityToBeChild(payload, companyId, pageable);
        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }


    /**
     * Use for create new relation for company
     *
     * @param payload
     * @return
     */
    @PostMapping
    public ResponseEntity addAccessHierarchy(@RequestBody AccessHierarchyPayload payload) {
        //check exist
        try {
            if (payload.getCompanyId() == null) {
                return ResponseEntity.badRequest().body("companyId empty.");
            }
            AccessHierarchy accessHierarchyExist = accessHierarchyRepository.findFirstByCompanyId(payload.getCompanyId()).orElse(null);
            if (accessHierarchyExist != null) {
                if (accessHierarchyExist.getParentIdList().contains(payload.getCompanyParentId()) || !accessHierarchyExist.getParentIdList().isEmpty() && payload.getCompanyParentId() == null) {
                    return ResponseEntity.badRequest().body("Relation with the company already exists.");
                }
                //check for case company exist children
                if (!accessHierarchyExist.getAccessCompanyChildRelations().isEmpty()) {
                    return ResponseEntity.badRequest().body("Company cannot has multiple parent.");
                }

            }

            //valid node parent cannot has children
            if (payload.getCompanyParentId() != null) {
                if(payload.getCompanyParentId().equals(payload.getCompanyId())){
                    return ResponseEntity.badRequest().body("Children must be different from their parents!");
                }
                AccessHierarchy a = accessHierarchyRepository.findFirstByCompanyId(payload.getCompanyParentId()).orElse(null);
                if(a==null){
                    return ResponseEntity.badRequest().body("Parent not exist!");
                }
                if (a.getAccessCompanyParentRelations().size() > 1) {
                    return ResponseEntity.badRequest().body("Company " + a.getCompany().getName() + " cannot has children!");
                }
            }

            if (payload.getParentIdList() != null) {
                List<AccessHierarchy> accessHierarchyList = accessHierarchyRepository.findAllByCompanyIdIn(payload.getParentIdList());
                for (AccessHierarchy a : accessHierarchyList) {
                    if (a.getAccessCompanyParentRelations().size() > 1) {
                        return ResponseEntity.badRequest().body("Company " + a.getCompany().getName() + " cannot has children!");
                    }
                }
            }
            accessHierarchyExist = accessHierarchyExist != null ? accessHierarchyExist : new AccessHierarchy();
            AccessHierarchy accessHierarchy = accessHierarchyService.save(payload.getModel(accessHierarchyExist), payload);
            return new ResponseEntity<>(accessHierarchy, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body("Error!");
    }

    /**
     * Use for change parent of company as update relation with parent
     * @param id
     * @param payload
     * @return
     */
/*
    @PutMapping("{id}")
    public ResponseEntity updateAccessHierarchy(@PathVariable(value = "id") Long id, @RequestBody AccessHierarchyPayload payload) {
        if (id == null) {
            return ResponseEntity.badRequest().body("id empty!");
        }
        AccessHierarchy accessHierarchy = accessHierarchyRepository.findById(id).orElse(null);
        if (accessHierarchy == null) {
            return ResponseEntity.badRequest().body("AccessHierarchy not found!");
        }
        //valid node parent cannot has multiple parent
        if (payload.getParentIdList() != null) {
            List<AccessHierarchy> accessHierarchyList = accessHierarchyRepository.findAllByCompanyIdIn(payload.getParentIdList());
            for (AccessHierarchy a : accessHierarchyList) {
                if (a.getAccessCompanyParentRelations().size() > 1) {
                    return ResponseEntity.badRequest().body("Company " + a.getCompany().getName() + " cannot has children!");
                }
            }
        }
        //valid node has children cannot has multiple parent


        AccessHierarchy res = accessHierarchyService.save(payload.getModel(accessHierarchy), payload);
        return new ResponseEntity<>(res, HttpStatus.OK);

    }
*/

    /**
     * Delete relation
     *
     * @param id
     * @return
     */
    @DeleteMapping("/company-relation/{id}")
    public ResponseEntity deleteAccessCompanyrelation(@PathVariable(value = "id") Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body("id empty!");
            }
            AccessCompanyRelation acr = accessCompanyRelationRepository.findById(id).orElse(null);
            if(acr==null)
                return ResponseEntity.badRequest().body("Relation not exist!");

            accessHierarchyService.deleteAccessCompanyRelation(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error!");
        }
        return new ResponseEntity<>("Success!", HttpStatus.OK);
    }

    /**
     * Delete this node
     *
     * @param companyId
     * @return
     */
    @DeleteMapping("{companyId}")
    public ResponseEntity deleteAccessHierarchy(@PathVariable(value = "companyId") Long companyId) {
        try {
            if (companyId == null) {
                return ResponseEntity.badRequest().body("id empty!");
            }
            AccessHierarchy accessHierarchy = accessHierarchyRepository.findFirstByCompanyId(companyId).orElse(null);
            if(accessHierarchy==null)
                return ResponseEntity.badRequest().body("Node not found!");
            accessHierarchyService.deleteAccessHierarchy(companyId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error!");
        }
        return new ResponseEntity<>("Success!", HttpStatus.OK);

    }


    //access mold

    /**
     * Get List mold of this company
     *
     * @param companyId
     * @return
     */
    @GetMapping("/molds/{companyId}")
    public ResponseEntity<Page<Mold>> getAllMoldOfCompany(@PathVariable(value = "companyId") Long companyId) {
        AccessHierarchyPayload payload = new AccessHierarchyPayload();
        payload.setCompanyOwnerId(companyId);
        Page<Mold> pageContent = moldService.findAll(payload.getPredicateMoldOfCompany(), PageRequest.of(0,
                Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "equipmentCode")));
        return new ResponseEntity<>(pageContent, HttpStatus.OK);

    }

    /**
     * Get list access mold of company parent(companyId)
     *
     * @param payload: companyId, accessCompanyRelationId
     * @return
     */
    @GetMapping("/access-mold")
    public ResponseEntity<Page<AccessMold>> getAccessMoldOfCompany(AccessHierarchyPayload payload) {

        Page<AccessMold> pageContent = accessHierarchyService.findAllAccessMold(payload.getPredicateAccessMold(), PageRequest.of(0,
                Integer.MAX_VALUE));
        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    /**
     * update list mold access for company parent
     *
     * @param accessHierarchyId
     * @param payload
     * @return
     */
    @PostMapping("/assigned-tooling/{accessHierarchyId}")
    public ResponseEntity assignedTooling(@PathVariable(value = "accessHierarchyId") Long accessHierarchyId, @RequestBody AccessHierarchyPayload payload) {
        try {


//            payload.setAccessCompanyRelationId(accessHierarchyId);
            List<AccessMold> accessMoldList = accessHierarchyService.assignedTooling(payload,accessHierarchyId);

            return new ResponseEntity<>(accessMoldList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body("Error!");
    }


}
