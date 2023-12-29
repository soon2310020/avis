package com.stg.service.dto.insurance;

import com.stg.service.dto.external.PackageNameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PackagePhotoDto {

    private PackageNameEnum type;

    private String image;

}
