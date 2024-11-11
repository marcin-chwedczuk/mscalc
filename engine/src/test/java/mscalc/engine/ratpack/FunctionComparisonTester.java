package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.RAT;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class FunctionComparisonTester {
    private static final int PRECISSION = 20;
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
        RAT rx = Conv.StringToRat(negative, mantissa, false, "0", BASE_10, PRECISSION);

        Ptr<RAT> actual = new Ptr<>(rx);
        actual(actual);
        String actualString = Conv.RatToString(actual, RatPack.NumberFormat.Float, BASE_10, PRECISSION);

        assertEquals(expected,
                    Double.parseDouble(actualString),
                    String.format("Failed for value x: %f", x));
    }
}
