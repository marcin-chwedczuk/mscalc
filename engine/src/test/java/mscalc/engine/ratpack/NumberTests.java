package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.NUMBER;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static mscalc.engine.ratpack.Num.lessnum;
import static mscalc.engine.ratpack.Num.zernum;
import static org.junit.jupiter.api.Assertions.*;

public class NumberTests {
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 10;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_10, 20);
    }

    @Test
    public void addition_two_numbers() {
        NUMBER n123 = Conv.i32tonum(123, RADIX_10);
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 123 + 77 = 200
        result.set(n123);
        Num.addnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(200, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
        Assertions.assertEquals(123, Conv.numtoi32(n123, RADIX_10));

        // 77 + 123 = 200
        result.set(n77);
        Num.addnum(result, n123, RADIX_10);
        // Check addition
        Assertions.assertEquals(200, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
        Assertions.assertEquals(123, Conv.numtoi32(n123, RADIX_10));
    }

    @Test
    public void addition_with_zero() {
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);
        NUMBER n0 = Conv.i32tonum(0, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 0 + 77 = 77
        result.set(n0);
        Num.addnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(77, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(0, Conv.numtoi32(n0, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));

        // 77 + 0 = 77
        result.set(n77);
        Num.addnum(result, n0, RADIX_10);
        // Check addition
        Assertions.assertEquals(77, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
        Assertions.assertEquals(0, Conv.numtoi32(n0, RADIX_10));
    }

    @Test
    public void addition_with_minus_one() {
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);
        NUMBER nm1 = Conv.i32tonum(-1, RADIX_10);
        NUMBER n76 = Conv.i32tonum(76, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // (-1) + 77 = 76
        result.set(nm1);
        Num.addnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(76, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(-1, Conv.numtoi32(nm1, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));

        // 77 + (-1) = 76
        result.set(n77);
        Num.addnum(result, nm1, RADIX_10);
        // Check addition
        Assertions.assertEquals(76, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
        Assertions.assertEquals(-1, Conv.numtoi32(nm1, RADIX_10));
    }

    @Test
    public void addition_with_carry_propagation() {
        NUMBER n299999 = Conv.i32tonum(299999, RADIX_10);
        NUMBER n2 = Conv.i32tonum(2, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 299999 + 2
        result.set(n299999);
        Num.addnum(result, n2, RADIX_10);
        // Check addition
        Assertions.assertEquals(300001, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(299999, Conv.numtoi32(n299999, RADIX_10));
        Assertions.assertEquals(2, Conv.numtoi32(n2, RADIX_10));

        // 2 + 299999
        result.set(n2);
        Num.addnum(result, n299999, RADIX_10);
        // Check addition
        Assertions.assertEquals(300001, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(299999, Conv.numtoi32(n299999, RADIX_10));
        Assertions.assertEquals(2, Conv.numtoi32(n2, RADIX_10));
    }

    @Test
    public void addition_can_add_number_to_itself() {
        NUMBER n2 = Conv.i32tonum(2, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        result.set(n2);
        Num.addnum(result, n2, RADIX_10);
        // Check addition
        Assertions.assertEquals(4, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(2, Conv.numtoi32(n2, RADIX_10));
    }

    @Test
    public void multiplication_two_numbers() {
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);
        NUMBER n111 = Conv.i32tonum(111, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * 111 = 8547
        result.set(n77);
        Num.mulnum(result, n111, RADIX_10);
        // Check addition
        Assertions.assertEquals(8547, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(111, Conv.numtoi32(n111, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));

        // 111 * 77 = 8547
        result.set(n111);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(8547, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(111, Conv.numtoi32(n111, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_with_zero() {
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);
        NUMBER n0 = Conv.i32tonum(0, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * 0 = 0
        result.set(n77);
        Num.mulnum(result, n0, RADIX_10);
        // Check addition
        Assertions.assertEquals(0, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(0, Conv.numtoi32(n0, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));

        // 0 * 77 = 0
        result.set(n0);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(0, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(0, Conv.numtoi32(n0, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_with_one() {
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);
        NUMBER n1 = Conv.i32tonum(1, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * 1 = 77
        result.set(n77);
        Num.mulnum(result, n1, RADIX_10);
        // Check addition
        Assertions.assertEquals(77, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(1, Conv.numtoi32(n1, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));

        // 1 * 77 = 77
        result.set(n1);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(77, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(1, Conv.numtoi32(n1, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_with_negative() {
        NUMBER n77 = Conv.i32tonum(77, RADIX_10);
        NUMBER nm1 = Conv.i32tonum(-1, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 77 * -1 = -77
        result.set(n77);
        Num.mulnum(result, nm1, RADIX_10);
        // Check addition
        Assertions.assertEquals(-77, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(-1, Conv.numtoi32(nm1, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));

        // -1 * 77 = -77
        result.set(nm1);
        Num.mulnum(result, n77, RADIX_10);
        // Check addition
        Assertions.assertEquals(-77, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(-1, Conv.numtoi32(nm1, RADIX_10));
        Assertions.assertEquals(77, Conv.numtoi32(n77, RADIX_10));
    }

    @Test
    public void multiplication_two_negative_numbers() {
        NUMBER nm7 = Conv.i32tonum(-7, RADIX_10);
        NUMBER nm5 = Conv.i32tonum(-5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // -7 * -5 = 35
        result.set(nm7);
        Num.mulnum(result, nm5, RADIX_10);
        // Check addition
        Assertions.assertEquals(35, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(-5, Conv.numtoi32(nm5, RADIX_10));
        Assertions.assertEquals(-7, Conv.numtoi32(nm7, RADIX_10));

        // -5 * -7 = 35
        result.set(nm5);
        Num.mulnum(result, nm7, RADIX_10);
        // Check addition
        Assertions.assertEquals(35, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(-5, Conv.numtoi32(nm5, RADIX_10));
        Assertions.assertEquals(-7, Conv.numtoi32(nm7, RADIX_10));
    }

    @Test
    public void multiplication_by_base() {
        NUMBER n100 = Conv.i32tonum(100, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 100 * 5 = 500
        result.set(n100);
        Num.mulnum(result, n5, RADIX_10);
        // Check addition
        Assertions.assertEquals(500, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(100, Conv.numtoi32(n100, RADIX_10));
        Assertions.assertEquals(5, Conv.numtoi32(n5, RADIX_10));

        // 5 * 100 = 500
        result.set(n5);
        Num.mulnum(result, n100, RADIX_10);
        // Check addition
        Assertions.assertEquals(500, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(100, Conv.numtoi32(n100, RADIX_10));
        Assertions.assertEquals(5, Conv.numtoi32(n5, RADIX_10));
    }

    @Test
    public void mutliplication_can_multiply_number_by_itself() {
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        result.set(n5);
        Num.mulnum(result, n5, RADIX_10);
        // Check addition
        Assertions.assertEquals(25, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(5, Conv.numtoi32(n5, RADIX_10));
    }

    @Test
    public void remainder_two_numbers() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER n7 = Conv.i32tonum(7, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 % 7 = 5
        result.set(n12);
        Num.remnum(result, n7, RADIX_10);
        // Check addition
        Assertions.assertEquals(5, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(7, Conv.numtoi32(n7, RADIX_10));
    }

    @Test
    public void remainder_with_itself() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 % 7 = 5
        result.set(n12);
        Num.remnum(result, n12, RADIX_10);
        // Check addition
        Assertions.assertEquals(0, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
    }

    @Test
    public void reminder_is_entire_number() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER n100 = Conv.i32tonum(100, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 % 100 = 12
        result.set(n12);
        Num.remnum(result, n100, RADIX_10);
        // Check addition
        Assertions.assertEquals(12, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(100, Conv.numtoi32(n100, RADIX_10));
    }

    @Test
    public void reminder_with_one() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER n1 = Conv.i32tonum(1, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 % 1 = 0
        result.set(n12);
        Num.remnum(result, n1, RADIX_10);
        // Check addition
        Assertions.assertEquals(0, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(1, Conv.numtoi32(n1, RADIX_10));
    }

    @Test
    @Disabled("Infinite loop when dividing by zero instead of throwing an error. TODO")
    public void reminder_with_zero_error() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER n0 = Conv.i32tonum(0, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 % 1 = 0
        result.set(n12);
        Num.remnum(result, n0, RADIX_10);
        // Check addition
        Assertions.assertEquals(0, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(0, Conv.numtoi32(n0, RADIX_10));
    }

    @Test
    public void remainder_negative_number() {
        NUMBER nm12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER n7 = Conv.i32tonum(7, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // -12 % 7 = -5
        result.set(nm12);
        Num.remnum(result, n7, RADIX_10);
        // Check addition
        Assertions.assertEquals(-5, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(-12, Conv.numtoi32(nm12, RADIX_10));
        Assertions.assertEquals(7, Conv.numtoi32(n7, RADIX_10));
    }

    @Test
    public void remainder_negative_number2() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER nm7 = Conv.i32tonum(-7, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 % -7 = 5
        result.set(n12);
        Num.remnum(result, nm7, RADIX_10);
        // Check addition
        Assertions.assertEquals(5, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(-7, Conv.numtoi32(nm7, RADIX_10));
    }

    @Test
    public void division_without_remainder() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER n4 = Conv.i32tonum(4, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 / 4 = 3
        result.set(n12);
        Num.divnum(result, n4, RADIX_10, PRECISION);
        // Check addition
        Assertions.assertEquals(3, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(4, Conv.numtoi32(n4, RADIX_10));
    }

    @Test
    public void division_with_remainder() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 / 5 = 2
        result.set(n12);
        Num.divnum(result, n5, RADIX_10, PRECISION);
        // Check addition
        Assertions.assertEquals(2, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(5, Conv.numtoi32(n5, RADIX_10));
    }

    @Test
    public void division_with_remainder_huge() {
        NUMBER n1247 = Conv.i32tonum(1247, RADIX_10);
        NUMBER n15 = Conv.i32tonum(15, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        result.set(n1247);
        Num.divnum(result, n15, RADIX_10, PRECISION);
        // Check addition
        Assertions.assertEquals(83, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(1247, Conv.numtoi32(n1247, RADIX_10));
        Assertions.assertEquals(15, Conv.numtoi32(n15, RADIX_10));
    }

    @Test
    // @Disabled("Badly defined, looks like it returns 9999, for 4 digit input")
    public void division_by_zero() {
        NUMBER n1222 = Conv.i32tonum(1222, RADIX_10);
        NUMBER n0 = Conv.i32tonum(0, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 / 0 = 9999???
        result.set(n1222);
        Num.divnum(result, n0, RADIX_10, PRECISION);
        // Check addition
        // TODO: Must be primitive form of Infinity
        Assertions.assertEquals(9999, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(1222, Conv.numtoi32(n1222, RADIX_10));
        Assertions.assertEquals(0, Conv.numtoi32(n0, RADIX_10));
    }

    @Test
    public void division_negative_number() {
        NUMBER n12 = Conv.i32tonum(12, RADIX_10);
        NUMBER nm5 = Conv.i32tonum(-5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // 12 / -5 = -2
        result.set(n12);
        Num.divnum(result, nm5, RADIX_10, PRECISION);
        // Check addition
        Assertions.assertEquals(-2, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(n12, RADIX_10));
        Assertions.assertEquals(-5, Conv.numtoi32(nm5, RADIX_10));
    }

    @Test
    @Disabled("TODO: May be a bug, returns -12 instead of 2. Need to investigate.")
    public void division_negative_number2() {
        NUMBER nm12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);

        Ptr<NUMBER> result = new Ptr<>(null);

        // -12 / 5 = -2
        result.set(nm12);
        Num.divnum(result, n5, RADIX_10, PRECISION);
        // Check addition
        Assertions.assertEquals(-2, Conv.numtoi32(result.deref(), RADIX_10));
        // Check arguments not destroyed:
        Assertions.assertEquals(12, Conv.numtoi32(nm12, RADIX_10));
        Assertions.assertEquals(5, Conv.numtoi32(n5, RADIX_10));
    }

    @Test
    public void equal_numbers() {
        NUMBER nm12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER nm12Dup = Conv.i32tonum(-12, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);
        NUMBER n5Dup = Conv.i32tonum(5, RADIX_10);

        NUMBER nm5 = Conv.i32tonum(-5, RADIX_10);
        NUMBER nm5Dup = Conv.i32tonum(-5, RADIX_10);

        assertTrue(Num.equnum(nm12, nm12));
        assertTrue(Num.equnum(nm12Dup, nm12));
        assertTrue(Num.equnum(nm12Dup, nm12Dup));

        assertTrue(Num.equnum(n5, n5));
        assertTrue(Num.equnum(n5Dup, n5));
        assertTrue(Num.equnum(n5, n5Dup));

        assertTrue(Num.equnum(nm5Dup, nm5Dup));
    }

    @Test
    public void not_equal_numbers() {
        // TODO: Looks like this does not take under account number sign.
        // Maybe a bug, maybe a feature?
        NUMBER nm12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);
        NUMBER nm5 = Conv.i32tonum(-5, RADIX_10);

        assertFalse(Num.equnum(nm12, n5));
        assertFalse(Num.equnum(n5, nm12));
        // assertFalse(Num.equnum(n5, nm12));

        // assertFalse(Num.equnum(n5, nm5));
        // assertFalse(Num.equnum(nm5, n5));
        assertFalse(Num.equnum(nm5, nm12));
        assertFalse(Num.equnum(nm12, nm5));
    }

    @Test
    public void lessnum_works() {
        NUMBER nm12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER n12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER n15 = Conv.i32tonum(15, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);

        // Does the number equivalent of ( abs(a) < abs(b) )

        assertTrue(lessnum(n5, nm12));
        assertFalse(lessnum(nm12, n5));

        assertTrue(lessnum(n5, n15));
        assertFalse(lessnum(n15, n5));

        assertFalse(lessnum(n5, n5));
        assertFalse(lessnum(nm12, nm12));

        assertFalse(lessnum(n12, nm12));
        assertFalse(lessnum(nm12, n12));
    }

    @Test
    public void zeronum_works() {
        NUMBER nm12 = Conv.i32tonum(-12, RADIX_10);
        NUMBER zero = Conv.i32tonum(0, RADIX_10);
        NUMBER n5 = Conv.i32tonum(5, RADIX_10);

        assertTrue(zernum(zero));
        assertFalse(zernum(nm12));
        assertFalse(zernum(n5));
    }
}
