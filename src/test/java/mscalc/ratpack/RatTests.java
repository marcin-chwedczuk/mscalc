package mscalc.ratpack;

import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.RAT;
import mscalc.ratpack.Support.Global;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static mscalc.ratpack.Rat.fracrat;
import static mscalc.ratpack.Rat.mulrat;
import static mscalc.ratpack.RatPack.BASEX;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(result, "0.14159265358979323846");
    }

    @Test
    public void fracrat_integer() {
        Ptr<RAT> r = new Ptr<>(Global.rat_six.clone());

        fracrat(r, BASE_10, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals(result, "0");
    }

    @Test
    public void mulrat_multiply_by_zero() {
        Ptr<RAT> r = new Ptr<>(Global.rat_zero.clone());
        RAT r6 = Global.rat_six.clone();

        mulrat(r, r6, PRECISION);

        String result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals(result, "0");

        // Reverse parameters

        r = new Ptr<>(Global.rat_six.clone());
        r6 = Global.rat_zero.clone();

        mulrat(r, r6, PRECISION);

        result = Conv.RatToString(r, RatPack.NumberFormat.Float, BASE_10, PRECISION);
        assertEquals(result, "0");
    }

}
