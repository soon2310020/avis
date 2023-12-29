package com.stg.service.dto.crm;

import com.stg.service.caching.RmExcelDataCaching;
import com.stg.service.caching.base.ValueKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RmExcelInfo implements ValueKey {
    private String mbCode;
    private String fullName;
    private String email;
    private String phone;
    private String phone2;

    @Override
    public String key() {
        return RmExcelDataCaching.KEY.rmKey(mbCode);
    }
}
