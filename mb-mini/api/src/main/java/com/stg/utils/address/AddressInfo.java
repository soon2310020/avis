package com.stg.utils.address;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class AddressInfo {
    private String key;

    private String provinceName;
    private String districtName;
    private String wardName;
    private String streetName;

    public AddressInfo(String streetName) {
        this.streetName = streetName;
    }

    public AddressInfo(String streetName, String wardName, String districtName, String provinceName) {
        this.streetName = streetName;
        this.wardName = wardName;
        this.districtName = districtName;
        this.provinceName = provinceName;
    }

    @Override
    public String toString() {
        return "AddressInfo{" +
                "streetName='" + streetName + '\'' +
                ", wardName='" + wardName + '\'' +
                ", districtName='" + districtName + '\'' +
                ", provinceName='" + provinceName + '\'' +
                '}';
    }

    /***/
    public String fullAddress() {
        String addressDto = this.getStreetName() == null ? "" : this.getStreetName();
        if (StringUtils.hasText(this.getWardName())) addressDto += " - " + this.getWardName();
        if (StringUtils.hasText(this.getDistrictName())) addressDto += " - " + this.getDistrictName();
        if (StringUtils.hasText(this.getProvinceName())) addressDto += " - " + this.getProvinceName();
        return addressDto;
    }
}
