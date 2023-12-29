package saleson.model;

import lombok.Data;

import java.time.Instant;

@Data
public class MiniMold {
    private Long id;
    private String equipmentCode;
    private Instant lastShotAt;
}
