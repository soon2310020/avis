package saleson.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import java.time.Instant;

public class StatisticsData implements Comparable<StatisticsData> {

	private String moldCode;

	private String title;
	private Integer data;
	private Integer moldCount;

	private Long moldCreatedAt;
	private Long aliveTime;
	private Long uptime;

	private Double cycleTime;
	private Integer contractedCycleTime;

	@QueryProjection
	public StatisticsData(String moldCode, String title, Integer data){
		this.moldCode = moldCode;
		this.title = title;
		this.data = data;
	}

	@QueryProjection
	public StatisticsData(String moldCode, Instant moldCreatedAt, Long uptime){
		this.moldCode = moldCode;
		this.moldCreatedAt = moldCreatedAt.getEpochSecond();
		long current = Instant.now().getEpochSecond();
		long registeredTime = moldCreatedAt.getEpochSecond();
		this.aliveTime = current - registeredTime;
		this.uptime = uptime;
	}

	public StatisticsData(String moldCode, Integer data){
		this.moldCode = moldCode;
		this.data = data;
	}

	public StatisticsData(String moldCode, String title, Double cycleTime){
		this.moldCode = moldCode;
		this.title = title;
		this.cycleTime = cycleTime;
	}

	public StatisticsData(String title, Integer data, Integer moldCount){
		this.title = title;
		this.data = data;
		this.moldCount = moldCount;
	}

	public String getMoldCode() {
		return moldCode;
	}

	public void setMoldCode(String moldCode) {
		this.moldCode = moldCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getData() {
		return data;
	}

	public void setData(Integer data) {
		this.data = data;
	}

	public Integer getMoldCount() {
		return moldCount;
	}

	public void setMoldCount(Integer moldCount) {
		this.moldCount = moldCount;
	}

	public Integer getContractedCycleTime() {
		return contractedCycleTime;
	}

	public void setContractedCycleTime(Integer contractedCycleTime) {
		this.contractedCycleTime = contractedCycleTime;
	}

	public Double getCycleTime() {
		return cycleTime;
	}

	public void setCycleTime(Double cycleTime) {
		this.cycleTime = cycleTime;
	}

	public Long getMoldCreatedAt(){ return moldCreatedAt; }

	public void setMoldCreatedAt(Long moldCreatedAt){ this.moldCreatedAt = moldCreatedAt; }

	public Long getAliveTime(){ return aliveTime; }

	public void setAliveTime(Long aliveTime){ this.aliveTime = aliveTime; }

	public Long getUptime(){ return uptime; }

	public void setUptime(Long uptime){ this.uptime = uptime; }

	@Override
	public int compareTo(StatisticsData o) {
		return Integer.compare(Integer.parseInt(title), Integer.parseInt(o.getTitle()));

	}
}
