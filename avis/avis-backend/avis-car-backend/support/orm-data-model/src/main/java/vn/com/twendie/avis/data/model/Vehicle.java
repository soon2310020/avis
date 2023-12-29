package vn.com.twendie.avis.data.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

import static javax.persistence.FetchType.LAZY;

@Audited
@Table(name = "vehicle")
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Vehicle extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "number_plate")
    private String numberPlate;

    @Column(name = "type")
    private String type;

    @Column(name = "transmission_type")
    private Integer transmissionType;

    @Column(name = "owner")
    private String owner;

    @Column(name = "model")
    private String model;

    @Column(name = "color")
    private String color;

    @Column(name = "number_seat")
    private Integer numberSeat;

    @Column(name = "chassis_no")
    private String chassisNo;

    @Column(name = "engine_no")
    private String engineNo;

    @Column(name = "year_manufacture")
    private Integer yearManufacture;

    @Column(name = "start_using_date")
    private Timestamp startUsingDate;

    @Column(name = "registration_no")
    private String registrationNo;

    @Column(name = "travel_warrant_expiry_date")
    private Timestamp travelWarrantExpiryDate;

    @Column(name = "registration_to_date")
    private Timestamp registrationToDate;

    @Column(name = "insurance_no")
    private String insuranceNo;

    @Column(name = "insurance_expiry_date")
    private Timestamp insuranceExpiryDate;

    @Column(name = "road_fee_expiry_date")
    private Timestamp roadFeeExpiryDate;

    @Column(name = "liquidation_date")
    private Timestamp liquidationDate;

    @Column(name = "place_of_origin")
    private String placeOfOrigin;

    @Column(name = "status")
    private Integer status;

    @Column(name = "in_contract", nullable = false)
    private boolean inContract;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "current_journey_diary_id")
    private Long currentJourneyDiaryId;

    @Column(name = "current_contract_id")
    private Long currentContractId;

    @Column(name = "lending_contract_id")
    private Long lendingContractId;

    @ManyToOne
    @JoinColumn(name = "fuel_type_group_id", referencedColumnName = "id")
    private FuelTypeGroup fuelTypeGroup;

    @ManyToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "vehicle_supplier_group_id", referencedColumnName = "id")
    private VehicleSupplierGroup vehicleSupplierGroup;

    @ManyToOne
    @JoinColumn(name = "operation_admin_id", referencedColumnName = "id")
    private User operationAdmin;

    @ManyToOne
    @JoinColumn(name = "unit_operator_id", referencedColumnName = "id")
    private User unitOperator;

    @ManyToOne
    @JoinColumn(name = "accountant_id", referencedColumnName = "id")
    private User accountant;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "current_journey_diary_id", referencedColumnName = "id", insertable = false, updatable = false)
    private JourneyDiary currentJourneyDiary;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "current_contract_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Contract currentContract;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "lending_contract_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Contract lendingContract;

}