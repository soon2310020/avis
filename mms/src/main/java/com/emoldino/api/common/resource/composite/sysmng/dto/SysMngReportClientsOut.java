package com.emoldino.api.common.resource.composite.sysmng.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SysMngReportClientsOut {
	private List<SysMngGetOut> content = new ArrayList<>();
	private List<SysMngDevice> abnormalTerminals = new ArrayList<>();
	private List<SysMngDevice> abnormalSensors = new ArrayList<>();
	private List<SysMngDevice> abnormalToolings = new ArrayList<>();
}
