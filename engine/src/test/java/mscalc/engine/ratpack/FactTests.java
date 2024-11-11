package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, PRECISION);
    }

    @Test
    public void factorial_smoke_test() {
        for (int i = 0; i < 20; i++) {
            Ptr<RatPack.RAT> xRat = new Ptr<>(
                    Conv.StringToRat(false, Integer.toString(i), false, "0", RADIX_10, PRECISION)
            );
            Fact.factrat(xRat, RADIX_10, PRECISION);

            String actual = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);
            String expected = Long.toString(factorial(i));

            assertEquals(expected, actual, "failed for i = " + i);
        }
    }

    private long factorial(int n) {
        long f = 1;

        for (int i = 2; i <= n; i++) {
            f *= i;
        }

        return f;
    }
}
