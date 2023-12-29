package saleson.api.versioning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.api.location.LocationRepository;
import saleson.api.location.LocationService;
import saleson.api.versioning.repositories.LocationVersionRepository;
import saleson.api.versioning.repositories.ReversionHistoryRepository;
import saleson.common.enumeration.RevisionObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.Location;
import saleson.model.clone.LocationVersion;
import saleson.model.clone.RevisionHistory;

import java.util.Optional;

@Service
public class LocationVersionService {
    @Autowired
    LocationVersionRepository locationVersionRepository;

    @Autowired
    ReversionHistoryRepository reversionHistoryRepository;

    @Autowired
    LocationService locationService;

    public LocationVersion clone(Location location){

        LocationVersion locationVersion = LocationVersion.builder()
                .name(location.getName())
                .locationCode(location.getLocationCode())
                .address(location.getAddress())
                .memo(location.getMemo())
                .enabled(location.isEnabled())
                .companyId(location.getCompanyId())
                .build();
        locationVersion.setOriginId(location.getId());
        return locationVersion;
    }

    public Location convertToLocation(LocationVersion locationVersion, Location location){
        if(locationVersion == null) return null;
        
        if(location == null) location = new Location();
        else location.setId(locationVersion.getOriginId());

        location.setName(locationVersion.getName());
        location.setLocationCode(locationVersion.getLocationCode());
        location.setAddress(locationVersion.getAddress());
        location.setMemo(locationVersion.getMemo());
        location.setEnabled(locationVersion.isEnabled());
        location.setCompanyId(locationVersion.getCompanyId());

        return location;
    }

    public void writeHistory(Location location){
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(location.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.LOCATION);
        reversionHistoryRepository.save(revisionHistory);
        locationVersionRepository.save(clone(location));
    }

    public void updateAndWriteHistory(LocationVersion locationVersion){
        if(locationVersion == null || locationVersion.getOriginId() == null){
            return;
        }
        Location location = locationService.findById(locationVersion.getOriginId());
        writeHistory(location);
        locationService.save(convertToLocation(locationVersion, location));
    }
}
