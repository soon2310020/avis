package saleson.model.data;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoldLiteData {
  private Long id;
  private String equipmentCode;
  private Long locationId;
  private String locationCode;
  private String locationName;
  private Integer accumulatedShot;
  private PM_STRATEGY pmStrategy;

  public MoldLiteData(Long id, String equipmentCode, Long locationId, String locationCode, String locationName, Integer accumulatedShot) {
    this.id = id;
    this.equipmentCode = equipmentCode;
    this.locationId = locationId;
    this.locationCode = locationCode;
    this.locationName = locationName;
    this.accumulatedShot = accumulatedShot;
  }
}
