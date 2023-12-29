package saleson.api.versioning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.api.part.PartRepository;
import saleson.api.part.PartService;
import saleson.api.versioning.repositories.CompanyVersionRepository;
import saleson.api.versioning.repositories.LocationVersionRepository;
import saleson.api.versioning.repositories.PartVersionRepository;
import saleson.api.versioning.repositories.ReversionHistoryRepository;
import saleson.common.enumeration.RevisionObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.Part;
import saleson.model.clone.PartVersion;
import saleson.model.clone.RevisionHistory;

@Service
public class PartVersionService {

    @Autowired
    PartService partService;

    @Autowired
    PartVersionRepository partVersionRepository;

    @Autowired
    LocationVersionRepository locationVersionRepository;

    @Autowired
    ReversionHistoryRepository reversionHistoryRepository;
    public PartVersion clone(Part part){
        PartVersion partVersion = PartVersion.builder()
                .name(part.getName())
                .partCode(part.getPartCode())
                .resinCode(part.getResinCode())
                .resinGrade(part.getResinGrade())
                .designRevision(part.getDesignRevision())
                .size(part.getSize())
                .weight(part.getWeight())
                .categoryId(part.getCategoryId())
                .enabled(part.isEnabled())
                .build();
        partVersion.setOriginId(part.getId());
        return partVersion;
    }


    public Part convertToPart(PartVersion partVersion, Part part){
        if(partVersion == null) return null;

        if(part == null) part = new Part();
        else part.setId(partVersion.getOriginId());

        part.setName(partVersion.getName());
        part.setPartCode(partVersion.getPartCode());
        part.setResinCode(partVersion.getResinCode());
        part.setResinGrade(partVersion.getResinGrade());
        part.setDesignRevision(partVersion.getDesignRevision());
        part.setSize(partVersion.getSize());
        part.setWeight(partVersion.getWeight());
        part.setCategoryId(partVersion.getCategoryId());
        part.setEnabled(partVersion.isEnabled());
        return part;
    }

    public PartVersion writeHistoryPartVersion(Part part){
        PartVersion partVersion = partVersionRepository.save(clone(part));

        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionId(part.getId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.PART);

        reversionHistoryRepository.save(revisionHistory);
        return partVersion;
    }

    public void updatePartAndWriteHistory(PartVersion partVersion){
        if(partVersion == null || partVersion.getOriginId() == null) return;
        Part part = partService.findById(partVersion.getOriginId());
        writeHistoryPartVersion(part);
        partService.save(convertToPart(partVersion, part));
    }

    public Part test(){
        Part part = partService.findById(3882L);
        updatePartAndWriteHistory(clone(part));
        return null;
    }
}
