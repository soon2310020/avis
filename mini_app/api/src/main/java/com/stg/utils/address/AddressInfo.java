package com.stg.utils.address;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressInfo {
    private String provinceName;
    private String districtName;
    private String wardName;
    private String streetName;
    private String subAddressUnknown; // có giá trị khi provinceName | districtName | wardName = null

    public AddressInfo(String streetName, String wardName, String districtName, String provinceName) {
        this.streetName = streetName;
        this.wardName = wardName;
        this.districtName = districtName;
        this.provinceName = provinceName;
    }

    @Override
    public String toString() {
        return "AddressInfo{" +
                "subAddressUnknown='" + subAddressUnknown + '\'' +
                ", streetName='" + streetName + '\'' +
                ", wardName='" + wardName + '\'' +
                ", districtName='" + districtName + '\'' +
                ", provinceName='" + provinceName + '\'' +
                '}';
    }

}
