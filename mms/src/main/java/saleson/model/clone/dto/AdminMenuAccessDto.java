package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminMenuAccessDto {

    private boolean companies;
    private boolean locations;
    private boolean category;
    private boolean parts;
    private boolean terminals;
    private boolean tooling;
    private boolean counters;
    private boolean reset;
//    private boolean corrective;
    private boolean terminalAlert;
    private boolean counterAlert;
    private boolean toolingAlert;
}
