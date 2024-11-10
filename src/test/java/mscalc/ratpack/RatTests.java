package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.RAT;
import mscalc.ratpack.Support.Global;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static mscalc.ratpack.CalcErr.CALC_E_DIVIDEBYZERO;
import static mscalc.ratpack.CalcErr.CALC_E_INDEFINITE;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.BASEX;
import static org.junit.jupiter.api.Assertions.*;

public class RatTests {
    private static final uint BASE_10 = uint.of(10);
    private static final int PRECISION = 20;

    @BeforeAll
    static void beforeAll() {
        Support.ChangeConstants(BASE_10, PRECISION);
    }

    @Test
    public void gcdrad_has_non_one_gcd() {
        RAT r = new RAT();
        r.pp = Conv.i32tonum(64, BASEX);
        r.pq = Conv.i32tonum(256, BASEX);

        Ptr<RAT> result = new Ptr<>(r);
        Rat.gcdrat(result, PRECISION);

        int pp = Conv.numtoi32(result.deref().pp, BASEX);
        int pq = Conv.numtoi32(result.deref().pq, BASEX);

        assertEquals(1, pp );
        assertEquals(4, pq );
    }

    @Test
    public void fracrat_real_number() {
        Ptr<RAT> r = new Ptr<>(Global.pi.clone());

        fracrat(r, BASE_10, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("0.14159265358979323846", result);
    }

    @Test
    public void fracrat_integer() {
        Ptr<RAT> r = new Ptr<>(Global.rat_six.clone());

        fracrat(r, BASE_10, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("0", result);
    }

    @Test
    public void mulrat_multiply_by_zero() {
        Ptr<RAT> r = new Ptr<>(Global.rat_zero.toBaseXFrom(BASE_10));
        RAT r6 = Global.rat_six.toBaseXFrom(BASE_10);

        mulrat(r, r6, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASEX, PRECISION);
        assertEquals("0", result);

        // Reverse parameters

        r = new Ptr<>(Global.rat_six.toBaseXFrom(BASE_10));
        r6 = Global.rat_zero.toBaseXFrom(BASE_10);

        mulrat(r, r6, PRECISION);

        result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("0", result);
    }

    @Test
    public void mulrat_nonzero_values() {
        Ptr<RAT> r = new Ptr<>(Global.rat_two.toBaseXFrom(BASE_10));
        RAT r6 = Global.rat_six.toBaseXFrom(BASE_10);

        mulrat(r, r6, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("12", result);
    }

    @Test
    public void divrat_div_by_zero() {
        Ptr<RAT> r = new Ptr<>(Global.rat_two.toBaseXFrom(BASE_10));
        RAT rzero = Global.rat_zero.toBaseXFrom(BASE_10);

        var ex = assertThrows(ErrorCodeException.class, () -> divrat(r, rzero, PRECISION));

        assertEquals(CALC_E_DIVIDEBYZERO, ex.errorCode());
    }

    @Test
    public void divrat_zero_by_zero() {
        Ptr<RAT> r = new Ptr<>(Global.rat_zero.toBaseXFrom(BASE_10));
        RAT rzero = Global.rat_zero.toBaseXFrom(BASE_10);

        var ex = assertThrows(ErrorCodeException.class, () -> divrat(r, rzero, PRECISION));

        assertEquals(CALC_E_INDEFINITE, ex.errorCode());
    }

    @Test
    public void divrat_zero_by_nonzero() {
        Ptr<RAT> r = new Ptr<>(Global.rat_zero.toBaseXFrom(BASE_10));
        RAT rzero = Global.rat_six.toBaseXFrom(BASE_10);

        divrat(r, rzero, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("0", result);
    }

    @Test
    public void divrat_two_nonzero_values() {
        Ptr<RAT> r = new Ptr<>(Global.rat_one.toBaseXFrom(BASE_10));
        RAT rzero = Global.rat_six.toBaseXFrom(BASE_10);

        divrat(r, rzero, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("0.16666666666666666667", result);
    }

    @Test
    public void subrat_works() {
        Ptr<RAT> r = new Ptr<>(Global.rat_one.toBaseXFrom(BASE_10));
        RAT rzero = Global.rat_six.toBaseXFrom(BASE_10);

        subrat(r, rzero, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("-5", result);
    }

    @Test
    public void addrat_works_same_number() {
        Ptr<RAT> r = new Ptr<>(Global.rat_six.toBaseXFrom(BASE_10));
        RAT rzero = Global.rat_six.toBaseXFrom(BASE_10);

        addrat(r, rzero, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("12", result);
    }

    @Test
    public void addrat_works_different_numbers() {
        Ptr<RAT> r = new Ptr<>(Global.rat_six.toBaseXFrom(BASE_10));
        RAT rx = Conv.StringToRat(false, "0.034", false, "0", BASE_10, PRECISION);

        addrat(r, rx, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals("6.034", result);
    }

    @Test
    @Disabled("not impl yet.")
    public void rootrat() {

    }

    @Test
    public void zerrat_works() {
        assertTrue(zerrat(Global.rat_zero.clone()));
        assertFalse(zerrat(Global.rat_one.clone()));
        assertFalse(zerrat(Global.rat_neg_one.clone()));
    }
}
