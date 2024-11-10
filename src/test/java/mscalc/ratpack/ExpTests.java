package mscalc.ratpack;

import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.RAT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, PRECISION);
    }

    @Test
    public void exp_works() {
        String[] data = new String[] {
                // x, e^x
                "0", "1",
                // Data from wolfram-alpha
                "1", "2.7182818284590452354",
                "2", "7.3890560989306502272",
                "24", "26489122129.843472294",
                "147", "6.9388714177584033016e+63",

                // Real x
                "3.141592", "23.140677508263698368",
                // ln(2)
                "0.69314718055994530941", "2",
                // ln(100)
                "4.6051701859880913680359829093687284152022029772575459520666558019", "99.999999999999999996",

                // Negative x
                "-1", "0.3678794411714423216",
                // ln(1/10)
                "-2.302585092994045684017991454684364207601101488628772976033327900", "0.1"
        };

        for (int i = 0; i <= data.length - 2; i += 2) {
            String x = data[i];
            String expected = data[i+1];

            Ptr<RAT> xRat = new Ptr<>(
                    Conv.StringToRat(false, x, false, "0", RADIX_10, PRECISION)
            );
            Exp.exprat(xRat, RADIX_10, PRECISION);

            String actual = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

            // System.out.printf("exp(%s) = %s%n", x, expected);
            assertEquals(expected, actual);
        }
    }

}
