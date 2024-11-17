package mscalc.engine;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.UIntArray;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.NumberFormat;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static mscalc.engine.ratpack.CalcErr.CALC_E_INDEFINITE;
import static mscalc.engine.ratpack.Support.ChangeConstants;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RationalTests {
    private static final uint BASE_10 = uint.of(10);
    private static final int PRECISION = 128;

    @BeforeAll
    public static void setup() {
        ChangeConstants(BASE_10, PRECISION);
    }

    private static void assertRatEquals(Rational r, int i) {
        assertEquals(r.toString(BASE_10, NumberFormat.Float, PRECISION), Integer.toString(i));
    }

    @Test
    void TestModuloOperandsNotModified() {
        // Verify results but also check that operands are not modified
        Rational rat25 = Rational.of(25);
        Rational ratminus25 = Rational.of(-25);
        Rational rat4 = Rational.of(4);
        Rational ratminus4 = Rational.of(-4);
        Rational res = RationalMath.mod(rat25, rat4);
        assertRatEquals(res, 1);
        assertRatEquals(rat25, 25);
        assertRatEquals(rat4, 4);
        res = RationalMath.mod(rat25, ratminus4);
        assertRatEquals(res, -3);
        assertRatEquals(rat25, 25);
        assertRatEquals(ratminus4, -4);
        res = RationalMath.mod(ratminus25, ratminus4);
        assertRatEquals(res, -1);
        assertRatEquals(ratminus25, -25);
        assertRatEquals(ratminus4, -4);
        res = RationalMath.mod(ratminus25, rat4);
        assertRatEquals(res, 3);
        assertRatEquals(ratminus25, -25);
        assertRatEquals(rat4, 4);
    }

    @Test
    void TestModuloInteger() {
        // Check with integers
        var res = RationalMath.mod(Rational.of(426), Rational.of(56478));
        assertRatEquals(res, 426);
        res = RationalMath.mod(Rational.of(56478), Rational.of(426));
        assertRatEquals(res, 246);
        res = RationalMath.mod(Rational.of(-643), Rational.of(8756));
        assertRatEquals(res, 8113);
        res = RationalMath.mod(Rational.of(643), Rational.of(-8756));
        assertRatEquals(res, -8113);
        res = RationalMath.mod(Rational.of(-643), Rational.of(-8756));
        assertRatEquals(res, -643);
        res = RationalMath.mod(Rational.of(1000), Rational.of(250));
        assertRatEquals(res, 0);
        res = RationalMath.mod(Rational.of(1000), Rational.of(-250));
        assertRatEquals(res, 0);
    }

    @Test
    void TestModuloZero() {
        // Test with Zero
        var res = RationalMath.mod(Rational.of(343654332), Rational.of(0));
        assertRatEquals(res, 343654332);
        res = RationalMath.mod(Rational.of(0), Rational.of(8756));
        assertRatEquals(res, 0);
        res = RationalMath.mod(Rational.of(0), Rational.of(-242));
        assertRatEquals(res, 0);
        res = RationalMath.mod(Rational.of(0), Rational.of(0));
        assertRatEquals(res, 0);
        res = RationalMath.mod(
                new Rational(
                        new Number(1, 0, UIntArray.ofValues(23242)),
                        new Number(1, 0, UIntArray.ofValues(2))),
                new Rational(
                        new Number(1, 0, UIntArray.ofValues(0)),
                        new Number(1, 0, UIntArray.ofValues(23))));
        assertRatEquals(res, 11621);
    }

    @Test
    void TestModuloRational() {
        // Test with rational numbers
        var res = RationalMath.mod(new Rational(Number(1, 0, new int[]{250}), Number(1, 0, new int[]{100})), Rational.of(89));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "2.5");
        res = RationalMath.mod(new Rational(Number(1, 0, new int[]{3330}), Number(1, 0, new int[]{1332})), Rational.of(1));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.5");
        res = RationalMath.mod(new Rational(Number(1, 0, new int[]{12250}), Number(1, 0, new int[]{100})), Rational.of(10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "2.5");
        res = RationalMath.mod(new Rational(Number(-1, 0, new int[]{12250}), Number(1, 0, new int[]{100})), Rational.of(10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "7.5");
        res = RationalMath.mod(new Rational(Number(-1, 0, new int[]{12250}), Number(1, 0, new int[]{100})), Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-2.5");
        res = RationalMath.mod(new Rational(Number(1, 0, new int[]{12250}), Number(1, 0, new int[]{100})), Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-7.5");
        res = RationalMath.mod(new Rational(Number(1, 0, new int[]{1000}), Number(1, 0, new int[]{3})), Rational.of(1));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.33333333");
        res = RationalMath.mod(new Rational(Number(1, 0, new int[]{1000}), Number(1, 0, new int[]{3})), Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-6.6666667");
        res = RationalMath.mod(Rational.of(834345), new Rational(Number(1, 0, new int[]{103}), Number(1, 0, new int[]{100})));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.71");
        res = RationalMath.mod(Rational.of(834345), new Rational(Number(-1, 0, new int[]{103}), Number(1, 0, new int[]{100})));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-0.32");
    }

    private static Number Number(int s, int e, int[] arr) {
        return new Number(s, e, UIntArray.ofValues(arr));
    }

    @Test
    void TestRemainderOperandsNotModified() {
        // Verify results but also check that operands are not modified
        Rational rat25 = Rational.of(25);
        Rational ratminus25 = Rational.of(-25);
        Rational rat4 = Rational.of(4);
        Rational ratminus4 = Rational.of(-4);
        Rational res = rat25.modulo(rat4);
        assertRatEquals(res, 1);
        assertRatEquals(rat25, 25);
        assertRatEquals(rat4, 4);
        res = rat25.modulo(ratminus4);
        assertRatEquals(res, 1);
        assertRatEquals(rat25, 25);
        assertRatEquals(ratminus4, -4);
        res = ratminus25.modulo(ratminus4);
        assertRatEquals(res, -1);
        assertRatEquals(ratminus25, -25);
        assertRatEquals(ratminus4, -4);
        res = ratminus25.modulo(rat4);
        assertRatEquals(res, -1);
        assertRatEquals(ratminus25, -25);
        assertRatEquals(rat4, 4);
    }

    @Test
    void TestRemainderInteger() {
        // Check with integers
        var res = Rational.of(426).modulo(Rational.of(56478));
        assertRatEquals(res, 426);
        res = Rational.of(56478).modulo(Rational.of(426));
        assertRatEquals(res, 246);
        res = Rational.of(-643).modulo(Rational.of(8756));
        assertRatEquals(res, -643);
        res = Rational.of(643).modulo(Rational.of(-8756));
        assertRatEquals(res, 643);
        res = Rational.of(-643).modulo(Rational.of(-8756));
        assertRatEquals(res, -643);
        res = Rational.of(-124).modulo(Rational.of(-124));
        assertRatEquals(res, 0);
        res = Rational.of(24).modulo(Rational.of(24));
        assertRatEquals(res, 0);
    }

    @Test
    void TestRemainderZero() {
        // Test with Zero
        var res = Rational.of(0).modulo(Rational.of(3654));
        assertRatEquals(res, 0);
        res = Rational.of(0).modulo(Rational.of(-242));
        assertRatEquals(res, 0);
        for (var number : new int[]{343654332, 0, -23423}) {
            try {
                res = new Rational(number).modulo(Rational.of(0));
                fail();
            } catch (ErrorCodeException t) {
                if (t.errorCode() != CALC_E_INDEFINITE) {
                    fail();
                }
            } catch (Exception e) {
                fail();
            }

            try {
                res = new Rational(Number(1, number, new int[]{0}), Number(1, 0, new int[]{2})).modulo(
                        new Rational(Number(1, 0, new int[]{0}), Number(1, 0, new int[]{23})));
                fail();
            } catch (ErrorCodeException t) {
                if (t.errorCode() != CALC_E_INDEFINITE) {
                    fail();
                }
            } catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    void TestRemainderRational() {
        // Test with rational numbers
        var res = new Rational(Number(1, 0, new int[]{250}), Number(1, 0, new int[]{100})).modulo(Rational.of(89));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "2.5");
        res = new Rational(Number(1, 0, new int[]{3330}), Number(1, 0, new int[]{1332})).modulo(Rational.of(1));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.5");
        res = new Rational(Number(1, 0, new int[]{12250}), Number(1, 0, new int[]{100})).modulo(Rational.of(10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "2.5");
        res = new Rational(Number(-1, 0, new int[]{12250}), Number(1, 0, new int[]{100})).modulo(Rational.of(10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-2.5");
        res = new Rational(Number(-1, 0, new int[]{12250}), Number(1, 0, new int[]{100})).modulo(Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-2.5");
        res = new Rational(Number(1, 0, new int[]{12250}), Number(1, 0, new int[]{100})).modulo(Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "2.5");
        res = new Rational(Number(1, 0, new int[]{1000}), Number(1, 0, new int[]{3})).modulo(Rational.of(1));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.33333333");
        res = new Rational(Number(1, 0, new int[]{1000}), Number(1, 0, new int[]{3})).modulo(Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "3.3333333");
        res = new Rational(Number(-1, 0, new int[]{1000}), Number(1, 0, new int[]{3})).modulo(Rational.of(-10));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-3.3333333");
        res = Rational.of(834345).modulo(new Rational(Number(1, 0, new int[]{103}), Number(1, 0, new int[]{100})));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.71");
        res = Rational.of(834345).modulo(new Rational(Number(-1, 0, new int[]{103}), Number(1, 0, new int[]{100})));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "0.71");
        res = Rational.of(-834345).modulo(new Rational(Number(1, 0, new int[]{103}), Number(1, 0, new int[]{100})));
        assertEquals(res.toString(BASE_10, NumberFormat.Float, 8), "-0.71");
    }
}
