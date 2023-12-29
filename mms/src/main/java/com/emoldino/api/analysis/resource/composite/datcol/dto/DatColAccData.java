package com.emoldino.api.analysis.resource.composite.datcol.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL) 
@Data
public class DatColAccData implements Cloneable {
	private Double accMesuredTime; // gp_2
	private Double accMesuredValue; // gp_3
	private Double normAccValue; // gp_6
	private Double firstGradient; // gp_7
	private Double secondGradient; // gp_8
	private Integer firstGradientSign; // gp_9
	private Integer secondGradientSign; // gp _10
	private Integer determinant3; // gp_13
	private Integer determinant4; // gp_14
	private Integer filterLabel; // gp_15
	private Integer accLabel; // gp_16
	private Double processTime; // gp_17
	private Double processValue; // gp_18
	private Integer finalDet; // gp_101
	private Double timeDiff;
	private int det;
	private int det2;
	private int fin;
	
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }    
}
