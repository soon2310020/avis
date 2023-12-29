package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warning {
    private boolean warning;
    private String message;
    private List<String> days;

}
