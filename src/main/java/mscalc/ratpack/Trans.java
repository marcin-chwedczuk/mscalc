package mscalc.ratpack;

import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.AngleType;
import mscalc.ratpack.RatPack.RAT;
import mscalc.ratpack.RatPack.TYLOR;

import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.DUPNUM;
import static mscalc.ratpack.RatPack.DUPRAT;
import static mscalc.ratpack.Support.*;
import static mscalc.ratpack.Support.Global.*;

public interface Trans {
    static void scalerat(Ptr<RAT> pa, AngleType angletype, uint radix, int precision)
    {
        switch (angletype)
        {
            case AngleType.Radians:
                scale2pi(pa, radix, precision);
                break;
            case AngleType.Degrees:
                scale(pa, rat_360, radix, precision);
                break;
            case AngleType.Gradians:
                scale(pa, rat_400, radix, precision);
                break;
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: sinrat, _sinrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the sine of
    //
    //  RETURN: sin of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___          2j+1
    //   \  ]   j    X
    //    \   -1  * ---------
    //    /          (2j+1)!
    //   /__]
    //   j=0
    //          or,
    //    n
    //   ___                                                 2
    //   \  ]                                              -X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j)*(2j+1)
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //-----------------------------------------------------------------------------
    static void _sinrat(Ptr<RAT> px, int precision)
    {
        TYLOR t = new TYLOR(px.deref(), precision);

        t.pret = DUPRAT(px.deref());
        t.thisterm = DUPRAT(px.deref());

        t.n2 = DUPNUM(num_one);
        t.xx.pp.sign *= -1;

        do
        {
            t.NEXTTERM(t.xx, () -> {
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
            }, precision);
        } while (!t.thisterm.SMALL_ENOUGH_RAT(precision));

        px.set(t.RESULT());

        // Since *px might be epsilon above 1 or below -1, due to TRIMIT we need
        // this trick here.
        inbetween(px, rat_one, precision);

        // Since *px might be epsilon near zero we must set it to zero.
        if (rat_le(px.deref(), rat_smallest, precision) && rat_ge(px.deref(), rat_negsmallest, precision))
        {
            px.set(DUPRAT(rat_zero));
        }
    }

    static void sinrat(Ptr<RAT> px, uint radix, int precision)
    {
        scale2pi(px, radix, precision);
        _sinrat(px, precision);
    }

    static void sinanglerat(Ptr<RAT> pa, AngleType angletype, uint radix, int precision)
    {
        scalerat(pa, angletype, radix, precision);
        switch (angletype)
        {
            case AngleType.Degrees:
                if (rat_gt(pa.deref(), rat_180, precision))
                {
                    subrat(pa, rat_360, precision);
                }
                divrat(pa, rat_180, precision);
                mulrat(pa, pi, precision);
                break;
            case AngleType.Gradians:
                if (rat_gt(pa.deref(), rat_200, precision))
                {
                    subrat(pa, rat_400, precision);
                }
                divrat(pa, rat_200, precision);
                mulrat(pa, pi, precision);
            break;
        }
        _sinrat(pa, precision);
    }
}
