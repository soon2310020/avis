package saleson.restdocs.dto;

import lombok.Data;

//Use to export file pdf
@Data
public class MoldDtoPDF {
    private String toolingID;
    private String counter;
    private String location;
    private String lastShot;
    private String cycleTime;
    private String op;
    private String status;
}
