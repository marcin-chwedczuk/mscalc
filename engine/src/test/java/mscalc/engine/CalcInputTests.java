package mscalc.engine;

import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mscalc.engine.CalcInput.MAX_STRLEN;
import static org.junit.jupiter.api.Assertions.*;

public class CalcInputTests {
    private static uint BASE_16 = uint.of(16);
    private static uint BASE_10 = uint.of(10);
    private static uint BASE_8 = uint.of(8);
    private static uint BASE_2 = uint.of(2);

    CalcInput calcInput;

    @BeforeEach
    void setup() {
        calcInput = new CalcInput('.');
    }

    @AfterEach
    void destroy() {
        calcInput.clear();
        calcInput.setDecimalSymbol('.');
    }

    @Test
    void testClear() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryToggleSign(false, "999");
        calcInput.tryAddDecimalPt();
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        calcInput.tryAddDigit(3, BASE_10, false, "999", 64, 32);

        assertEquals("-1.2e+3", calcInput.toString(BASE_10), "Verify input is correct.");

        calcInput.clear();

        System.out.println(calcInput.toString(BASE_10));
        assertEquals("0", calcInput.toString(BASE_10), "Verify input is 0 after clear.");
    }

    @Test
    void testTryToggleSignZero() {
        assertTrue(calcInput.tryToggleSign(false, "999"), "Verify toggling 0 succeeds.");
        assertEquals("0", calcInput.toString(BASE_10), "Verify toggling 0 does not create -0.");
    }

    @Test
    void testTryToggleSignExponent() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        assertTrue(calcInput.tryToggleSign(false, "999"), "Verify toggling exponent sign succeeds.");
        assertEquals("1.e-2", calcInput.toString(BASE_10), "Verify toggling exponent sign does not toggle base sign.");
        assertTrue(calcInput.tryToggleSign(false, "999"), "Verify toggling exponent sign succeeds.");
        assertEquals("1.e+2", calcInput.toString(BASE_10), "Verify toggling negative exponent sign does not toggle base sign.");
    }

    @Test
    void testTryToggleSignBase() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertTrue(calcInput.tryToggleSign(false, "999"), "Verify toggling base sign succeeds.");
        assertEquals("-1", calcInput.toString(BASE_10), "Verify toggling base sign creates negative base.");
        assertTrue(calcInput.tryToggleSign(false, "999"), "Verify toggling base sign succeeds.");
        assertEquals("1", calcInput.toString(BASE_10), "Verify toggling negative base sign creates positive base.");
    }

    @Test
    void testTryToggleSignBaseIntegerMode() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertTrue(calcInput.tryToggleSign(true, "999"), "Verify toggling base sign in integer mode succeeds.");
        assertEquals("-1", calcInput.toString(BASE_10), "Verify toggling base sign creates negative base.");
    }

    @Test
    void testTryToggleSignRollover() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        assertTrue(calcInput.tryToggleSign(true, "127"), "Verify toggling base sign in integer mode succeeds.");
        calcInput.tryAddDigit(8, BASE_10, false, "999", 64, 32);
        assertFalse(calcInput.tryToggleSign(true, "127"), "Verify toggling base sign in integer mode fails on rollover.");
        assertEquals("-128", calcInput.toString(BASE_10), "Verify toggling base sign on rollover does not change value.");
    }

    @Test
    void testTryAddDigitLeadingZeroes() {
        assertTrue(calcInput.tryAddDigit(0, BASE_10, false, "999", 64, 32), "Verify TryAddDigit succeeds.");
        assertTrue(calcInput.tryAddDigit(0, BASE_10, false, "999", 64, 32), "Verify TryAddDigit succeeds.");
        assertTrue(calcInput.tryAddDigit(0, BASE_10, false, "999", 64, 32), "Verify TryAddDigit succeeds.");
        assertEquals("0", calcInput.toString(BASE_10), "Verify leading zeros are ignored.");
    }

    @Test
    void testTryAddDigitMaxCount() {
        assertTrue(calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32), "Verify TryAddDigit for base with length < maxDigits succeeds.");
        assertEquals("1", calcInput.toString(BASE_10), "Verify adding digit for base with length < maxDigits succeeded.");
        assertFalse(calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 1), "Verify TryAddDigit for base with length > maxDigits fails.");
        assertEquals("1", calcInput.toString(BASE_10), "Verify digit for base was not added.");
        calcInput.tryBeginExponent();
        assertTrue(calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32), "Verify TryAddDigit for exponent with length < maxDigits succeeds.");
        assertTrue(calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32), "Verify TryAddDigit for exponent with length < maxDigits succeeds.");
        assertTrue(calcInput.tryAddDigit(3, BASE_10, false, "999", 64, 32), "Verify TryAddDigit for exponent with length < maxDigits succeeds.");
        assertTrue(calcInput.tryAddDigit(4, BASE_10, false, "999", 64, 32), "Verify TryAddDigit for exponent with length < maxDigits succeeds.");
        assertFalse(calcInput.tryAddDigit(5, BASE_10, false, "999", 64, 32), "Verify TryAddDigit for exponent with length > maxDigits fails.");
        assertEquals("1.e+1234", calcInput.toString(BASE_10), "Verify adding digits for exponent with length < maxDigits succeeded.");

        calcInput.clear();
        calcInput.tryAddDecimalPt();
        assertTrue(calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 1), "Verify decimal point and leading zero does not count toward maxDigits.");
        assertEquals("0.1", calcInput.toString(BASE_10), "Verify input value checking dec pt and leading zero impact on maxDigits.");
    }

    @Test
    void testTryAddDigitValues() {
        // Use an arbitrary value > 16 to test that input accepts digits > hexadecimal 0xF.
        // TryAddDigit does not validate whether the digit fits within the current radix.
        for (int i = 0; i < 25; i++) {
            assertTrue(calcInput.tryAddDigit(i, BASE_10, false, "999", 64, 32), ("Verify TryAddDigit succeeds for " + i));
            calcInput.clear();
        }
    }

    @Test
    void testTryAddDigitRolloverBaseCheck() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertFalse(calcInput.tryAddDigit(2, BASE_16, true, "999", 64, 1), "Verify TryAddDigit rollover fails for bases other than 8,10.");
        assertFalse(calcInput.tryAddDigit(1, BASE_2, true, "999", 64, 1), "Verify TryAddDigit rollover fails for bases other than 8,10.");
    }

    @Test
    void testTryAddDigitRolloverOctalByte() {
        calcInput.tryAddDigit(1, BASE_8, true, "777", 64, 32);
        assertTrue(calcInput.tryAddDigit(2, BASE_8, true, "377", 8, 1), "Verify we can add an extra digit in OctalByte if first digit <= 3.");

        calcInput.clear();
        calcInput.tryAddDigit(4, BASE_8, true, "777", 64, 32);
        assertFalse(calcInput.tryAddDigit(2, BASE_8, true, "377", 8, 1), "Verify we cannot add an extra digit in OctalByte if first digit > 3.");
    }

    @Test
    void testTryAddDigitRolloverOctalWord() {
        calcInput.tryAddDigit(1, BASE_8, true, "777", 64, 32);
        assertTrue(calcInput.tryAddDigit(2, BASE_8, true, "377", 16, 1), "Verify we can add an extra digit in OctalByte if first digit == 1.");

        calcInput.clear();
        calcInput.tryAddDigit(2, BASE_8, true, "777", 64, 32);
        assertFalse(calcInput.tryAddDigit(2, BASE_8, true, "377", 16, 1), "Verify we cannot add an extra digit in OctalByte if first digit > 1.");
    }

    @Test
    void testTryAddDigitRolloverOctalDword() {
        calcInput.tryAddDigit(1, BASE_8, true, "777", 64, 32);
        assertTrue(calcInput.tryAddDigit(2, BASE_8, true, "377", 32, 1), "Verify we can add an extra digit in OctalByte if first digit <= 3.");

        calcInput.clear();
        calcInput.tryAddDigit(4, BASE_8, true, "777", 64, 32);
        assertFalse(calcInput.tryAddDigit(2, BASE_8, true, "377", 32, 1), "Verify we cannot add an extra digit in OctalByte if first digit > 3.");
    }

    @Test
    void testTryAddDigitRolloverOctalQword() {
        calcInput.tryAddDigit(1, BASE_8, true, "777", 64, 32);
        assertTrue(calcInput.tryAddDigit(2, BASE_8, true, "377", 64, 1), "Verify we can add an extra digit in OctalByte if first digit == 1.");

        calcInput.clear();
        calcInput.tryAddDigit(2, BASE_8, true, "777", 64, 32);
        assertFalse(calcInput.tryAddDigit(2, BASE_8, true, "377", 64, 1), "Verify we cannot add an extra digit in OctalByte if first digit > 1.");
    }

    @Test
    void testTryAddDigitRolloverDecimal() {
        calcInput.tryAddDigit(1, BASE_10, true, "127", 64, 32);
        assertFalse(calcInput.tryAddDigit(0, BASE_10, true, "1", 8, 1), "Verify we cannot add a digit if input size matches maxStr size.");
        calcInput.tryAddDigit(2, BASE_10, true, "127", 64, 32);
        assertFalse(calcInput.tryAddDigit(2, BASE_10, true, "110", 8, 2), "Verify we cannot add a digit if n char comparison > 0.");
        assertTrue(calcInput.tryAddDigit(7, BASE_10, true, "130", 8, 2), "Verify we can add a digit if n char comparison < 0.");

        calcInput.clear();
        calcInput.tryAddDigit(1, BASE_10, true, "127", 64, 32);
        calcInput.tryAddDigit(2, BASE_10, true, "127", 64, 32);
        assertFalse(calcInput.tryAddDigit(8, BASE_10, true, "127", 8, 2), "Verify we cannot add a digit if digit exceeds max value.");
        assertTrue(calcInput.tryAddDigit(7, BASE_10, true, "127", 8, 2), "Verify we can add a digit if digit does not exceed max value.");

        calcInput.backspace();
        calcInput.tryToggleSign(true, "127");
        assertFalse(calcInput.tryAddDigit(9, BASE_10, true, "127", 8, 2), "Negative value: verify we cannot add a digit if digit exceeds max value.");
        assertTrue(
                calcInput.tryAddDigit(8, BASE_10, true, "127", 8, 2), "Negative value: verify we can add a digit if digit does not exceed max value.");
    }

    @Test
    void testTryAddDecimalPtEmpty() {
        assertFalse(calcInput.hasDecimalPoint(), "Verify input has no decimal point.");
        assertTrue(calcInput.tryAddDecimalPt(), "Verify adding decimal to empty input.");
        assertTrue(calcInput.hasDecimalPoint(), "Verify input has decimal point.");
        assertEquals("0.", calcInput.toString(BASE_10), "Verify decimal on empty input.");
    }

    @Test
    void testTryAddDecimalPointTwice() {
        assertFalse(calcInput.hasDecimalPoint(), "Verify input has no decimal point.");
        assertTrue(calcInput.tryAddDecimalPt(), "Verify adding decimal to empty input.");
        assertTrue(calcInput.hasDecimalPoint(), "Verify input has decimal point.");
        assertFalse(calcInput.tryAddDecimalPt(), "Verify adding decimal point fails if input has decimal point.");
    }

    @Test
    void testTryAddDecimalPointExponent() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        assertFalse(calcInput.tryAddDecimalPt(), "Verify adding decimal point fails if input has exponent.");
    }

    @Test
    void testTryBeginExponentNoExponent() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertTrue(calcInput.tryBeginExponent(), "Verify adding exponent succeeds on input without exponent.");
        assertEquals("1.e+0", calcInput.toString(BASE_10), "Verify exponent present.");
    }

    @Test
    void testTryBeginExponentWithExponent() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertTrue(calcInput.tryBeginExponent(), "Verify adding exponent succeeds on input without exponent.");
        assertFalse(calcInput.tryBeginExponent(), "Verify cannot add exponent if input already has exponent.");
    }

    @Test
    void testBackspaceZero() {
        calcInput.backspace();
        assertEquals("0", calcInput.toString(BASE_10), "Verify backspace on 0 is still 0.");
    }

    @Test
    void testBackspaceSingleChar() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertEquals("1", calcInput.toString(BASE_10), "Verify input before backspace.");
        calcInput.backspace();
        assertEquals("0", calcInput.toString(BASE_10), "Verify input after backspace.");
    }

    @Test
    void testBackspaceMultiChar() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        assertEquals("12", calcInput.toString(BASE_10), "Verify input before backspace.");
        calcInput.backspace();
        assertEquals("1", calcInput.toString(BASE_10), "Verify input after backspace.");
    }

    @Test
    void testBackspaceDecimal() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDecimalPt();
        assertEquals("1.", calcInput.toString(BASE_10), "Verify input before backspace.");
        assertTrue(calcInput.hasDecimalPoint(), "Verify input has decimal point.");
        calcInput.backspace();
        assertEquals("1", calcInput.toString(BASE_10), "Verify input after backspace.");
        assertFalse(calcInput.hasDecimalPoint(), "Verify decimal point was removed.");
    }

    @Test
    void testBackspaceMultiCharDecimal() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDecimalPt();
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(3, BASE_10, false, "999", 64, 32);
        assertEquals("1.23", calcInput.toString(BASE_10), "Verify input before backspace.");
        calcInput.backspace();
        assertEquals("1.2", calcInput.toString(BASE_10), "Verify input after backspace.");
    }

    // Issue #817: Prefixed multiple zeros
    @Test
    void testBackspaceZeroDecimalWithoutPrefixZeros() {
        calcInput.tryAddDigit(0, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDecimalPt();
        assertEquals("0.", calcInput.toString(BASE_10), "Verify input before backspace.");
        calcInput.backspace();
        calcInput.tryAddDigit(0, BASE_10, false, "999", 64, 32);
        assertEquals("0", calcInput.toString(BASE_10), "Verify input after backspace.");
    }

    @Test
    void testSetDecimalSymbol() {
        calcInput.tryAddDecimalPt();
        assertEquals("0.", calcInput.toString(BASE_10), "Verify default decimal point.");
        calcInput.setDecimalSymbol(',');
        assertEquals("0,", calcInput.toString(BASE_10), "Verify new decimal point.");
    }

    @Test
    void testToStringEmpty() {
        assertEquals("0", calcInput.toString(BASE_10), "Verify ToString of empty value.");
    }

    @Test
    void testToStringNegative() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryToggleSign(false, "999");
        assertEquals("-1", calcInput.toString(BASE_10), "Verify ToString of negative value.");
    }

    @Test
    void testToStringExponentBase10() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        assertEquals("1.e+0", calcInput.toString(BASE_10), "Verify ToString of empty base10 exponent.");
    }

    @Test
    void testToStringExponentBase8() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        assertEquals("1.^+0", calcInput.toString(BASE_8), "Verify ToString of empty base8 exponent.");
    }

    @Test
    void testToStringExponentNegative() {
        calcInput.tryAddDigit(1, BASE_8, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        calcInput.tryToggleSign(false, "999");
        assertEquals("1.e-0", calcInput.toString(BASE_10), "Verify ToString of empty negative exponent.");
    }

    @Test
    void testToStringExponentPositive() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(3, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(4, BASE_10, false, "999", 64, 32);
        assertEquals("1.e+234", calcInput.toString(BASE_10), "Verify ToString of exponent with value.");
    }

    @Test
    void testToStringInteger() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        assertEquals("1", calcInput.toString(BASE_10), "Verify ToString of integer value hides decimal.");
    }

    @Test
    void testToStringBaseTooLong() {
        StringBuilder maxStr = new StringBuilder();
        for (int i = 0; i < MAX_STRLEN + 1; i++) {
            maxStr.append('1');
            calcInput.tryAddDigit(1, BASE_10, false, maxStr.toString(), 64, 100);
        }
        var result = calcInput.toString(BASE_10);
        assertTrue(result.isEmpty(), "Verify ToString of base value that is too large yields empty string.");
    }

    @Test
    void testToStringExponentTooLong() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryBeginExponent();
        StringBuilder maxStr = new StringBuilder();
        maxStr.append("11");
        boolean exponentCapped = false;
        for (int i = 0; i < MAX_STRLEN + 1; i++) {
            maxStr.append('1');
            if (!calcInput.tryAddDigit(1, BASE_10, false, maxStr.toString(), 64, MAX_STRLEN + 25)) {
                exponentCapped = true;
            }
        }
        var result = calcInput.toString(BASE_10);

        // TryAddDigit caps the exponent length to C_EXP_MAX_DIGITS = 4, so ToString() succeeds.
        // If that cap is removed, ToString() should return an empty string.
        if (exponentCapped) {
            assertEquals("1.e+1111", result, "Verify ToString succeeds; exponent length is capped at C_EXP_MAX_DIGITS.");
        } else {
            assertTrue(result.isEmpty(), "Verify ToString of exponent value that is too large yields empty string.");
        }
    }

    @Test
    void testToRational() {
        calcInput.tryAddDigit(1, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(2, BASE_10, false, "999", 64, 32);
        calcInput.tryAddDigit(3, BASE_10, false, "999", 64, 32);
        assertEquals("123", calcInput.toString(BASE_10), "Verify input before conversion to rational.");

        var rat = calcInput.toRational(BASE_10, 0);
        assertEquals(1, rat.p().mantissa().length(), "Verify digit count of rational.");
        assertEquals(123, rat.p().mantissa().at(0).raw(), "Verify first digit of mantissa.");
    }

}
