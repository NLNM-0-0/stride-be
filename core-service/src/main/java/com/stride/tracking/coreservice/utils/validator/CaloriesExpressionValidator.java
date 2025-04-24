package com.stride.tracking.coreservice.utils.validator;

import com.stride.tracking.coreservice.constant.RuleCaloriesType;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CaloriesExpressionValidator {
    private CaloriesExpressionValidator() {}

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\[[A-Z_]+]|\\d+(\\.\\d+)?|[=!<>]=?|[()\\s]|&&|\\|\\|"
    );

    private static final Set<String> ALLOWED_VARIABLES = Arrays.stream(RuleCaloriesType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    public static boolean isValid(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(expression);
        while (matcher.find()) {
            String token = matcher.group().trim();

            //Check whether token is [SPEED], [WEIGHT], ... or not
            if (token.matches("\\[[A-Z_]+]")) {
                String type = token.substring(1, token.length() - 1);
                if (!ALLOWED_VARIABLES.contains(type)) {
                    return false;
                }
            }
        }

        // Check if there are any illegal characters remaining
        String cleaned = matcher.replaceAll("").replaceAll("\\s+", "");
        return cleaned.isEmpty();
    }
}
