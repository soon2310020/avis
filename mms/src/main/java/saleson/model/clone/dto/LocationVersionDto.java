package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationVersionDto {
    private Long id;
    private String name;
    private String locationCode;
    private String address;
    private String memo;
    private boolean enabled;
    private String companyName;
}
