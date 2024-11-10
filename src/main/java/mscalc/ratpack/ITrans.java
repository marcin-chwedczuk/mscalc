package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.AngleType;
import mscalc.ratpack.RatPack.RAT;
import mscalc.ratpack.RatPack.TYLOR;

import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.Conv.createrat;
import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.*;
import static mscalc.ratpack.Support.*;
import static mscalc.ratpack.Support.Global.*;

public interface ITrans {
    static void ascalerat(Ptr<RAT> pa, AngleType angletype, int precision)
    {
        switch (angletype)
        {
            case AngleType.Radians:
                break;
            case AngleType.Degrees:
                divrat(pa, two_pi, precision);
                mulrat(pa, rat_360, precision);
                break;
            case AngleType.Gradians:
                divrat(pa, two_pi, precision);
                mulrat(pa, rat_400, precision);
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
    static void _asinrat(Ptr<RAT> px, int precision)
    {
        TYLOR t = new TYLOR(px.deref(), precision);

        t.pret = DUPRAT(px.deref());
        t.thisterm = DUPRAT(px.deref());
        t.n2 = DUPNUM(num_one);

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

    static void asinanglerat(Ptr<RAT> pa, AngleType angletype, uint radix, int precision)
    {
        asinrat(pa, radix, precision);
        ascalerat(pa, angletype, precision);
    }

    static void asinrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> pret = new Ptr<>();
        Ptr<RAT> phack = new Ptr<>();
        int sgn = px.deref().SIGN();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        // Avoid the really bad part of the asin curve near +/-1.
        phack.set(DUPRAT(px.deref()));
        subrat(phack, rat_one, precision);
        // Since *px might be epsilon near zero we must set it to zero.
        if (rat_le(phack.deref(), rat_smallest, precision) && rat_ge(phack.deref(), rat_negsmallest, precision))
        {
            px.set(DUPRAT(pi_over_two));
        }
        else
        {
            if (rat_gt(px.deref(), pt_eight_five, precision))
            {
                if (rat_gt(px.deref(), rat_one, precision))
                {
                    subrat(px, rat_one, precision);
                    if (rat_gt(px.deref(), rat_smallest, precision))
                    {
                        throw new ErrorCodeException(CALC_E_DOMAIN);
                    }
                    else
                    {
                        px.set(DUPRAT(rat_one));
                    }
                }
                pret.set(DUPRAT(px.deref()));
                mulrat(px, pret.deref(), precision);
                px.deref().pp.sign *= -1;
                addrat(px, rat_one, precision);
                rootrat(px, rat_two, radix, precision);
                _asinrat(px, precision);
                px.deref().pp.sign *= -1;
                addrat(px, pi_over_two, precision);
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
    static void acosanglerat(Ptr<RAT> pa, AngleType angletype, uint radix, int precision)
    {
        acosrat(pa, radix, precision);
        ascalerat(pa, angletype, precision);
    }

    static void _acosrat(Ptr<RAT> px, int precision)
    {
        TYLOR t = new TYLOR(px.deref(), precision);
        t.thisterm = createrat();
        t.thisterm.pp = i32tonum(1, BASEX);
        t.thisterm.pq = i32tonum(1, BASEX);

        t.n2 = DUPNUM(num_one);

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

    static void acosrat(Ptr<RAT> px, uint radix, int precision)
    {
        int sgn = px.deref().SIGN();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        if (rat_equ(px.deref(), rat_one, precision))
        {
            if (sgn == -1)
            {
                px.set(DUPRAT(pi));
            }
            else
            {
                px.set(DUPRAT(rat_zero));
            }
        }
        else
        {
            px.deref().pp.sign = sgn;
            asinrat(px, radix, precision);
            px.deref().pp.sign *= -1;
            addrat(px, pi_over_two, precision);
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
    static void atananglerat(Ptr<RAT> pa, AngleType angletype, uint radix, int precision)
    {
        atanrat(pa, radix, precision);
        ascalerat(pa, angletype, precision);
    }

    static void _atanrat(Ptr<RAT> px, int precision)
    {
        TYLOR t = new TYLOR(px.deref(), precision);

        t.pret = DUPRAT(px.deref());
        t.thisterm = DUPRAT(px.deref());

        t.n2 = DUPNUM(num_one);

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

    static void atanrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> tmpx = new Ptr<>();
        int sgn = px.deref().SIGN();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        if (rat_gt(px.deref(), pt_eight_five, precision))
        {
            if (rat_gt(px.deref(), rat_two, precision))
            {
                px.deref().pp.sign = sgn;
                px.deref().pq.sign = 1;
                tmpx.set(DUPRAT(rat_one));
                divrat(tmpx, px.deref(), precision);
                _atanrat(tmpx, precision);
                tmpx.deref().pp.sign = sgn;
                tmpx.deref().pq.sign = 1;
                px.set(DUPRAT(pi_over_two));
                subrat(px, tmpx.deref(), precision);
            }
            else
            {
                px.deref().pp.sign = sgn;
                tmpx.set(DUPRAT(px.deref()));
                mulrat(tmpx, px.deref(), precision);
                addrat(tmpx, rat_one, precision);
                rootrat(tmpx, rat_two, radix, precision);
                divrat(px, tmpx.deref(), precision);
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

        if (rat_gt(px.deref(), pi_over_two, precision))
        {
            subrat(px, pi, precision);
        }
    }
}
