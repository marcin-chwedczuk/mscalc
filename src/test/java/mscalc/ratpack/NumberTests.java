package mscalc.ratpack;

import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import org.junit.jupiter.api.Test;

import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.Conv.numtoi32;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberTests {
    private static final uint RADIX_10 = uint.of(10);

    @Test
    public void addition_two_numbers() {
        NUMBER n123 = i32tonum(123, RADIX_10);
        NUMBER n77 = i32tonum(77, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 123 + 77 = 200
        result.set(n123);
        Num.addnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(200, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(77, numtoi32(n77, RADIX_10));
        assertEquals(123, numtoi32(n123, RADIX_10));

        // 77 + 123 = 200
        result.set(n77);
        Num.addnum(result, n123, RADIX_10);
        // Check addition
        assertEquals(200, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(77, numtoi32(n77, RADIX_10));
        assertEquals(123, numtoi32(n123, RADIX_10));
    }

    @Test
    public void addition_with_zero() {
        NUMBER n77 = i32tonum(77, RADIX_10);
        NUMBER n0 = i32tonum(0, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 0 + 77 = 77
        result.set(n0);
        Num.addnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(77, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(0, numtoi32(n0, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));

        // 77 + 0 = 77
        result.set(n77);
        Num.addnum(result, n0, RADIX_10);
        // Check addition
        assertEquals(77, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(77, numtoi32(n77, RADIX_10));
        assertEquals(0, numtoi32(n0, RADIX_10));
    }

    @Test
    public void addition_with_minus_one() {
        NUMBER n77 = i32tonum(77, RADIX_10);
        NUMBER nm1 = i32tonum(-1, RADIX_10);
        NUMBER n76 = i32tonum(76, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // (-1) + 77 = 76
        result.set(nm1);
        Num.addnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(76, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(-1, numtoi32(nm1, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));

        // 77 + (-1) = 76
        result.set(n77);
        Num.addnum(result, nm1, RADIX_10);
        // Check addition
        assertEquals(76, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(77, numtoi32(n77, RADIX_10));
        assertEquals(-1, numtoi32(nm1, RADIX_10));
    }

    @Test
    public void addition_with_carry_propagation() {
        NUMBER n299999 = i32tonum(299999, RADIX_10);
        NUMBER n2 = i32tonum(2, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 299999 + 2
        result.set(n299999);
        Num.addnum(result, n2, RADIX_10);
        // Check addition
        assertEquals(300001, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(299999, numtoi32(n299999, RADIX_10));
        assertEquals(2, numtoi32(n2, RADIX_10));

        // 2 + 299999
        result.set(n2);
        Num.addnum(result, n299999, RADIX_10);
        // Check addition
        assertEquals(300001, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(299999, numtoi32(n299999, RADIX_10));
        assertEquals(2, numtoi32(n2, RADIX_10));
    }

    @Test
    public void multiplication_two_numbers() {
        NUMBER n77 = i32tonum(77, RADIX_10);
        NUMBER n111 = i32tonum(111, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * 111 = 8547
        result.set(n77);
        Num.mulnum(result, n111, RADIX_10);
        // Check addition
        assertEquals(8547, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(111, numtoi32(n111, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));

        // 111 * 77 = 8547
        result.set(n111);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(8547, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(111, numtoi32(n111, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_with_zero() {
        NUMBER n77 = i32tonum(77, RADIX_10);
        NUMBER n0 = i32tonum(0, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * 0 = 0
        result.set(n77);
        Num.mulnum(result, n0, RADIX_10);
        // Check addition
        assertEquals(0, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(0, numtoi32(n0, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));

        // 0 * 77 = 0
        result.set(n0);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(0, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(0, numtoi32(n0, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_with_one() {
        NUMBER n77 = i32tonum(77, RADIX_10);
        NUMBER n1 = i32tonum(1, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * 1 = 77
        result.set(n77);
        Num.mulnum(result, n1, RADIX_10);
        // Check addition
        assertEquals(77, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(1, numtoi32(n1, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));

        // 1 * 77 = 77
        result.set(n1);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(77, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(1, numtoi32(n1, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_with_negative() {
        NUMBER n77 = i32tonum(77, RADIX_10);
        NUMBER nm1 = i32tonum(-1, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * -1 = -77
        result.set(n77);
        Num.mulnum(result, nm1, RADIX_10);
        // Check addition
        assertEquals(-77, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(-1, numtoi32(nm1, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));

        // -1 * 77 = -77
        result.set(nm1);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        assertEquals(-77, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(-1, numtoi32(nm1, RADIX_10));
        assertEquals(77, numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_two_negative_numbers() {
        NUMBER nm7 = i32tonum(-7, RADIX_10);
        NUMBER nm5 = i32tonum(-5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // -7 * -5 = 35
        result.set(nm7);
        Num.mulnum(result, nm5, RADIX_10);
        // Check addition
        assertEquals(35, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(-5, numtoi32(nm5, RADIX_10));
        assertEquals(-7, numtoi32(nm7, RADIX_10));

        // -5 * -7 = 35
        result.set(nm5);
        Num.mulnum(result, nm7, RADIX_10);
        // Check addition
        assertEquals(35, numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        assertEquals(-5, numtoi32(nm5, RADIX_10));
        assertEquals(-7, numtoi32(nm7, RADIX_10));
    }

}
