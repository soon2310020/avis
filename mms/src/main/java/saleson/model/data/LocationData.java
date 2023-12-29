package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import saleson.dto.LocationDTO;
import saleson.model.Location;

@Data
@Builder
public class LocationData {
    private Location location;
    private Long numberOfTerminal;

    @QueryProjection
    @Builder
    public LocationData(Location location, Long numberOfTerminal) {
        this.location = location;
        this.numberOfTerminal = numberOfTerminal;
    }

    public static Location convertToModel(LocationData locationData){
        Location location = locationData.getLocation();
        if(location!= null) location.setNumberOfTerminal(locationData.getNumberOfTerminal());
        return location;
    }
}
