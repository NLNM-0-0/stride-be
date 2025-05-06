package com.stride.tracking.apigateway.constant;

import lombok.experimental.NonFinal;

public class AppConstant {
    private AppConstant() {}

    @NonFinal
    public static String[] publicEndpoints = {
            "/bridge/ping",
            "/bridge/emails/send",
            "/bridge/fcm/users/*",
            "/bridge/fcm/tokens/*",

            "/core/ping",

            "/identity/ping",
            "/identity/auth/**",
            "/identity/users/**",
            "/identity/users/register",

            "/profile/ping",

            "/route/ping",
    };
}
