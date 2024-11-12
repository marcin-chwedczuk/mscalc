package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TransHTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, PRECISION);
    }


    @Test
    public void sinh_cmp_tests() {
        var tester = new FunctionComparisonTester("sinh", -17, 17, 1.137) {
            @Override
            double expected(double x) {
                return Math.sinh(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                TransH.sinhrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void cosh_cmp_tests() {
        var tester = new FunctionComparisonTester("cosh", -17, 17, 1.137) {
            @Override
            double expected(double x) {
                return Math.cosh(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                TransH.coshrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void tan_cmp_tests() {
        var tester = new FunctionComparisonTester("tanh", -4, 4, 0.337) {
            @Override
            double expected(double x) {
                return Math.tanh(x);
            }

            @Override
            void actual(Ptr<RatPack.RAT> x) {
                TransH.tanhrat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }
}
