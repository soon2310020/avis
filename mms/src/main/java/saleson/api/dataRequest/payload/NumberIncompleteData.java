package saleson.api.dataRequest.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberIncompleteData {
    private Long locationNumber;
    private Long moldNumber;
    private Long partNumber;
    private Long machineNumber;
    private Long companyNumber;

    public boolean isCompleted() {
        return locationNumber == 0 && moldNumber == 0 && partNumber == 0
                && machineNumber == 0 && companyNumber == 0;
    }
}
