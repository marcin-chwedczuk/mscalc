package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.cpp.uint;
import mscalc.engine.resource.JavaBundleResourceProvider;
import mscalc.engine.resource.ResourceProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static mscalc.engine.EngineStrings.IDS_ERR_INPUT_OVERFLOW;
import static mscalc.engine.EngineStrings.IDS_ERR_UNK_CH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CCalcEngineTests {
    final int MAX_HISTORY_SIZE = 20;

    CCalcEngine m_calcEngine;
    ResourceProvider m_resourceProvider;
    HistoryDisplay m_history;

    @BeforeEach
    public void before()
    {
        m_resourceProvider = new JavaBundleResourceProvider();
        // m_history = new HistoryDisplay(MAX_HISTORY_SIZE);
        m_history = new HistoryDisplay() {
            @Override
            public int addToHistory(List<Pair<String, Integer>> tokens, List<IExpressionCommand> commands, String result) {
                return 0;
            }

            @Override
            public void close() throws Exception {

            }
        };
        CCalcEngine.InitialOneTimeOnlySetup(m_resourceProvider);
        m_calcEngine = new CCalcEngine(
                false /* Respect Order of Operations */, false /* Set to Integer Mode */, m_resourceProvider, null, m_history);
    }


    @Test
    void TestGroupDigitsPerRadix()
    {
        // Empty/Error cases
        assertTrue(m_calcEngine.GroupDigitsPerRadix("", uint.of(10)).isEmpty(), "Verify grouping empty string returns empty string.");
        assertEquals("12345678", m_calcEngine.GroupDigitsPerRadix("12345678", uint.of(9)), "Verify grouping on invalid base returns original string");

        // Octal
        assertEquals("1 234 567", m_calcEngine.GroupDigitsPerRadix("1234567", uint.of(8)), "Verify grouping in octal.");
        assertEquals("123", m_calcEngine.GroupDigitsPerRadix("123", uint.of(8)), "Verify minimum grouping in octal.");

        // Binary/Hexadecimal
        assertEquals("12 3456 7890", m_calcEngine.GroupDigitsPerRadix("1234567890", uint.of(2)), "Verify grouping in binary.");
        assertEquals("1234", m_calcEngine.GroupDigitsPerRadix("1234", uint.of(2)), "Verify minimum grouping in binary.");
        assertEquals("12 3456 7890", m_calcEngine.GroupDigitsPerRadix("1234567890", uint.of(16)), "Verify grouping in hexadecimal.");
        assertEquals("1234", m_calcEngine.GroupDigitsPerRadix("1234", uint.of(16)), "Verify minimum grouping in hexadecimal.");

        // Decimal
        assertEquals("1,234,567,890", m_calcEngine.GroupDigitsPerRadix("1234567890", uint.of(10)), "Verify grouping in base10.");
        assertEquals("1,234,567.89", m_calcEngine.GroupDigitsPerRadix("1234567.89", uint.of(10)), "Verify grouping in base10 with decimal.");
        assertEquals("1,234,567e89", m_calcEngine.GroupDigitsPerRadix("1234567e89", uint.of(10)), "Verify grouping in base10 with exponent.");
        assertEquals(
                "1,234,567.89e5", m_calcEngine.GroupDigitsPerRadix("1234567.89e5", uint.of(10)), "Verify grouping in base10 with decimal and exponent.");
        assertEquals("-123,456,789", m_calcEngine.GroupDigitsPerRadix("-123456789", uint.of(10)), "Verify grouping in base10 with negative.");
    }

    @Test void TestIsNumberInvalid()
    {
        // Binary Number Checks
        List<String> validBinStrs = List.of( "0", "1", "0011", "1100" );
        List<String> invalidBinStrs = List.of( "2", "A", "0.1" );
        for (var str : validBinStrs)
        {
            assertEquals(0, m_calcEngine.IsNumberInvalid(str, 0, 0, uint.of(2) /* Binary */));
        }
        for (var str : invalidBinStrs)
        {
            assertEquals(IDS_ERR_UNK_CH, m_calcEngine.IsNumberInvalid(str, 0, 0, uint.of(2) /* Binary */));
        }

        // Octal Number Checks
        List<String> validOctStrs = List.of( "0", "7", "01234567", "76543210" );
        List<String> invalidOctStrs = List.of( "8", "A", "0.7" );
        for (String str : validOctStrs)
        {
            assertEquals(0, m_calcEngine.IsNumberInvalid(str, 0, 0,uint.of( 8) /* Octal */));
        }
        for (String str : invalidOctStrs)
        {
            assertEquals(IDS_ERR_UNK_CH, m_calcEngine.IsNumberInvalid(str, 0, 0,uint.of( 8) /* Octal */));
        }

        // Hexadecimal Number Checks
        List<String> validHexStrs = List.of( "0", "F", "0123456789ABCDEF", "FEDCBA9876543210" );
        List<String> invalidHexStrs = List.of( "G", "abcdef", "x", "0.1" );
        for (String str : validHexStrs)
        {
            assertEquals(0, m_calcEngine.IsNumberInvalid(str, 0, 0, uint.of(16) /* HEx */));
        }
        for (String str : invalidHexStrs)
        {
            assertEquals(IDS_ERR_UNK_CH, m_calcEngine.IsNumberInvalid(str, 0, 0, uint.of(16) /* Hex */));
        }

        // Decimal Number Checks

        // Special case errors: long exponent, long mantissa
        String longExp = "1e12345";
        assertEquals(0, m_calcEngine.IsNumberInvalid(longExp, 5 /* Max exp length */, 100, uint.of(10) /* Decimal */));
        assertEquals(IDS_ERR_INPUT_OVERFLOW, m_calcEngine.IsNumberInvalid(longExp, 4 /* Max exp length */, 100, uint.of(10) /* Decimal */));
        // Mantissa length is sum of:
        //  - digits before decimal separator, minus leading zeroes
        //  - digits after decimal separator, including trailing zeroes
        // Each of these mantissa values should calculate as a length of 5
        List<String> longMantStrs = List.of( "10000", "10.000", "0000012345", "123.45", "0.00123", "0.12345", "-123.45e678" );
        for (String str : longMantStrs)
        {
            assertEquals(0, m_calcEngine.IsNumberInvalid(str, 100, 5 /* Max mantissa length */, uint.of(10) /* Decimal */));
        }
        for (String str : longMantStrs)
        {
            assertEquals(IDS_ERR_INPUT_OVERFLOW, m_calcEngine.IsNumberInvalid(str, 100, 4 /* Max mantissa length */, uint.of(10) /* Decimal */));
        }

        // Regex matching (descriptions taken from CalcUtils.cpp)
        // Use 100 for exp/mantissa length as they are tested above
        List<String> validDecStrs = List.of( // Start with an optional + or -
        "+1",
                "-1",
                "1",
                // Followed by zero or more digits
                "-",
                "",
                "1234567890",
                // Followed by an optional decimal point
                "1.0",
                "-.",
                "1.",
                // Followed by zero or more digits
                "0.0",
                "0.123456",
                // Followed by an optional exponent ('e')
                "1e",
                "1.e",
                "-e",
                // If there's an exponent, its optionally followed by + or -
                // and followed by zero or more digits
                "1e+12345",
                "1e-12345",
                "1e123",
                // All together
                "-123.456e+789"
    );
        List<String> invalidDecStrs = List.of( "x123", "123-", "1e1.2", "1-e2" );
        for (String str : validDecStrs)
        {
            assertEquals(0, m_calcEngine.IsNumberInvalid(str, 100, 100, uint.of(10) /* Dec */));
        }
        for (String str : invalidDecStrs)
        {
            assertEquals(IDS_ERR_UNK_CH, m_calcEngine.IsNumberInvalid(str, 100, 100, uint.of(10) /* Dec */), "failed for: " + str);
        }
    }

    @Test void TestDigitGroupingStringToGroupingVector()
    {
        List<Integer> groupingVector = new ArrayList<>();
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector(""), "Verify empty grouping");

        groupingVector = List.of( 1 );
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector("1"), "Verify simple grouping");

        groupingVector = List.of(3, 0);
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector("3;0"), "Verify standard grouping");

        groupingVector = List.of( 3, 0, 0 );
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector("3;0;0"), "Verify expanded non-repeating grouping");

        groupingVector = List.of(5, 3, 2, 4, 6 );
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector("5;3;2;4;6"), "Verify long grouping");

        groupingVector = List.of( 15, 15, 15, 0 );
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector("15;15;15;0"), "Verify large grouping");

        groupingVector = List.of( 4, 7, 0 );
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector("4;16;7;25;0"), "Verify we ignore oversize grouping");

        groupingVector = List.of( 3, 0 );
        String nonRepeatingGrouping = "3;0;0";
        String repeatingGrouping = nonRepeatingGrouping.substring(0, 3);
        assertEquals(groupingVector, CCalcEngine.DigitGroupingStringToGroupingVector(repeatingGrouping), "Verify we don't go past the end of wstring_view range");
    }

    @Test void TestGroupDigits()
    {
        String result = "1234567";
        assertEquals(result, m_calcEngine.GroupDigits("", List.of( 3, 0 ), "1234567", false), "Verify handling of empty delimiter.");
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of(), "1234567", false), "Verify handling of empty grouping.");

        result = "1,234,567";
        assertEquals(result, m_calcEngine.GroupDigits(",",List.of( 3, 0 ), "1234567", false), "Verify standard digit grouping.");

        result = "1 234 567";
        assertEquals(result, m_calcEngine.GroupDigits(" ",List.of( 3, 0 ), "1234567", false), "Verify delimiter change.");

        result = "1|||234|||567";
        assertEquals(result, m_calcEngine.GroupDigits("|||", List.of( 3, 0 ), "1234567", false), "Verify long delimiter.");

        result = "12,345e67";
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of( 3, 0 ), "12345e67", false), "Verify respect of exponent.");

        result = "12,345.67";
        assertEquals(result, m_calcEngine.GroupDigits(",",List.of( 3, 0 ), "12345.67", false), "Verify respect of decimal.");

        result = "1,234.56e7";
        assertEquals(result, m_calcEngine.GroupDigits(",",List.of( 3, 0 ), "1234.56e7", false), "Verify respect of exponent and decimal.");

        result = "-1,234,567";
        assertEquals(result, m_calcEngine.GroupDigits(",",List.of( 3, 0 ), "-1234567", true), "Verify negative number grouping.");

        // Test various groupings
        result = "1234567890123456";
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of( 0, 0 ), "1234567890123456", false), "Verify no grouping.");

        result = "1234567890123,456";
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of( 3 ), "1234567890123456", false), "Verify non-repeating grouping.");
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of( 3, 0, 0 ), "1234567890123456", false), "Verify expanded form non-repeating grouping.");

        result = "12,34,56,78,901,23456";
        assertEquals(
                result, m_calcEngine.GroupDigits(",", List.of( 5, 3, 2, 0 ), "1234567890123456", false), "Verify multigroup with repeating grouping.");

        result = "1234,5678,9012,3456";
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of( 4, 0 ), "1234567890123456", false), "Verify repeating non-standard grouping.");

        result = "123456,78,901,23456";
        assertEquals(result, m_calcEngine.GroupDigits(",", List.of( 5, 3, 2 ), "1234567890123456", false), "Verify multigroup non-repeating grouping.");
        assertEquals(
                result,
                m_calcEngine.GroupDigits(",", List.of( 5, 3, 2, 0, 0 ), "1234567890123456", false),
                "Verify expanded form multigroup non-repeating grouping.");
    }


}
