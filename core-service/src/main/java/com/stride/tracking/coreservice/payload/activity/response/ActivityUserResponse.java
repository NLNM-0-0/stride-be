package com.stride.tracking.coreservice.payload.activity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUserResponse {
    private String id;
    private String name;
    private String ava;
}
