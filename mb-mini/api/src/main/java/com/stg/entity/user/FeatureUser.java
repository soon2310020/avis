package com.stg.entity.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "feature_user")
@Accessors(chain = true)
public class FeatureUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "feature")
    private String feature;

    public List<String> getFeatureAsList() {
        List<String> features = new ArrayList<>();
        if (this.getFeature() == null) {
            return Collections.emptyList();
        }
        if (this.getFeature().contains("[")) {
            String featureStr = this.getFeature().substring(1, this.getFeature().length() - 1);
            features = Arrays.asList(featureStr.split(","));
        }
        return features.parallelStream().map(String::trim).collect(Collectors.toList());
    }

}
