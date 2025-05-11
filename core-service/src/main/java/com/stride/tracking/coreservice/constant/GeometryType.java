package com.stride.tracking.coreservice.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeometryType {
    LINESTRING("LineString"),
    ;

    private final String value;
}
