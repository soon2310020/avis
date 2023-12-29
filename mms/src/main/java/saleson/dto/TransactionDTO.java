package saleson.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.DataUtils;
import saleson.model.Transfer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO
{
//    private Long id;
    private Long datetime;
//    private String at;
    @JsonProperty("tooling_id")
    private String ti;
    @JsonProperty("counter_id")
    private String ci;
    //    @JsonProperty("end_datetime")
//    private String rt;
//    @JsonProperty("last_shot_datetime")
//    private String lst;
    @JsonProperty("shot_count")
    private Integer sc;
    @JsonProperty("cycle_time")
    private Double ct;            // 정의서에는 INT(11) 소수점이 넘어와서 Double로 변경..
    /*
        @JsonProperty("frequency")
        private String cf;			// 정의서에는 INT(11) Java는 String => String으로 변경..
        @JsonProperty("temperature")
        private String temp;		// 온도데이터
        @JsonProperty("temp_datetime")
        private String tff;			// 온도1 시간
    */
    private String date;

    @JsonIgnore
    private Integer scMax;


//    @QueryProjection
    public TransactionDTO(String ti, String ci, Integer sc, Double ct)
    {
        this.ti = ti;
        this.ci = ci;
        this.sc = sc;
        this.ct = ct;
    }
    @QueryProjection
    public TransactionDTO(String ti, String ci, Integer sc, Integer scMax)
    {
        this.ti = ti;
        this.ci = ci;
        this.sc = sc;
        this.scMax = scMax;
    }

    public static TransactionDTO convertToDTO(Transfer transfer)
    {
        TransactionDTO transactionDTO = DataUtils.mapper.map(transfer, TransactionDTO.class);
        transactionDTO.setDatetime(transfer.getCreatedAt().toEpochMilli());


        return transactionDTO;

    }
}
