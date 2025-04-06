package com.stride.tracking.profileservice.model;

import com.stride.tracking.dto.constant.Equipment;
import com.stride.tracking.dto.constant.HeartRateZone;
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
    private int height;
    private int weight;
    private boolean male;
    private String city;
    private int maxHeartRate;
    private Map<Equipment, Integer> equipmentsWeight;
    private Map<HeartRateZone, Integer> heartRateZones;
    private boolean isBlock;
}
