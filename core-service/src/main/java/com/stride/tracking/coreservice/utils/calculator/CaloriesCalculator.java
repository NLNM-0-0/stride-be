package com.stride.tracking.coreservice.utils.calculator;

import com.stride.tracking.coreservice.constant.RuleCaloriesType;
import com.stride.tracking.coreservice.model.Rule;
import com.stride.tracking.coreservice.model.Sport;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CaloriesCalculator {
    public int calculateCalories(Sport sport, int weight, long trainingSeconds, Map<RuleCaloriesType, Double> inputValues) {
        double met = calculateMET(sport, inputValues);


        return (int) Math.round(met * 3.5 * weight * trainingSeconds / 12000);
    }

    public double calculateMET(Sport sport, Map<RuleCaloriesType, Double> inputValues) {
        List<Rule> rules = sport.getRules();
        for (Rule rule : rules) {
            if (isConditionMet(rule, inputValues)) {
               return rule.getMet();
            }
        }
        return 0;
    }

    private boolean isConditionMet(Rule rule, Map<RuleCaloriesType, Double> inputValues) {
        String expression = rule.getExpression();
        return evaluateExpression(expression, inputValues);
    }

    private boolean evaluateExpression(String expression, Map<RuleCaloriesType, Double> inputValues) {
        // Extract all variables (e.g., [SPEED], [WEIGHT]) from the expression
        Set<String> variablesInExpression = extractVariablesFromExpression(expression);

        // Check if all the required variables are present in the input values map
        for (String variable : variablesInExpression) {
            try {
                RuleCaloriesType type = RuleCaloriesType.valueOf(variable);
                if (!inputValues.containsKey(type)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // 1. Chuẩn hóa biểu thức SpEL: thay [VAR] thành #VAR
        String normalizedExpression = expression.replaceAll("\\[([A-Z_]+)]", "#$1");

        // 2. Tạo context
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (Map.Entry<RuleCaloriesType, Double> entry : inputValues.entrySet()) {
            context.setVariable(entry.getKey().name(), entry.getValue());
        }

        // 3. Evaluate
        ExpressionParser parser = new SpelExpressionParser();
        try {
            Boolean result = parser.parseExpression(normalizedExpression).getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Set<String> extractVariablesFromExpression(String expression) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = Pattern.compile("\\[([A-Z_]+)]").matcher(expression);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
}
