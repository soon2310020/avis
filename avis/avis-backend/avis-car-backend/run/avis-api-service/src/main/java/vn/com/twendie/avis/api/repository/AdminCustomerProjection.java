package vn.com.twendie.avis.api.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import vn.com.twendie.avis.api.mapping.IdCardTypeMapping;
import vn.com.twendie.avis.api.mapping.Mapping;

import java.sql.Timestamp;
import java.util.Objects;

public interface AdminCustomerProjection {

    @JsonProperty("customer_id")
    Long getCustomerId();

    @JsonProperty("customer_name")
    String getCustomerName();

    @JsonProperty("tax_code")
    String getTaxCode();

    @JsonProperty("address")
    String getAddress();

    @JsonIgnore
    Timestamp getCustomerCreatedAt();

    @JsonProperty("member_customer_id")
    Long getMemberCustomerId();

    @JsonProperty("admin_name")
    String getAdminName();

    @JsonIgnore
    Timestamp getMemberCustomerCreatedAt();

    @JsonProperty("created_by")
    String getCreatedBy();

    @JsonProperty("customer_type")
    String getCustomerType();

    @JsonProperty("id_card")
    String getCustomerIdCard();

    @JsonProperty("card_type")
    @Mapping(IdCardTypeMapping.class)
    Integer getCustomerCardType();

    @JsonProperty("created_at")
    default Timestamp getCreatedAt() {
        return Objects.isNull(getMemberCustomerId()) ? getCustomerCreatedAt() : getMemberCustomerCreatedAt();
    }

    // ------------addition data for export------------------------------
    @JsonIgnore
    String getCustomerCode();
    @JsonIgnore
    String getCustomerCountryCode();
    @JsonIgnore
    String getCustomerMobile();
    @JsonIgnore
    String getCustomerEmail();
    @JsonIgnore
    String getBankAccountNumber();
    @JsonIgnore
    String getBankAccountHolder();
    @JsonIgnore
    String getBankName();
    @JsonIgnore
    String getCustomerTypeName();
    @JsonIgnore
    String getMemberCode();
    @JsonIgnore
    String getMemberRole();
    @JsonIgnore
    String getMemberDepartment();
    @JsonIgnore
    String getMemberMobile();
    @JsonIgnore
    String getMemberCountryCode();
    @JsonIgnore
    String getMemberEmail();


}
