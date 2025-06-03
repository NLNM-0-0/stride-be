package com.stride.tracking.apigateway.constant;

import lombok.experimental.NonFinal;

public class AppConstant {
    private AppConstant() {}

    @NonFinal
    public static String[] publicEndpoints = {
            "/bridge/ping",

            "/core/ping",
            "/core/sports/all", //For sync sports between microservices

            "/identity/ping",
            "/identity/auth/**",
            "/identity/users/**",
            "/identity/users/register",

            "/profile/ping",

            "/metric/ping",
    };
}
