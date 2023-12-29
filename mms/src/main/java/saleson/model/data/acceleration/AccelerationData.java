package saleson.model.data.acceleration;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AccelerationData {
    private Long dataId;
    private String moldCode;
    private String title;
    private String time;
    private String value;

    public AccelerationData(String moldCode, String title, String value) {
        this.moldCode = moldCode;
        this.title = title;
        this.value = value;
    }

    public AccelerationData(Long dataId,String moldCode, String title,  String value,String time) {
        this.dataId = dataId;
        this.moldCode = moldCode;
        this.title = title;
        this.time = time;
        this.value = value;
    }
}
