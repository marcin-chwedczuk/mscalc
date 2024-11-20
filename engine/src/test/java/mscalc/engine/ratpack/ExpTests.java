package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.RAT;
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

            assertEquals(expected, actual);
        }
    }

    @Test
    public void exp_cmp_test() {
        var tester = new FunctionComparisonTester("exp", -4.0, 4.0, 0.17) {
            @Override
            double expected(double x) {
                return Math.exp(x);
            }

            @Override
            void actual(Ptr<RAT> x) {
                Exp.exprat(x, RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void ln_works() {
        String[] data = new String[] {
                // x,
                "1", "0",
                // Data from wolfram-alpha
                "2.7182818284590452353602874713526624977572470936999595749669676277", "1",
                "7.3890560989306502272", "2",
                "26489122129.843472294", "24",
                "6.9388714177584033016e+63", "147",

                // Real x
                "23.140677508263698368", "3.141592",
                // ln(2)
                "2","0.69314718055994530942",
                // ln(100)
                "100", "4.605170185988091368",

                // Negative x
                "0.3678794411714423215955237701614608674458111310317678345078368016", "-1",
                // ln(1/10)
                "0.1", "-2.302585092994045684"
        };

        for (int i = 0; i <= data.length - 2; i += 2) {
            String x = data[i];
            String expected = data[i+1];

            Ptr<RAT> xRat = new Ptr<>(
                    Conv.StringToRat(false, x, false, "0", RADIX_10, PRECISION)
            );
            Exp.lograt(xRat, PRECISION);

            String actual = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

            assertEquals(expected, actual);
        }
    }

    @Test
    public void ln_cmp_test() {
        var tester = new FunctionComparisonTester("ln", 0.00001, 4.0, 0.037) {
            @Override
            double expected(double x) {
                return Math.log(x);
            }

            @Override
            void actual(Ptr<RAT> x) {
                Exp.lograt(x, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void log10_works() {
        // log10 is based on ln, no extensive testing needed
        String[] data = new String[] {
                // x,
                "1", "0",
                "10", "1",
                "100", "2",
                "0.1", "-1",

                "77", "1.8864907251724818715"
        };

        for (int i = 0; i <= data.length - 2; i += 2) {
            String x = data[i];
            String expected = data[i+1];

            Ptr<RAT> xRat = new Ptr<>(
                    Conv.StringToRat(false, x, false, "0", RADIX_10, PRECISION)
            );
            Exp.log10rat(xRat, PRECISION);

            String actual = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

            assertEquals(expected, actual);
        }
    }

    @Test
    public void log10_cmp_test() {
        var tester = new FunctionComparisonTester("log10", 0.00001, 4.0, 0.037) {
            @Override
            double expected(double x) {
                return Math.log10(x);
            }

            @Override
            void actual(Ptr<RAT> x) {
                Exp.log10rat(x, PRECISION);
            }
        };

        tester.runTests();
    }

    @Test
    public void power_tests() {
        String[] data = new String[] {
                // x, y, x^y

                // Integral powers
                "8", "0", "1",
                "7", "1", "7",

                "2", "32", "4294967296",
                "7", "13", "96889010407",

                // Real x
                "0.123", "9", "6.443858614676334363e-9",
                "1.3", "7", "6.2748517",

                // Real y
                "8", "3.7", "2194.9920512743284003",
                "9", "0.5", "3",

                // Both real
                "3.333", "4.4444", "210.71199939326800129"
        };

        for (int i = 0; i <= data.length - 3; i += 3) {
            String x = data[i];
            String y = data[i+1];
            String expected = data[i+2];

            Ptr<RAT> xRat = new Ptr<>(
                    Conv.StringToRat(false, x, false, "0", RADIX_10, PRECISION)
            );
            RAT yRat = Conv.StringToRat(false, y, false, "0", RADIX_10, PRECISION);
            Exp.powrat(xRat, yRat, RADIX_10, PRECISION);

            String actual = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

            assertEquals(expected, actual);
        }
    }

    @Test
    public void pow_cmp_test() {
        // TODO: Add tester for 2 parameters e.g. x and y
        var tester = new FunctionComparisonTester("pow", 0.1, 4.0, 0.137) {
            @Override
            double expected(double x) {
                return Math.pow(x, x);
            }

            @Override
            void actual(Ptr<RAT> x) {
                Exp.powrat(x, x.deref().clone(), RADIX_10, PRECISION);
            }
        };

        tester.runTests();
    }
}
