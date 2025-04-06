package com.stride.tracking.apigateway.constant;

import lombok.experimental.NonFinal;

public class AppConstant {
    private AppConstant() {}

    @NonFinal
    public static String[] publicEndpoints = {
            "/identity/auth/**",
            "/identity/users/**",
            "/notification/emails/send",
            "/identity/users/register"
    };
}
