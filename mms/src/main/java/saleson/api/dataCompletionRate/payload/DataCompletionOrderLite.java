package saleson.api.dataCompletionRate.payload;

import lombok.Data;
import saleson.model.User;

import java.util.List;

@Data
public class DataCompletionOrderLite {
    private Long id;
    private String orderId;
    private String dueDay;
    private Integer numberOfCompanies;
    private Integer numberOfLocations;
    private Integer numberOfCategories;
    private Integer numberOfParts;
    private Integer numberOfMolds;
    private Integer numberOfMachines;

    private List<User> users;
    private boolean completed;
}
