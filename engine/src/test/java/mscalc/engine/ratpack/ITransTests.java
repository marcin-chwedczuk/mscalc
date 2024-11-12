package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ITransTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, PRECISION);
    }

    @Test
    public void asin_cmp_tests() {
        var tester = new FunctionComparisonTester("asin", -1, 1, 0.137) {
            @Override
            double expected(double x) {
                return Math.asin(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                ITrans.asinrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void acos_cmp_tests() {
        var tester = new FunctionComparisonTester("acos", -1, 1, 0.057) {
            @Override
            double expected(double x) {
                return Math.acos(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                ITrans.acosrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }
}
