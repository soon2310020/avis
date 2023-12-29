package saleson.restdocs.dto.exports;

import lombok.Data;
import saleson.model.Company;

import java.time.Instant;

@Data
public class UserInformationDto {

    private String name;
    private String email;
    private Company company;
    private Instant lastLogin;
}
