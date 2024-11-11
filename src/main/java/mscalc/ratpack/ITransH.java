package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.Exp.lograt;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.DUPNUM;
import static mscalc.ratpack.RatPack.DUPRAT;
import static mscalc.ratpack.Support.Global.*;
import static mscalc.ratpack.Support.rat_gt;
import static mscalc.ratpack.Support.rat_lt;

public class ITransH {
    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: asinhrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the inverse
    //    hyperbolic sine of
    //  RETURN: asinh of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___                                                   2 2
    //   \  ]                                           -(2j+1) X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j+2)*(2j+3)
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   For abs(x) < .85, and
    //
    //   asinh(x) = log(x+sqrt(x^2+1))
    //
    //   For abs(x) >= .85
    //
    //-----------------------------------------------------------------------------
    static void asinhrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> neg_pt_eight_five = new Ptr<>();

        neg_pt_eight_five.set(DUPRAT(pt_eight_five));
        neg_pt_eight_five.deref().pp.sign *= -1;

        if (rat_gt(px.deref(), pt_eight_five, precision) || rat_lt(px.deref(), neg_pt_eight_five.deref(), precision))
        {
            Ptr<RAT> ptmp = new Ptr<>(DUPRAT(px.deref()));
            mulrat(ptmp, px.deref(), precision);
            addrat(ptmp, rat_one, precision);
            rootrat(ptmp, rat_two, radix, precision);
            addrat(px, ptmp.deref(), precision);
            lograt(px, precision);
        }
        else
        {
            RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);
            t.xx.pp.sign *= -1;

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
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: acoshrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the inverse
    //    hyperbolic cose of
    //  RETURN: acosh of x in PRAT form.
    //
    //  EXPLANATION: This uses
    //
    //   acosh(x)=ln(x+sqrt(x^2-1))
    //
    //   For x >= 1
    //
    //-----------------------------------------------------------------------------
    static void acoshrat(Ptr<RAT> px, uint radix, int precision)
    {
        if (rat_lt(px.deref(), rat_one, precision))
        {
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }
        else
        {
            Ptr<RAT> ptmp = new Ptr<>(DUPRAT(px.deref()));
            mulrat(ptmp, px.deref(), precision);
            subrat(ptmp, rat_one, precision);
            rootrat(ptmp, rat_two, radix, precision);
            addrat(px, ptmp.deref(), precision);
            lograt(px, precision);
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: atanhrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the inverse
    //              hyperbolic tangent of
    //
    //  RETURN: atanh of x in PRAT form.
    //
    //  EXPLANATION: This uses
    //
    //             1     x+1
    //  atanh(x) = -*ln(----)
    //             2     x-1
    //
    //-----------------------------------------------------------------------------
    static void atanhrat(Ptr<RAT> px, int precision)
    {
        Ptr<RAT> ptmp = new Ptr<>(DUPRAT(px.deref()));
        subrat(ptmp, rat_one, precision);
        addrat(px, rat_one, precision);
        divrat(px, ptmp.deref(), precision);
        px.deref().pp.sign *= -1;
        lograt(px, precision);
        divrat(px, rat_two, precision);
    }

}
