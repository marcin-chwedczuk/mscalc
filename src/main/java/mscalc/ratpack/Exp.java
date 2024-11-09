package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.Num.addnum;
import static mscalc.ratpack.Rat.mulrat;
import static mscalc.ratpack.Rat.subrat;
import static mscalc.ratpack.RatPack.BASEX;
import static mscalc.ratpack.RatPack.DUPRAT;
import static mscalc.ratpack.Support.*;
import static mscalc.ratpack.Support.Global.*;

public interface Exp {
    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: exprat
    //
    //  ARGUMENTS: x PRAT representation of number to exponentiate
    //
    //  RETURN: exp  of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___
    //   \  ]                                               X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j      j+1
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //-----------------------------------------------------------------------------
    static void _exprat(Ptr<RAT> px, uint radix, int precision)
    {
        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret.ppPtr(ppp -> addnum(ppp, num_one, radix));
        t.pret.pqPtr(ppq -> addnum(ppq, num_one, radix));

        t.thisterm = DUPRAT(t.pret);
        t.n2 = i32tonum(0, radix);

        do
        {
            t.NEXTTERM(px.deref(), () -> {
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
            }, precision);


            uint RADIX_10 = uint.of(10);
            RAT tmp = new RAT();
            tmp.pp = nRadixxtonum(t.thisterm.pp, RADIX_10, 10);
            tmp.pq = nRadixxtonum(t.thisterm.pq, RADIX_10, 10);
            String str = Conv.RatToString(new Ptr<>(tmp), RatPack.NumberFormat.Float, RADIX_10, 10);
            System.out.println("STEP " + str);
            try { Thread.sleep(10); } catch (Exception e) { }
             // TODO: Cleanup


        } while (!t.thisterm.SMALL_ENOUGH_RAT(precision));

        px.set(t.RESULT());
    }

    static void exprat(Ptr<RAT> px, uint radix, int precision)
    {
        if (rat_gt(px.deref(), rat_max_exp, precision) || rat_lt(px.deref(), rat_min_exp, precision))
        {
            // Don't attempt exp of anything large.
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }

        Ptr<RAT> pwr = new Ptr<>(DUPRAT(rat_exp));
        Ptr<RAT> pint = new Ptr<>(DUPRAT(px.deref()));

        intrat(pint, radix, precision);

        final int intpwr = rattoi32(pint.deref(), radix, precision);
        ratpowi32(pwr, intpwr, precision);

        subrat(px, pint.deref(), precision);

        // It just so happens to be an integral power of e.
        if (rat_gt(px.deref(), rat_negsmallest, precision) && rat_lt(px.deref(), rat_smallest, precision))
        {
            px.set(DUPRAT(pwr.deref()));
        }
        else
        {
            _exprat(px, radix, precision);
            mulrat(px, pwr.deref(), precision);
        }
    }

    static void lograt(Ptr<RAT> px, int precision) {
        System.out.println("Not implemented");
    }
}
