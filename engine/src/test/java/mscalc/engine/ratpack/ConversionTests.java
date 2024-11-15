package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.cpp.ulong;
import mscalc.engine.ratpack.RatPack.NUMBER;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static mscalc.engine.ratpack.Conv.*;
import static mscalc.engine.ratpack.RatPack.BASEX;
import static mscalc.engine.ratpack.RatPack.DUPNUM;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConversionTests {
    private static final uint RADIX_2 = uint.of(2);
    private static final uint RADIX_10 = uint.of(10);
    private static final uint RADIX_16 = uint.of(16);

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, 20);
    }

    @Test
    public void number_int_roundtrip_conversion() {
        int[] data = { -1993, -2, -1, 0, 1, 2, 393, 1818383 };
        int[] radix = { 2, 10, 16 };

        for (int r : radix) {
            for (int d : data) {
                NUMBER number = Conv.i32tonum(d, uint.of(r));
                int roundtripped = Conv.numtoi32(number, uint.of(r));

                assertEquals(d, roundtripped, "failed for d= " + d + ", r = " + r);
            }
        }
    }

    @Test
    public void NumberToString_works_radix10_float() {
        Ptr<NUMBER> n1993 = new Ptr<>(Conv.i32tonum(1993, RADIX_10));
        Ptr<NUMBER> nm1993 = new Ptr<>(Conv.i32tonum(-1993, RADIX_10));
        Ptr<NUMBER> n3899474 = new Ptr<>(Conv.i32tonum(3899474, RADIX_10));
        Ptr<NUMBER> one = new Ptr<>(Conv.i32tonum(1, RADIX_10));
        Ptr<NUMBER> zero = new Ptr<>(Conv.i32tonum(0, RADIX_10));

        String result = Conv.NumberToString(n1993, RatPack.NumberFormat.Float, RADIX_10, 10);
        assertEquals("1993", result);

        result = Conv.NumberToString(nm1993, RatPack.NumberFormat.Float, RADIX_10, 10);
        assertEquals("-1993", result);

        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Float, RADIX_10, 10);
        assertEquals("3899474", result);

        // Lower precision aka number of significant digits
        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Float, RADIX_10, 2);
        assertEquals("3.9e+6", result);

        result = Conv.NumberToString(one, RatPack.NumberFormat.Float, RADIX_10, 10);
        assertEquals("1", result);

        result = Conv.NumberToString(zero, RatPack.NumberFormat.Float, RADIX_10, 10);
        assertEquals("0", result);
    }

    @Test
    public void NumberToString_works_radix2_float() {
        Ptr<NUMBER> n1993 = new Ptr<>(Conv.i32tonum(1993, RADIX_2));
        Ptr<NUMBER> nm1993 = new Ptr<>(Conv.i32tonum(-1993, RADIX_2));
        Ptr<NUMBER> n3899474 = new Ptr<>(Conv.i32tonum(3899474, RADIX_2));
        Ptr<NUMBER> one = new Ptr<>(Conv.i32tonum(1, RADIX_2));
        Ptr<NUMBER> zero = new Ptr<>(Conv.i32tonum(0, RADIX_2));

        String result = Conv.NumberToString(n1993, RatPack.NumberFormat.Float, RADIX_2, 20);
        assertEquals("11111001001", result);

        result = Conv.NumberToString(nm1993, RatPack.NumberFormat.Float, RADIX_2, 20);
        assertEquals("-11111001001", result);

        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Float, RADIX_2, 40);
        assertEquals("1110111000000001010010", result);

        // Lower precision aka number of significant digits
        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Float, RADIX_2, 20);
        assertEquals("1.1101110000000010101^+10101", result); // TODO: Check with win calc.exe

        result = Conv.NumberToString(one, RatPack.NumberFormat.Float, RADIX_2, 20);
        assertEquals("1", result);

        result = Conv.NumberToString(zero, RatPack.NumberFormat.Float, RADIX_2, 20);
        assertEquals("0", result);
    }

    @Test
    public void NumberToString_works_radix16_float() {
        Ptr<NUMBER> n1993 = new Ptr<>(Conv.i32tonum(1993, RADIX_16));
        Ptr<NUMBER> nm1993 = new Ptr<>(Conv.i32tonum(-1993, RADIX_16));
        Ptr<NUMBER> n3899474 = new Ptr<>(Conv.i32tonum(3899474, RADIX_16));
        Ptr<NUMBER> one = new Ptr<>(Conv.i32tonum(1, RADIX_16));
        Ptr<NUMBER> zero = new Ptr<>(Conv.i32tonum(0, RADIX_16));

        String result = Conv.NumberToString(n1993, RatPack.NumberFormat.Float, RADIX_16, 10);
        assertEquals("7C9", result);

        result = Conv.NumberToString(nm1993, RatPack.NumberFormat.Float, RADIX_16, 10);
        assertEquals("-7C9", result);

        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Float, RADIX_16, 10);
        assertEquals("3B8052", result);

        // Lower precision aka number of significant digits
        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Float, RADIX_16, 2);
        assertEquals("3.C^+5", result);

        result = Conv.NumberToString(one, RatPack.NumberFormat.Float, RADIX_16, 10);
        assertEquals("1", result);

        result = Conv.NumberToString(zero, RatPack.NumberFormat.Float, RADIX_16, 10);
        assertEquals("0", result);
    }

    @Test
    public void NumberToString_works_radix10_scientific() {
        Ptr<NUMBER> n1993 = new Ptr<>(Conv.i32tonum(1993, RADIX_10));
        Ptr<NUMBER> nm1993 = new Ptr<>(Conv.i32tonum(-1993, RADIX_10));
        Ptr<NUMBER> n3899474 = new Ptr<>(Conv.i32tonum(3899474, RADIX_10));
        Ptr<NUMBER> one = new Ptr<>(Conv.i32tonum(1, RADIX_10));
        Ptr<NUMBER> zero = new Ptr<>(Conv.i32tonum(0, RADIX_10));

        String result = Conv.NumberToString(n1993, RatPack.NumberFormat.Scientific, RADIX_10, 10);
        assertEquals("1.993e+3", result);

        result = Conv.NumberToString(nm1993, RatPack.NumberFormat.Scientific, RADIX_10, 10);
        assertEquals("-1.993e+3", result);

        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Scientific, RADIX_10, 10);
        assertEquals("3.899474e+6", result);

        // Lower precision aka number of significant digits
        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Scientific, RADIX_10, 2);
        assertEquals("3.9e+6", result);

        result = Conv.NumberToString(one, RatPack.NumberFormat.Scientific, RADIX_10, 10);
        assertEquals("1.e+0", result);

        result = Conv.NumberToString(zero, RatPack.NumberFormat.Scientific, RADIX_10, 10);
        assertEquals("0.e+0", result);
    }

    @Test
    public void NumberToString_works_radix10_engineering() {
        /*
        The only difference between scientific notation and engineering notation is
        that for engineering notation the exponent is always a multiple of three.
         */
        Ptr<NUMBER> n1993 = new Ptr<>(Conv.i32tonum(1993, RADIX_10));
        Ptr<NUMBER> nm1993 = new Ptr<>(Conv.i32tonum(-1993, RADIX_10));
        Ptr<NUMBER> n3899474 = new Ptr<>(Conv.i32tonum(3899474, RADIX_10));
        Ptr<NUMBER> one = new Ptr<>(Conv.i32tonum(1, RADIX_10));
        Ptr<NUMBER> zero = new Ptr<>(Conv.i32tonum(0, RADIX_10));
        Ptr<NUMBER> n19932 = new Ptr<>(Conv.i32tonum(19932, RADIX_10));

        String result = Conv.NumberToString(n1993, RatPack.NumberFormat.Engineering, RADIX_10, 10);
        assertEquals("1.993e+3", result);

        result = Conv.NumberToString(nm1993, RatPack.NumberFormat.Engineering, RADIX_10, 10);
        assertEquals("-1.993e+3", result);

        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Engineering, RADIX_10, 10);
        assertEquals("3.899474e+6", result);

        // Lower precision aka number of significant digits
        result = Conv.NumberToString(n3899474, RatPack.NumberFormat.Engineering, RADIX_10, 2);
        assertEquals("3.9e+6", result);

        result = Conv.NumberToString(one, RatPack.NumberFormat.Engineering, RADIX_10, 10);
        assertEquals("1.e+0", result);

        result = Conv.NumberToString(zero, RatPack.NumberFormat.Engineering, RADIX_10, 10);
        assertEquals("0.e+0", result);

        result = Conv.NumberToString(n19932, RatPack.NumberFormat.Engineering, RADIX_10, 10);
        assertEquals("19.932e+3", result);
    }

    @Test
    public void StringToNumber_radix10() {
        assertEquals(0, string2numberSUT("0", RADIX_10));
        assertEquals(0, string2numberSUT("00", RADIX_10));
        assertEquals(0, string2numberSUT("000", RADIX_10));

        assertEquals(1, string2numberSUT("1", RADIX_10));
        assertEquals(1, string2numberSUT("01", RADIX_10));
        assertEquals(1, string2numberSUT("001", RADIX_10));

        assertEquals(32927, string2numberSUT("32927", RADIX_10));
        assertEquals(-32927, string2numberSUT("-32927", RADIX_10));
        assertEquals(1000, string2numberSUT("1000", RADIX_10));

        assertEquals(1000, string2numberSUT("1e+3", RADIX_10));
        assertEquals(1230, string2numberSUT("1.23e+3", RADIX_10));
        assertEquals(-1230, string2numberSUT("-1.23e+3", RADIX_10));
    }

    private int string2numberSUT(String str, uint radix) {
        NUMBER number = Conv.StringToNumber(str, radix, 20);
        return Conv.numtoi32(number, radix);
    }

    @Test
    public void StringNumber_roundtrips() {
        int[] numbers = { -7372, 324, 83, 0, 2, 54, 8282, 47483883 };
        int[] radix = { 2, 10, 16 };

        for (int d : numbers) {
            for (int r : radix) {
                NUMBER number = Conv.StringToNumber(Integer.toString(d, r), uint.of(r), 40);
                String str = Conv.NumberToString(new Ptr<>(number), RatPack.NumberFormat.Float, uint.of(r), 40);
                int rd = Integer.parseInt(str, r);
                assertEquals(d, rd, "failed for d = " + d + ", r = " + r);
            }
        }
    }

    @Test
    public void gcd_works() {
        // GCD expects args to be in BASEX format

        NUMBER zero = Conv.i32tonum(0, BASEX);
        NUMBER one = Conv.i32tonum(1, BASEX);
        NUMBER n5 = Conv.i32tonum(5, BASEX);
        NUMBER n15 = Conv.i32tonum(15, BASEX);
        NUMBER n22 = Conv.i32tonum(22, BASEX);
        NUMBER n25 = Conv.i32tonum(25, BASEX);

        assertEquals(1, gcdSUT(one, zero));

        assertEquals(5, gcdSUT(n15, n25));
        assertEquals(5, gcdSUT(n25, n15));

        assertEquals(15, gcdSUT(n15, n15));

        assertEquals(1, gcdSUT(n22, n25));
        assertEquals(1, gcdSUT(n25, n22));

        assertEquals(5, gcdSUT(n5, n25));
        assertEquals(5, gcdSUT(n25, n5));
    }

    private int gcdSUT(NUMBER a, NUMBER b) {
        NUMBER aCopy = DUPNUM(a);
        NUMBER bCopy = DUPNUM(b);

        NUMBER result = Conv.gcd(a, b);

        // Assert arguments not changed
        Assertions.assertEquals(Conv.numtoi32(aCopy, BASEX), Conv.numtoi32(a, BASEX));
        Assertions.assertEquals(Conv.numtoi32(bCopy, BASEX), Conv.numtoi32(b, BASEX));

        return Conv.numtoi32(result, BASEX);
    }

    @Test
    public void numpowi32_works() {
        Ptr<NUMBER> p2 = new Ptr<>(Conv.i32tonum(2, RADIX_10));
        Conv.numpowi32(p2, 6, RADIX_10, 10);

        int result = Conv.numtoi32(p2.deref(), RADIX_10);
        assertEquals(64, result);
    }

    @Test
    public void rattoUi64_works() {
        // Max UInt value
        RatPack.RAT maxUInt = StringToRat(false, "4294967295", false, "0", RADIX_10, 10);
        ulong ul = rattoUi64(maxUInt, RADIX_10, 10);
        assertEquals("4294967295", Long.toUnsignedString(ul.raw()));

        // Max ULong value
        maxUInt = StringToRat(false, "18446744073709551615", false, "0", RADIX_10, 10);
        ul = rattoUi64(maxUInt, RADIX_10, 10);
        assertEquals("18446744073709551615", Long.toUnsignedString(ul.raw()));
    }
}
