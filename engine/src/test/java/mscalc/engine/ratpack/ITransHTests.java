package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ITransHTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, PRECISION);
    }

    // There are no inverse hyperbolic functions in java Math class.
    // We test that the round-trip was successful.

    @Test
    public void asinh_cmp_tests() {
        var tester = new FunctionComparisonTester("asinh", -10, 10, 0.537) {
            @Override
            double expected(double x) {
                return x;
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                TransH.sinhrat(x, RADIX_10, PRECISION);
                ITransH.asinhrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void acosh_cmp_tests() {
        var tester = new FunctionComparisonTester("acosh", 0, 13, 0.157) {
            @Override
            double expected(double x) {
                return x;
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                TransH.coshrat(x, RADIX_10, PRECISION);
                ITransH.acoshrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void atanh_cmp_tests() {
        var tester = new FunctionComparisonTester("atanh", -17, 17, 0.157) {
            @Override
            double expected(double x) {
                return x;
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                TransH.tanhrat(x, RADIX_10, PRECISION);
                ITransH.atanhrat(x, PRECISION);
            }
        };

        tester.runTests();
    }
}
