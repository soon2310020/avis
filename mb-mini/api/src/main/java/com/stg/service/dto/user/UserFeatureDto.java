package com.stg.service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserFeatureDto {

    private List<Integer> featureIds;
//    @NotEmpty(message = "Must select at least one user to update")
    private List<Long> userIds;

}
