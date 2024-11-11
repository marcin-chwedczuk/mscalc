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
    public void smoke_test() {
        for (int i = 0; i < 10; i++) {
            Ptr<RatPack.RAT> xRat = new Ptr<>(
                    Conv.StringToRat(false, Integer.toString(i), false, "0", RADIX_10, PRECISION)
            );
            Fact.factrat(xRat, RADIX_10, PRECISION);

            String actual = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

            System.out.printf("%d! = %s%n", i, actual);
            // assertEquals(expected, actual);
        }
    }
}
