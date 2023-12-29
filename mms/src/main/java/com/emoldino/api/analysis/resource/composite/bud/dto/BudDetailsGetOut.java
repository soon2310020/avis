package com.emoldino.api.analysis.resource.composite.bud.dto;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetOut.BudDetails;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.CurrencyType;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class BudDetailsGetOut extends PageImpl<BudDetails> {

    private FltSupplier supplier;

    public BudDetailsGetOut(List<BudDetails> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BudDetailsGetOut(List<BudDetails> content, Pageable pageable, long total, FltSupplier supplier) {
        super(content, pageable, total);
        this.supplier = supplier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BudDetails {
        private Long moldId;
        private String moldCode;

        private Long cost; // Cost Of Tooling
        private Long salvage; // Salvage Value
        @Enumerated(EnumType.STRING)
        private CurrencyType costType;

        private int utilizationRate;
        @Enumerated(EnumType.STRING)
        private ToolingUtilizationStatus lifeCycleStatus;
    }

}
