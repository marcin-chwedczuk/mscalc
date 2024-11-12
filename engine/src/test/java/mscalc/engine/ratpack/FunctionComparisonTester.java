package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.RAT;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class FunctionComparisonTester {
    // Number of digits used in assertions
    private static final int CMP_TEST_PRECISION = 10;

    private static final int PRECISION = 20;
    private static final uint BASE_10 = uint.of(10);

    private final String sutName;
    private final double from;
    private final double to;
    private final double step;

    public FunctionComparisonTester(String sutName, double from, double to, double step) {
        this.sutName = sutName;

        this.from = from;
        this.to = to;
        this.step = step;

        if (from + step >= to) {
            throw new IllegalArgumentException("empty range");
        }

        if (step < 0.001)
            throw new IllegalArgumentException("step is too small");
    }

    abstract double expected(double x);
    abstract void actual(Ptr<RAT> x);

    public void runTests() {
        int nTests = 0;
        for (double x = from; x < to; x += step) {
            try {
                System.out.println("Testing " + sutName + " for " + x);
                runTest(x);
            } catch (ErrorCodeException e) {
                fail("Unexpected exception: " + e + ", for value x = " + x);
            }
            nTests++;
        }
        System.out.println("Function: " + sutName + ": " + nTests + " comparison tests ran.");
    }

    private void runTest(double x) {
        double expected = expected(x);

        boolean negative = x < 0;
        String mantissa = new BigDecimal(Math.abs(x)).toPlainString();
        RAT rx = Conv.StringToRat(negative, mantissa, false, "0", BASE_10, PRECISION);

        Ptr<RAT> actual = new Ptr<>(rx);
        actual(actual);
        String actualString = Conv.RatToString(actual, RatPack.NumberFormat.Float, BASE_10, PRECISION);

        assertEquals(round(Double.toString(expected)),
                     round(actualString),
                     String.format("Failed for value x: %f (full actual: %s)", x, actualString));
    }

    private static String round(String value) {
        double rawDouble = Double.parseDouble(value);
        BigDecimal rounded = new BigDecimal(rawDouble).round(new MathContext(CMP_TEST_PRECISION));
        String result = rounded.toPlainString();

        // Fix case: 17 vs 17.0000
        // Remove trailing zeros, with maybe decimal point
        return result.replaceFirst("[.]?0+$", "");
    }
}
