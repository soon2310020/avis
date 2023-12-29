package com.emoldino.api.integration.resource.base.ai.repository.aipqresult;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.model.Category;
import saleson.model.Company;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.Part;
import saleson.model.Statistics;
import saleson.model.support.DateAudit;

@SuppressWarnings("serial")
@Table(indexes = @Index(name = "UX_CODE_DATA", columnList = "STATISTICS_ID, MOLD_ID", unique = true))
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AiPqResult extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Statistics statistics;
    @ManyToOne(fetch = FetchType.LAZY)
    private Counter counter;
    @ManyToOne(fetch = FetchType.LAZY)
    private Mold mold;
    @ManyToOne(fetch = FetchType.LAZY)
    private Part part;
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    private Category parent;
    @ManyToOne(fetch = FetchType.LAZY)
    private Company company;

    @Column(name = "STATISTICS_ID", insertable = false, updatable = false)
    private Long statisticsId;
    @Column(name = "COUNTER_ID", insertable = false, updatable = false)
    private Long counterId;
    @Column(name = "MOLD_ID", insertable = false, updatable = false)
    private Long moldId;
    @Column(name = "PART_ID", insertable = false, updatable = false)
    private Long partId;
    @Column(name = "CATEGORY_ID", insertable = false, updatable = false)
    private Long categoryId;
    @Column(name = "PARENT_ID", insertable = false, updatable = false)
    private Long parentId;
    @Column(name = "COMPANY_ID", insertable = false, updatable = false)
    private Long companyId;

    private String ctStatus;
    private String tempStatus;
    private String ppaStatus;
    private String hour;
    private String qdStatus;
    private String pqResult;
    private Instant pqResultTime;

    public AiPqResult(Long statisticsId, Long moldId, Long counterId, Long partId, Long companyId, Long categoryId, Long parentId) {
        this.statisticsId = statisticsId;
        this.moldId = moldId;
        this.counterId = counterId;
        this.partId = partId;
        this.companyId = companyId;
        this.categoryId = categoryId;
        this.parentId = parentId;
    }
}
