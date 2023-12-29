package saleson.model.data.wut;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class BasedWUTData {
    private Integer index; // temp based: index of the block of uptime; ctt based: index of the hour of uptime
    private Integer indexWUT; // index of each 10 minutes [0, 1, 2, 3, etc.]
    private Double wut; // warm-up time value
}
