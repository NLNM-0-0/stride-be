package com.stride.tracking.coreservice.payload.activity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateActivityRequest {
    private int rpe;
    private String name;
    private String sportId;
    private List<String> images;
    private String description;
}
