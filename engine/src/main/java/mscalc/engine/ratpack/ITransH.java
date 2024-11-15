package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;

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
    public static void asinhrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Ptr<RatPack.RAT> neg_pt_eight_five = new Ptr<>();

        neg_pt_eight_five.set(RatPack.DUPRAT(Support.Global.pt_eight_five));
        neg_pt_eight_five.deref().pp.sign *= -1;

        if (Support.rat_gt(px.deref(), Support.Global.pt_eight_five, precision) || Support.rat_lt(px.deref(), neg_pt_eight_five.deref(), precision))
        {
            Ptr<RatPack.RAT> ptmp = new Ptr<>(RatPack.DUPRAT(px.deref()));
            Rat.mulrat(ptmp, px.deref(), precision);
            Rat.addrat(ptmp, Support.Global.rat_one, precision);
            Rat.rootrat(ptmp, Support.Global.rat_two, radix, precision);
            Rat.addrat(px, ptmp.deref(), precision);
            Exp.lograt(px, precision);
        }
        else
        {
            RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);
            t.xx.pp.sign *= -1;

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
    public static void acoshrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        if (Support.rat_lt(px.deref(), Support.Global.rat_one, precision))
        {
            throw new ErrorCodeException(CalcErr.CALC_E_DOMAIN);
        }
        else
        {
            Ptr<RatPack.RAT> ptmp = new Ptr<>(RatPack.DUPRAT(px.deref()));
            Rat.mulrat(ptmp, px.deref(), precision);
            Rat.subrat(ptmp, Support.Global.rat_one, precision);
            Rat.rootrat(ptmp, Support.Global.rat_two, radix, precision);
            Rat.addrat(px, ptmp.deref(), precision);
            Exp.lograt(px, precision);
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
    public static void atanhrat(Ptr<RatPack.RAT> px, int precision)
    {
        Ptr<RatPack.RAT> ptmp = new Ptr<>(RatPack.DUPRAT(px.deref()));
        Rat.subrat(ptmp, Support.Global.rat_one, precision);
        Rat.addrat(px, Support.Global.rat_one, precision);
        Rat.divrat(px, ptmp.deref(), precision);
        px.deref().pp.sign *= -1;
        Exp.lograt(px, precision);
        Rat.divrat(px, Support.Global.rat_two, precision);
    }

}
