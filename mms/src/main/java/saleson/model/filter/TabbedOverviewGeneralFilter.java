package saleson.model.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.*;
import saleson.model.data.MiniComponentData;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor
public class TabbedOverviewGeneralFilter extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TABBED_OVERVIEW_GENERAL_FILTER_PRODUCT",
            joinColumns = @JoinColumn(name = "TABBED_OVERVIEW_GENERAL_FILTER_ID"),
            inverseJoinColumns = @JoinColumn(name = "PRODUCT_ID")
    )
    private List<Category> products;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TABBED_OVERVIEW_GENERAL_FILTER_PART",
            joinColumns = @JoinColumn(name = "TABBED_OVERVIEW_GENERAL_FILTER_ID"),
            inverseJoinColumns = @JoinColumn(name = "PART_ID")
    )
    private List<Part> parts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TABBED_OVERVIEW_GENERAL_FILTER_TOOLING",
            joinColumns = @JoinColumn(name = "TABBED_OVERVIEW_GENERAL_FILTER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TOOLING_ID")
    )
    private List<Mold> toolings;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TABBED_OVERVIEW_GENERAL_FILTER_SUPPLIER",
            joinColumns = @JoinColumn(name = "TABBED_OVERVIEW_GENERAL_FILTER_ID"),
            inverseJoinColumns = @JoinColumn(name = "SUPPLIER_ID")
    )
    private List<Company> suppliers;


    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean allProduct;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean allPart;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean allTooling;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean allSupplier;

    private Integer duration; // number of days in time period filter

    public List<MiniComponentData> getToolingsSelected() {
        if(this.toolings!=null)
            return this.toolings.stream().map(o->{
                return new MiniComponentData(o.getId(),o.getEquipmentCode());
            }).collect(Collectors.toList());
        return null;
    }
    public List<MiniComponentData> getPartsSelected() {
        if(this.parts!=null)
            return this.parts.stream().map(o->{
                return new MiniComponentData(o.getId(),o.getPartCode(),o.getName());
            }).collect(Collectors.toList());
        return null;
    }
    public List<MiniComponentData> getProductsSelected() {
        if(this.products!=null)
            return this.products.stream().map(o->{
                return new MiniComponentData(o.getId(),o.getName());
            }).collect(Collectors.toList());
        return null;
    }
    public List<MiniComponentData> getSuppliersSelected() {
        if(this.suppliers!=null)
            return this.suppliers.stream().map(o->{
                return new MiniComponentData(o.getId(),o.getName());
            }).collect(Collectors.toList());
        return null;
    }

    public TabbedOverviewGeneralFilter(boolean allProduct, boolean allPart, boolean allTooling, boolean allSupplier) {
        this.allProduct = allProduct;
        this.allPart = allPart;
        this.allTooling = allTooling;
        this.allSupplier = allSupplier;
        this.duration = 7;
    }
}
