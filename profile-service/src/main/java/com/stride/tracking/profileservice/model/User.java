package com.stride.tracking.profileservice.model;

import com.stride.tracking.profile.dto.profile.Equipment;
import com.stride.tracking.profile.dto.profile.HeartRateZone;
import com.stride.tracking.profileservice.persistence.BaseEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User extends BaseEntity {
    private String name;
    private String ava;
    private String dob;
    private String email;
    private Integer height;
    private Integer weight;
    private Boolean male;
    private String city;
    private Integer maxHeartRate;
    private Map<Equipment, Integer> equipmentWeight;
    private Map<HeartRateZone, Integer> heartRateZones;
    private boolean isAdmin;
    private boolean isBlocked;
}
