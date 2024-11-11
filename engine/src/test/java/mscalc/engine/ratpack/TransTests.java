package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TransTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, PRECISION);
    }


    @Test
    public void sine_cmp_tests() {
        var tester = new FunctionComparisonTester("sin", -4, 4, 0.137) {
            @Override
            double expected(double x) {
                return Math.sin(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                Trans.sinrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void cosine_cmp_tests() {
        var tester = new FunctionComparisonTester("cos", -4, 4, 0.137) {
            @Override
            double expected(double x) {
                return Math.cos(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                Trans.cosrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void tan_cmp_tests() {
        var tester = new FunctionComparisonTester("tan", -40, 40, 1.037) {
            @Override
            double expected(double x) {
                return Math.tan(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                Trans.tanrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }
}
