package com.stride.tracking.coreservice.model;

import com.stride.tracking.profile.dto.user.HeartRateZone;
import lombok.*;

@Builder(toBuilder = true)
public record HeartRateZoneValue(HeartRateZone zone, int min, int max, int value) {
}
