package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.RAT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SupportTests {
    private static final uint BASE_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    static void beforeAll() {
        Support.ChangeConstants(BASE_10, PRECISION);
    }

    @Test
    public void intrat_works_for_real_numbers() {
        RAT rx = Conv.StringToRat(false, "237.034", false, "0", BASE_10, PRECISION);
        Ptr<RAT> r = new Ptr<>(rx);

        // TODO: Figure out how radix works here
        Support.intrat(r, BASE_10, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("237", result);
    }

    @Test
    public void intrat_works_for_integers() {
        RAT rx = Conv.StringToRat(false, "23", false, "0", BASE_10, PRECISION);
        Ptr<RAT> r = new Ptr<>(rx);

        Support.intrat(r, BASE_10, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("23", result);
    }

    @Test
    public void rat_equ_works() {
        RAT r1 = Support.Global.rat_one.clone();
        RAT rm1 = Support.Global.rat_neg_one.clone();
        RAT r6 = Support.Global.rat_six;

        Assertions.assertTrue(Support.rat_equ(r1, r1, PRECISION));
        Assertions.assertTrue(Support.rat_equ(rm1, rm1, PRECISION));
        Assertions.assertTrue(Support.rat_equ(r6, r6, PRECISION));

        Assertions.assertFalse(Support.rat_equ(r1, rm1, PRECISION));
        Assertions.assertFalse(Support.rat_equ(rm1, r1, PRECISION));

        Assertions.assertFalse(Support.rat_equ(rm1, r6, PRECISION));
        Assertions.assertFalse(Support.rat_equ(r6, rm1, PRECISION));

        Assertions.assertFalse(Support.rat_equ(r6, r1, PRECISION));
        Assertions.assertFalse(Support.rat_equ(r1, r6, PRECISION));
    }

    @Test
    public void rat_ge_works() {
        RAT r1 = Support.Global.rat_one.clone();
        RAT rm1 = Support.Global.rat_neg_one.clone();
        RAT r6 = Support.Global.rat_six;

        Assertions.assertTrue(Support.rat_ge(r1, r1, PRECISION));
        Assertions.assertTrue(Support.rat_ge(rm1, rm1, PRECISION));

        Assertions.assertTrue(Support.rat_ge(r1, rm1, PRECISION));
        Assertions.assertTrue(Support.rat_ge(r6, r1, PRECISION));

        Assertions.assertFalse(Support.rat_ge(rm1, r1, PRECISION));
        Assertions.assertFalse(Support.rat_ge(rm1, r6, PRECISION));
        Assertions.assertFalse(Support.rat_ge(r1, r6, PRECISION));
    }

    @Test
    public void rat_gt_works() {
        RAT r1 = Support.Global.rat_one.clone();
        RAT rm1 = Support.Global.rat_neg_one.clone();
        RAT r6 = Support.Global.rat_six;

        Assertions.assertFalse(Support.rat_gt(r1, r1, PRECISION));
        Assertions.assertFalse(Support.rat_gt(rm1, rm1, PRECISION));

        Assertions.assertTrue(Support.rat_gt(r1, rm1, PRECISION));
        Assertions.assertTrue(Support.rat_gt(r6, r1, PRECISION));

        Assertions.assertFalse(Support.rat_gt(rm1, r1, PRECISION));
        Assertions.assertFalse(Support.rat_gt(rm1, r6, PRECISION));
        Assertions.assertFalse(Support.rat_gt(r1, r6, PRECISION));
    }

    @Test
    public void rat_le_works() {
        RAT r1 = Support.Global.rat_one.clone();
        RAT rm1 = Support.Global.rat_neg_one.clone();
        RAT r6 = Support.Global.rat_six;

        Assertions.assertTrue(Support.rat_le(r1, r1, PRECISION));
        Assertions.assertTrue(Support.rat_le(rm1, rm1, PRECISION));

        Assertions.assertTrue(Support.rat_le(rm1, r1, PRECISION));
        Assertions.assertTrue(Support.rat_le(rm1, r6, PRECISION));
        Assertions.assertTrue(Support.rat_le(r1, r6, PRECISION));

        Assertions.assertFalse(Support.rat_le(r1, rm1, PRECISION));
        Assertions.assertFalse(Support.rat_le(r6, r1, PRECISION));
    }

    @Test
    public void rat_lt_works() {
        RAT r1 = Support.Global.rat_one.clone();
        RAT rm1 = Support.Global.rat_neg_one.clone();
        RAT r6 = Support.Global.rat_six;

        Assertions.assertFalse(Support.rat_lt(r1, r1, PRECISION));
        Assertions.assertFalse(Support.rat_lt(rm1, rm1, PRECISION));

        Assertions.assertTrue(Support.rat_lt(rm1, r1, PRECISION));
        Assertions.assertTrue(Support.rat_lt(rm1, r6, PRECISION));
        Assertions.assertTrue(Support.rat_lt(r1, r6, PRECISION));

        Assertions.assertFalse(Support.rat_lt(r1, rm1, PRECISION));
        Assertions.assertFalse(Support.rat_lt(r6, r1, PRECISION));
    }

    @Test
    public void rat_neq_works() {
        RAT r1 = Support.Global.rat_one.clone();
        RAT rm1 = Support.Global.rat_neg_one.clone();
        RAT r6 = Support.Global.rat_six;

        Assertions.assertFalse(Support.rat_neq(r1, r1, PRECISION));
        Assertions.assertFalse(Support.rat_neq(rm1, rm1, PRECISION));

        Assertions.assertTrue(Support.rat_neq(r1, rm1, PRECISION));
        Assertions.assertTrue(Support.rat_neq(rm1, r1, PRECISION));
        Assertions.assertTrue(Support.rat_neq(r6, r1, PRECISION));
        Assertions.assertTrue(Support.rat_neq(r1, r6, PRECISION));
    }
}
