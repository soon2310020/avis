package saleson.api.dataCompletionRate.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.*;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCompletionOrderPayload {
    private Long id;
    private String orderId;

    private boolean allCompany = false;
    private boolean allLocation = false;
    private boolean allCategory = false;
    private boolean allPart = false;
    private boolean allTooling = false;
    private boolean allMachine = false;

    private List<Company> companies = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Part> parts = new ArrayList<>();
    private List<Mold> molds = new ArrayList<>();
    private List<Machine> machines = new ArrayList<>();

    private String dueDay;

    private List<User> assignedUsers = new ArrayList<>();
}
