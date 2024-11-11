package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;

public interface Trans {
    static void scalerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        switch (angletype)
        {
            case RatPack.AngleType.Radians:
                Support.scale2pi(pa, radix, precision);
                break;
            case RatPack.AngleType.Degrees:
                Support.scale(pa, Support.Global.rat_360, radix, precision);
                break;
            case RatPack.AngleType.Gradians:
                Support.scale(pa, Support.Global.rat_400, radix, precision);
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
    static void _sinrat(Ptr<RatPack.RAT> px, int precision)
    {
        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret = RatPack.DUPRAT(px.deref());
        t.thisterm = RatPack.DUPRAT(px.deref());

        t.n2 = RatPack.DUPNUM(Support.Global.num_one);
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
        Support.inbetween(px, Support.Global.rat_one, precision);

        // Since *px might be epsilon near zero we must set it to zero.
        if (Support.rat_le(px.deref(), Support.Global.rat_smallest, precision) && Support.rat_ge(px.deref(), Support.Global.rat_negsmallest, precision))
        {
            px.set(RatPack.DUPRAT(Support.Global.rat_zero));
        }
    }

    static void sinrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Support.scale2pi(px, radix, precision);
        _sinrat(px, precision);
    }

    static void sinanglerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        scalerat(pa, angletype, radix, precision);
        switch (angletype)
        {
            case RatPack.AngleType.Degrees:
                if (Support.rat_gt(pa.deref(), Support.Global.rat_180, precision))
                {
                    Rat.subrat(pa, Support.Global.rat_360, precision);
                }
                Rat.divrat(pa, Support.Global.rat_180, precision);
                Rat.mulrat(pa, Support.Global.pi, precision);
                break;
            case RatPack.AngleType.Gradians:
                if (Support.rat_gt(pa.deref(), Support.Global.rat_200, precision))
                {
                    Rat.subrat(pa, Support.Global.rat_400, precision);
                }
                Rat.divrat(pa, Support.Global.rat_200, precision);
                Rat.mulrat(pa, Support.Global.pi, precision);
            break;
        }
        _sinrat(pa, precision);
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: cosrat, _cosrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the cosine of
    //
    //  RETURN: cosine of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___    2j   j
    //   \  ]  X   -1
    //    \   ---------
    //    /    (2j)!
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
    //   thisterm  = 1 ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //-----------------------------------------------------------------------------
    static void _cosrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret.pp = Conv.i32tonum(1, radix);
        t.pret.pq = Conv.i32tonum(1, radix);

        t.thisterm = RatPack.DUPRAT(t.pret);

        t.n2 = Conv.i32tonum(0, radix);
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
        Support.inbetween(px, Support.Global.rat_one, precision);

        // Since *px might be epsilon near zero we must set it to zero.
        if (Support.rat_le(px.deref(), Support.Global.rat_smallest, precision) && Support.rat_ge(px.deref(), Support.Global.rat_negsmallest, precision))
        {
            px.set(RatPack.DUPRAT(Support.Global.rat_zero));
        }
    }

    static void cosrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Support.scale2pi(px, radix, precision);
        _cosrat(px, radix, precision);
    }

    static void cosanglerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        scalerat(pa, angletype, radix, precision);
        switch (angletype)
        {
            case RatPack.AngleType.Degrees:
                if (Support.rat_gt(pa.deref(), Support.Global.rat_180, precision))
                {
                    Ptr<RatPack.RAT> ptmp = new Ptr<>(RatPack.DUPRAT(Support.Global.rat_360));
                    Rat.subrat(ptmp, pa.deref(), precision);
                    pa.set(ptmp.deref());
                }
                Rat.divrat(pa, Support.Global.rat_180, precision);
                Rat.mulrat(pa, Support.Global.pi, precision);
                break;
            case RatPack.AngleType.Gradians:
                if (Support.rat_gt(pa.deref(), Support.Global.rat_200, precision))
                {
                    Ptr<RatPack.RAT> ptmp = new Ptr<>(RatPack.DUPRAT(Support.Global.rat_400));
                    Rat.subrat(ptmp, pa.deref(), precision);
                    pa.set(ptmp.deref());
                }
                Rat.divrat(pa, Support.Global.rat_200, precision);
                Rat.mulrat(pa, Support.Global.pi, precision);
                break;
        }
        _cosrat(pa, radix, precision);
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: tanrat, _tanrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the tangent of
    //
    //  RETURN: tan     of x in PRAT form.
    //
    //  EXPLANATION: This uses sinrat and cosrat
    //
    //-----------------------------------------------------------------------------
    static void _tanrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Ptr<RatPack.RAT> ptmp = new Ptr<>(RatPack.DUPRAT(px.deref()));

        _sinrat(px, precision);
        _cosrat(ptmp, radix, precision);

        if (Rat.zerrat(ptmp.deref()))
        {
            throw new ErrorCodeException(CalcErr.CALC_E_DOMAIN);
        }
        Rat.divrat(px, ptmp.deref(), precision);
    }

    static void tanrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Support.scale2pi(px, radix, precision);
        _tanrat(px, radix, precision);
    }

    static void tananglerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        scalerat(pa, angletype, radix, precision);
        switch (angletype)
        {
            case RatPack.AngleType.Degrees:
                if (Support.rat_gt(pa.deref(), Support.Global.rat_180, precision))
                {
                    Rat.subrat(pa, Support.Global.rat_180, precision);
                }
                Rat.divrat(pa, Support.Global.rat_180, precision);
                Rat.mulrat(pa, Support.Global.pi, precision);
                break;
            case RatPack.AngleType.Gradians:
                if (Support.rat_gt(pa.deref(), Support.Global.rat_200, precision))
                {
                    Rat.subrat(pa, Support.Global.rat_200, precision);
                }
                Rat.divrat(pa, Support.Global.rat_200, precision);
                Rat.mulrat(pa, Support.Global.pi, precision);
                break;
        }
        _tanrat(pa, radix, precision);
    }
}
