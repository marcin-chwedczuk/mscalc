package mscalc.ratpack;

import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import org.junit.jupiter.api.Test;

import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.Conv.numtoi32;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConversionTests {
    @Test
    public void number_int_roundtrip_conversion() {
        int[] data = { -1993, -2, -1, 0, 1, 2, 393, 1818383 };
        int[] radix = { 2, 10, 16 };

        for (int r : radix) {
            for (int d : data) {
                NUMBER number = i32tonum(d, uint.of(r));
                int roundtripped = numtoi32(number, uint.of(r));

                assertEquals(d, roundtripped, "failed for d= " + d + ", r = " + r);
            }
        }
    }
}
