package com.stride.tracking.coreservice.utils.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaloriesExpressionValidatorTest {

    @Test
    void testValidSimpleExpressions() {
        assertTrue(CaloriesExpressionValidator.isValid("[SPEED] > 5"));
        assertTrue(CaloriesExpressionValidator.isValid("[EQUIPMENT_WEIGHT] <= 70"));
        assertTrue(CaloriesExpressionValidator.isValid("[SPEED] > 5 and [EQUIPMENT_WEIGHT] < 80"));
        assertTrue(CaloriesExpressionValidator.isValid("([SPEED] >= 4) or ([EQUIPMENT_WEIGHT] < 60)"));
        assertTrue(CaloriesExpressionValidator.isValid("[SPEED] == 5"));
    }

    @Test
    void testInvalidVariableNames() {
        assertFalse(CaloriesExpressionValidator.isValid("[AGE] > 20"));
        assertFalse(CaloriesExpressionValidator.isValid("[HEIGHT] < 170"));
        assertFalse(CaloriesExpressionValidator.isValid("[PASSWORD] == '123'"));
    }

    @Test
    void testInvalidCharacters() {
        assertFalse(CaloriesExpressionValidator.isValid("[SPEED] > 5; DROP TABLE users"));
        assertFalse(CaloriesExpressionValidator.isValid("[SPEED] > 5 + alert('xss')"));
    }

    @Test
    void testMixedValid() {
        assertFalse(CaloriesExpressionValidator.isValid("[SPEED] > 5 and [HACK] == 1"));
        assertTrue(CaloriesExpressionValidator.isValid("[SPEED] > 5 or 1 == 1"));
    }

    @Test
    void testEmptyAndNullExpression() {
        assertFalse(CaloriesExpressionValidator.isValid(""));
        assertFalse(CaloriesExpressionValidator.isValid("   "));
        assertFalse(CaloriesExpressionValidator.isValid(null));
    }
}