package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;

public interface ITrans {
    static void ascalerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, int precision)
    {
        switch (angletype)
        {
            case RatPack.AngleType.Radians:
                break;
            case RatPack.AngleType.Degrees:
                Rat.divrat(pa, Support.Global.two_pi, precision);
                Rat.mulrat(pa, Support.Global.rat_360, precision);
                break;
            case RatPack.AngleType.Gradians:
                Rat.divrat(pa, Support.Global.two_pi, precision);
                Rat.mulrat(pa, Support.Global.rat_400, precision);
                break;
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: asinrat, _asinrat
    //
    //  ARGUMENTS: x PRAT representation of number to take the inverse
    //    sine of
    //  RETURN: asin  of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___                                                   2 2
    //   \  ]                                            (2j+1) X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j+2)*(2j+3)
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   If abs(x) > 0.85 then an alternate form is used
    //      pi/2-sgn(x)*asin(sqrt(1-x^2)
    //
    //
    //-----------------------------------------------------------------------------
    static void _asinrat(Ptr<RatPack.RAT> px, int precision)
    {
        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret = RatPack.DUPRAT(px.deref());
        t.thisterm = RatPack.DUPRAT(px.deref());
        t.n2 = RatPack.DUPNUM(Support.Global.num_one);

        do
        {
            t.NEXTTERM(t.xx, () -> {
                t.MULNUM(t.n2);
                t.MULNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
            }, precision);

        } while (!t.thisterm.SMALL_ENOUGH_RAT(precision));

        px.set(t.RESULT());
    }

    static void asinanglerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        asinrat(pa, radix, precision);
        ascalerat(pa, angletype, precision);
    }

    static void asinrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Ptr<RatPack.RAT> pret = new Ptr<>();
        Ptr<RatPack.RAT> phack = new Ptr<>();
        int sgn = px.deref().SIGN();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        // Avoid the really bad part of the asin curve near +/-1.
        phack.set(RatPack.DUPRAT(px.deref()));
        Rat.subrat(phack, Support.Global.rat_one, precision);
        // Since *px might be epsilon near zero we must set it to zero.
        if (Support.rat_le(phack.deref(), Support.Global.rat_smallest, precision) && Support.rat_ge(phack.deref(), Support.Global.rat_negsmallest, precision))
        {
            px.set(RatPack.DUPRAT(Support.Global.pi_over_two));
        }
        else
        {
            if (Support.rat_gt(px.deref(), Support.Global.pt_eight_five, precision))
            {
                if (Support.rat_gt(px.deref(), Support.Global.rat_one, precision))
                {
                    Rat.subrat(px, Support.Global.rat_one, precision);
                    if (Support.rat_gt(px.deref(), Support.Global.rat_smallest, precision))
                    {
                        throw new ErrorCodeException(CalcErr.CALC_E_DOMAIN);
                    }
                    else
                    {
                        px.set(RatPack.DUPRAT(Support.Global.rat_one));
                    }
                }
                pret.set(RatPack.DUPRAT(px.deref()));
                Rat.mulrat(px, pret.deref(), precision);
                px.deref().pp.sign *= -1;
                Rat.addrat(px, Support.Global.rat_one, precision);
                Rat.rootrat(px, Support.Global.rat_two, radix, precision);
                _asinrat(px, precision);
                px.deref().pp.sign *= -1;
                Rat.addrat(px, Support.Global.pi_over_two, precision);
            }
            else
            {
                _asinrat(px, precision);
            }
        }

        px.deref().pp.sign = sgn;
        px.deref().pq.sign = 1;
    }


    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: acosrat, _acosrat
    //
    //  ARGUMENTS: x PRAT representation of number to take the inverse
    //    cosine of
    //  RETURN: acos  of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___                                                   2 2
    //   \  ]                                            (2j+1) X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j+2)*(2j+3)
    //   /__]
    //   j=0
    //
    //   thisterm  = 1 ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   In this case pi/2-asin(x) is used.  At least for now _acosrat isn't
    //      called.
    //
    //-----------------------------------------------------------------------------
    static void acosanglerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        acosrat(pa, radix, precision);
        ascalerat(pa, angletype, precision);
    }

    static void _acosrat(Ptr<RatPack.RAT> px, int precision)
    {
        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);
        t.thisterm = Conv.createrat();
        t.thisterm.pp = Conv.i32tonum(1, RatPack.BASEX);
        t.thisterm.pq = Conv.i32tonum(1, RatPack.BASEX);

        t.n2 = RatPack.DUPNUM(Support.Global.num_one);

        do
        {
            t.NEXTTERM(t.xx, () -> {
                t.MULNUM(t.n2);
                t.MULNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
            }, precision);
        } while (!t.thisterm.SMALL_ENOUGH_RAT(precision));

        px.set(t.RESULT());
    }

    static void acosrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        int sgn = px.deref().SIGN();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        if (Support.rat_equ(px.deref(), Support.Global.rat_one, precision))
        {
            if (sgn == -1)
            {
                px.set(RatPack.DUPRAT(Support.Global.pi));
            }
            else
            {
                px.set(RatPack.DUPRAT(Support.Global.rat_zero));
            }
        }
        else
        {
            px.deref().pp.sign = sgn;
            asinrat(px, radix, precision);
            px.deref().pp.sign *= -1;
            Rat.addrat(px, Support.Global.pi_over_two, precision);
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: atanrat, _atanrat
    //
    //  ARGUMENTS: x PRAT representation of number to take the inverse
    //              hyperbolic tangent of
    //
    //  RETURN: atanh of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___                                                   2
    //   \  ]                                            (2j)*X (-1^j)
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j+2)
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   If abs(x) > 0.85 then an alternate form is used
    //      asin(x/sqrt(q+x^2))
    //
    //   And if abs(x) > 2.0 then this form is used.
    //
    //   pi/2 - atan(1/x)
    //
    //-----------------------------------------------------------------------------
    static void atananglerat(Ptr<RatPack.RAT> pa, RatPack.AngleType angletype, uint radix, int precision)
    {
        atanrat(pa, radix, precision);
        ascalerat(pa, angletype, precision);
    }

    static void _atanrat(Ptr<RatPack.RAT> px, int precision)
    {
        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret = RatPack.DUPRAT(px.deref());
        t.thisterm = RatPack.DUPRAT(px.deref());

        t.n2 = RatPack.DUPNUM(Support.Global.num_one);

        t.xx.pp.sign *= -1;

        do
        {
            t.NEXTTERM(t.xx, () -> {
                t.MULNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
            }, precision);
        } while (!t.thisterm.SMALL_ENOUGH_RAT(precision));

        px.set(t.RESULT());
    }

    static void atanrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Ptr<RatPack.RAT> tmpx = new Ptr<>();
        int sgn = px.deref().SIGN();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        if (Support.rat_gt(px.deref(), Support.Global.pt_eight_five, precision))
        {
            if (Support.rat_gt(px.deref(), Support.Global.rat_two, precision))
            {
                px.deref().pp.sign = sgn;
                px.deref().pq.sign = 1;
                tmpx.set(RatPack.DUPRAT(Support.Global.rat_one));
                Rat.divrat(tmpx, px.deref(), precision);
                _atanrat(tmpx, precision);
                tmpx.deref().pp.sign = sgn;
                tmpx.deref().pq.sign = 1;
                px.set(RatPack.DUPRAT(Support.Global.pi_over_two));
                Rat.subrat(px, tmpx.deref(), precision);
            }
            else
            {
                px.deref().pp.sign = sgn;
                tmpx.set(RatPack.DUPRAT(px.deref()));
                Rat.mulrat(tmpx, px.deref(), precision);
                Rat.addrat(tmpx, Support.Global.rat_one, precision);
                Rat.rootrat(tmpx, Support.Global.rat_two, radix, precision);
                Rat.divrat(px, tmpx.deref(), precision);
                asinrat(px, radix, precision);
                px.deref().pp.sign = sgn;
                px.deref().pq.sign = 1;
            }
        }
        else
        {
            px.deref().pp.sign = sgn;
            px.deref().pq.sign = 1;
            _atanrat(px, precision);
        }

        if (Support.rat_gt(px.deref(), Support.Global.pi_over_two, precision))
        {
            Rat.subrat(px, Support.Global.pi, precision);
        }
    }
}
