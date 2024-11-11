package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.mulnumx;
import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.Exp.exprat;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.DUPNUM;
import static mscalc.ratpack.RatPack.DUPRAT;
import static mscalc.ratpack.Support.Global.*;
import static mscalc.ratpack.Support.rat_ge;
import static mscalc.ratpack.Support.rat_lt;

public class TransH {
    static boolean IsValidForHypFunc(RAT px, int precision)
    {
        Ptr<RAT> ptmp = new Ptr<>();
        boolean bRet = true;

        ptmp.set( DUPRAT(rat_min_exp) );
        divrat(ptmp, rat_ten, precision);
        if (rat_lt(px, ptmp.deref(), precision))
        {
            bRet = false;
        }
        return bRet;
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: sinhrat, _sinhrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the sine hyperbolic
    //    of
    //  RETURN: sinh of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___    2j+1
    //   \  ]  X
    //    \   ---------
    //    /    (2j+1)!
    //   /__]
    //   j=0
    //          or,
    //    n
    //   ___                                                 2
    //   \  ]                                               X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j)*(2j+1)
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   if x is bigger than 1.0 (e^x-e^-x)/2 is used.
    //
    //-----------------------------------------------------------------------------
    static void _sinhrat(Ptr<RAT> px, int precision)
    {
        if (!IsValidForHypFunc(px.deref(), precision))
        {
            // Don't attempt exp of anything large or small
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }

        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret = DUPRAT(px.deref());
        t.thisterm = DUPRAT(t.pret);

        t.n2 = DUPNUM(num_one);

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
    }

    static void sinhrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> tmpx = new Ptr<>();

        if (rat_ge(px.deref(), rat_one, precision))
        {
            tmpx.set(DUPRAT(px.deref()));
            exprat(px, radix, precision);
            tmpx.deref().pp.sign *= -1;
            exprat(tmpx, radix, precision);
            subrat(px, tmpx.deref(), precision);
            divrat(px, rat_two, precision);
        }
        else
        {
            _sinhrat(px, precision);
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: coshrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the cosine
    //              hyperbolic of
    //
    //  RETURN: cosh  of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___    2j
    //   \  ]  X
    //    \   ---------
    //    /    (2j)!
    //   /__]
    //   j=0
    //          or,
    //    n
    //   ___                                                 2
    //   \  ]                                               X
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j   (2j)*(2j+1)
    //   /__]
    //   j=0
    //
    //   thisterm  = 1 ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   if x is bigger than 1.0 (e^x+e^-x)/2 is used.
    //
    //-----------------------------------------------------------------------------
    static void _coshrat(Ptr<RAT> px, uint radix, int precision)
    {
        if (!IsValidForHypFunc(px.deref(), precision))
        {
            // Don't attempt exp of anything large or small
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }

        RatPack.TYLOR t = new RatPack.TYLOR(px.deref(), precision);

        t.pret.pp = i32tonum(1, radix);
        t.pret.pq = i32tonum(1, radix);

        t.thisterm = DUPRAT(t.pret);

        t.n2 = i32tonum(0, radix);

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
    }

    static void coshrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> tmpx = new Ptr<>();

        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        if (rat_ge(px.deref(), rat_one, precision))
        {
            tmpx.set(DUPRAT(px.deref()));
            exprat(px, radix, precision);
            tmpx.deref().pp.sign *= -1;
            exprat(tmpx, radix, precision);
            addrat(px, tmpx.deref(), precision);
            divrat(px, rat_two, precision);
        }
        else
        {
            _coshrat(px, radix, precision);
        }
        // Since *px might be epsilon below 1 due to TRIMIT
        // we need this trick here.
        if (rat_lt(px.deref(), rat_one, precision))
        {
            px.set(DUPRAT(rat_one));
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: tanhrat
    //
    //  ARGUMENTS:  x PRAT representation of number to take the tangent
    //              hyperbolic of
    //
    //  RETURN: tanh    of x in PRAT form.
    //
    //  EXPLANATION: This uses sinhrat and coshrat
    //
    //-----------------------------------------------------------------------------
    static void tanhrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> ptmp = new Ptr<>();
        ptmp.set(DUPRAT(px.deref()));

        sinhrat(px, radix, precision);
        coshrat(ptmp, radix, precision);

        px.deref().ppPtr(ppp -> mulnumx(ppp, ptmp.deref().pq));
        px.deref().pqPtr(ppq -> mulnumx(ppq, ptmp.deref().pp));
    }
}


