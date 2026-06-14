package com.hellokaton.blade.validator;

import com.hellokaton.blade.exception.ValidatorException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Validators, Validation composition, and ValidationResult
 */
public class ValidatorsEnhancedTest {

    // ======================== notNull ========================

    @Test
    void testNotNull_pass() {
        ValidationResult result = Validators.notNull().test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testNotNull_fail() {
        ValidationResult result = Validators.notNull().test(null);
        assertFalse(result.isValid());
        assertNotNull(result.getMessage());
    }

    @Test
    void testNotNullWithCustomMessage() {
        ValidationResult result = Validators.notNull("custom error").test(null);
        assertFalse(result.isValid());
        assertEquals("custom error", result.getMessage());
    }

    // ======================== notEmpty ========================

    @Test
    void testNotEmpty_pass() {
        ValidationResult result = Validators.notEmpty().test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testNotEmpty_failOnEmpty() {
        ValidationResult result = Validators.notEmpty().test("");
        assertFalse(result.isValid());
    }

    @Test
    void testNotEmpty_failOnNull() {
        ValidationResult result = Validators.notEmpty().test(null);
        assertFalse(result.isValid());
    }

    @Test
    void testNotEmptyWithCustomMessage() {
        ValidationResult result = Validators.notEmpty("required").test("");
        assertFalse(result.isValid());
        assertEquals("required", result.getMessage());
    }

    // ======================== moreThan ========================

    @Test
    void testMoreThan_pass() {
        ValidationResult result = Validators.moreThan(3).test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testMoreThan_exactLength() {
        ValidationResult result = Validators.moreThan(5).test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testMoreThan_fail() {
        ValidationResult result = Validators.moreThan(10).test("hi");
        assertFalse(result.isValid());
    }

    // ======================== lessThan ========================

    @Test
    void testLessThan_pass() {
        ValidationResult result = Validators.lessThan(10).test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testLessThan_exactLength() {
        ValidationResult result = Validators.lessThan(5).test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testLessThan_fail() {
        ValidationResult result = Validators.lessThan(3).test("hello");
        assertFalse(result.isValid());
    }

    // ======================== length ========================

    @Test
    void testLength_pass() {
        ValidationResult result = Validators.length(3, 10).test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testLength_tooShort() {
        ValidationResult result = Validators.length(3, 10).test("hi");
        assertFalse(result.isValid());
    }

    @Test
    void testLength_tooLong() {
        ValidationResult result = Validators.length(3, 5).test("hello world");
        assertFalse(result.isValid());
    }

    // ======================== contains ========================

    @Test
    void testContains_pass() {
        ValidationResult result = Validators.contains("world").test("hello world");
        assertTrue(result.isValid());
    }

    @Test
    void testContains_fail() {
        ValidationResult result = Validators.contains("xyz").test("hello world");
        assertFalse(result.isValid());
    }

    // ======================== lowerThan ========================

    @Test
    void testLowerThan_pass() {
        ValidationResult result = Validators.lowerThan(10).test(5);
        assertTrue(result.isValid());
    }

    @Test
    void testLowerThan_fail() {
        ValidationResult result = Validators.lowerThan(5).test(10);
        assertFalse(result.isValid());
    }

    @Test
    void testLowerThan_equal() {
        // must be < max, not <=, so equal should fail
        ValidationResult result = Validators.lowerThan(5).test(5);
        assertFalse(result.isValid());
    }

    // ======================== greaterThan ========================

    @Test
    void testGreaterThan_pass() {
        ValidationResult result = Validators.greaterThan(5).test(10);
        assertTrue(result.isValid());
    }

    @Test
    void testGreaterThan_fail() {
        ValidationResult result = Validators.greaterThan(10).test(5);
        assertFalse(result.isValid());
    }

    @Test
    void testGreaterThan_equal() {
        // must be > min, not >=, so equal should fail
        ValidationResult result = Validators.greaterThan(5).test(5);
        assertFalse(result.isValid());
    }

    // ======================== range ========================

    @Test
    void testRange_pass() {
        ValidationResult result = Validators.range(1, 10).test(5);
        assertTrue(result.isValid());
    }

    @Test
    void testRange_tooLow() {
        // range is (min, max) exclusive on both ends
        ValidationResult result = Validators.range(5, 10).test(3);
        assertFalse(result.isValid());
    }

    @Test
    void testRange_tooHigh() {
        ValidationResult result = Validators.range(1, 5).test(10);
        assertFalse(result.isValid());
    }

    // ======================== isEmail ========================

    @Test
    void testIsEmail_pass() {
        ValidationResult result = Validators.isEmail().test("user@example.com");
        assertTrue(result.isValid());
    }

    @Test
    void testIsEmail_fail() {
        ValidationResult result = Validators.isEmail().test("not-an-email");
        assertFalse(result.isValid());
    }

    @Test
    void testIsEmail_empty() {
        ValidationResult result = Validators.isEmail().test("");
        assertFalse(result.isValid());
    }

    // ======================== isURL ========================

    @Test
    void testIsURL_pass() {
        ValidationResult result = Validators.isURL().test("https://example.com");
        assertTrue(result.isValid());
    }

    @Test
    void testIsURL_fail() {
        ValidationResult result = Validators.isURL().test("not a url");
        assertFalse(result.isValid());
    }

    @Test
    void testIsURL_empty() {
        ValidationResult result = Validators.isURL().test("");
        assertFalse(result.isValid());
    }

    // ======================== Validation composition ========================

    @Test
    void testValidationAnd_bothPass() {
        Validation<String> v = Validators.notEmpty().and(Validators.moreThan(3));
        ValidationResult result = v.test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testValidationAnd_firstFails() {
        Validation<String> v = Validators.notEmpty().and(Validators.moreThan(3));
        ValidationResult result = v.test("");
        assertFalse(result.isValid());
    }

    @Test
    void testValidationAnd_secondFails() {
        Validation<String> v = Validators.notEmpty().and(Validators.moreThan(10));
        ValidationResult result = v.test("hi");
        assertFalse(result.isValid());
    }

    @Test
    void testValidationOr_firstPasses() {
        Validation<String> v = Validators.notEmpty().or(Validators.contains("xyz"));
        ValidationResult result = v.test("hello");
        assertTrue(result.isValid());
    }

    @Test
    void testValidationOr_secondPasses() {
        Validation<String> v = Validators.notEmpty().or(Validators.notNull());
        ValidationResult result = v.test("");
        // notEmpty fails on "", but notNull passes on "" (it's not null)
        assertTrue(result.isValid());
    }

    @Test
    void testValidationOr_bothFail() {
        Validation<String> v = Validators.notEmpty().or(Validators.moreThan(5));
        ValidationResult result = v.test("");
        // notEmpty fails, moreThan also fails on empty string
        assertFalse(result.isValid());
    }

    // ======================== ValidationResult ========================

    @Test
    void testValidationResult_ok() {
        ValidationResult result = ValidationResult.ok();
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void testValidationResult_okWithCode() {
        ValidationResult result = ValidationResult.ok("200");
        assertTrue(result.isValid());
        assertEquals("200", result.getCode());
    }

    @Test
    void testValidationResult_fail() {
        ValidationResult result = ValidationResult.fail("error msg");
        assertFalse(result.isValid());
        assertEquals("error msg", result.getMessage());
    }

    @Test
    void testValidationResult_failWithCode() {
        ValidationResult result = ValidationResult.fail("400", "bad request");
        assertFalse(result.isValid());
        assertEquals("400", result.getCode());
        assertEquals("bad request", result.getMessage());
    }

    @Test
    void testValidationResult_throwIfInvalid_noThrow() {
        ValidationResult result = ValidationResult.ok();
        assertDoesNotThrow(() -> result.throwIfInvalid());
    }

    @Test
    void testValidationResult_throwIfInvalid_throws() {
        ValidationResult result = ValidationResult.fail("error");
        assertThrows(ValidatorException.class, result::throwIfInvalid);
    }

    @Test
    void testValidationResult_throwIfInvalidWithFieldName() {
        ValidationResult result = ValidationResult.fail("is required");
        ValidatorException ex = assertThrows(ValidatorException.class,
                () -> result.throwIfInvalid("name"));
        assertTrue(ex.getMessage().contains("name"));
        assertTrue(ex.getMessage().contains("is required"));
    }

    @Test
    void testValidationResult_throwMessage() {
        ValidationResult result = ValidationResult.fail("original");
        ValidatorException ex = assertThrows(ValidatorException.class,
                () -> result.throwMessage("custom message"));
        assertEquals("custom message", ex.getMessage());
    }

    @Test
    void testValidationResult_throwMessage_validNoThrow() {
        ValidationResult result = ValidationResult.ok();
        assertDoesNotThrow(() -> result.throwMessage("custom"));
    }

    // ======================== SimpleValidation ========================

    @Test
    void testSimpleValidation_from_pass() {
        SimpleValidation<Integer> v = SimpleValidation.from(i -> i > 0, "must be positive");
        ValidationResult result = v.test(5);
        assertTrue(result.isValid());
    }

    @Test
    void testSimpleValidation_from_fail() {
        SimpleValidation<Integer> v = SimpleValidation.from(i -> i > 0, "must be positive");
        ValidationResult result = v.test(-1);
        assertFalse(result.isValid());
        assertEquals("must be positive", result.getMessage());
    }
}
