package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;

public class Fact {
    static void ABSRAT(RatPack.RAT x) {
        x.pp.sign = 1;
        x.pq.sign = 1;
    }

    static void NEGATE(RatPack.RAT x) {
        x.pp.sign *= -1;
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: factrat, _gamma, gamma
    //
    //  ARGUMENTS:  x PRAT representation of number to take the sine of
    //
    //  RETURN: factorial of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //      n
    //     ___    2j
    //   n \  ]  A       1          A
    //  A   \   -----[ ---- - ---------------]
    //      /   (2j)!  n+2j   (n+2j+1)(2j+1)
    //     /__]
    //     j=0
    //
    //                        / oo
    //                        |    n-1 -x     __
    //  This was derived from |   x   e  dx = |
    //                        |               | (n) { = (n-1)! for +integers}
    //                        / 0
    //
    //  It can be shown that the above series is within precision if A is chosen
    //  big enough.
    //                          A    n  precision
    //  Based on the relation ne  = A 10            A was chosen as
    //
    //             precision
    //  A = ln(Base         /n)+1
    //  A += n*ln(A)  This is close enough for precision > base and n < 1.5
    //
    //
    //-----------------------------------------------------------------------------
    static void _gamma(Ptr<RatPack.RAT> pn, uint radix, int precision)
    {
        Ptr<RatPack.RAT> factorial = new Ptr<>();
        RatPack.NUMBER count = null;
        Ptr<RatPack.RAT> tmp = new Ptr<>();
        Ptr<RatPack.RAT> one_pt_five = new Ptr<>();
        Ptr<RatPack.RAT> a = new Ptr<>();
        Ptr<RatPack.RAT> a2 = new Ptr<>();
        Ptr<RatPack.RAT> term = new Ptr<>();
        Ptr<RatPack.RAT> sum = new Ptr<>();
        Ptr<RatPack.RAT> err = new Ptr<>();
        Ptr<RatPack.RAT> mpy = new Ptr<>();

        // Set up constants and initial conditions
        RatPack.RAT ratprec = Conv.i32torat(precision);

        // Find the best 'A' for convergence to the required precision.
        a.set( Conv.i32torat(radix.toInt()) );
        Exp.lograt(a, precision);
        Rat.mulrat(a, ratprec, precision);

        // Really is -ln(n)+1, but -ln(n) will be < 1
        // if we scale n between 0.5 and 1.5
        Rat.addrat(a, Support.Global.rat_two, precision);
        tmp.set(RatPack.DUPRAT(a.deref()));
        Exp.lograt(tmp, precision);
        Rat.mulrat(tmp, pn.deref(), precision);
        Rat.addrat(a, tmp.deref(), precision);
        Rat.addrat(a, Support.Global.rat_one, precision);

        // Calculate the necessary bump in precision and up the precision.
        // The following code is equivalent to
        // precision += ln(exp(a)*pow(a,n+1.5))-ln(radix));
        tmp.set( RatPack.DUPRAT(pn.deref()) );
        one_pt_five.set( Conv.i32torat(3) );
        Rat.divrat(one_pt_five, Support.Global.rat_two, precision);
        Rat.addrat(tmp, one_pt_five.deref(), precision);
        term.set( RatPack.DUPRAT(a.deref()) );
        Exp.powratcomp(term, tmp.deref(), radix, precision);
        tmp.set( RatPack.DUPRAT(a.deref()) );
        Exp.exprat(tmp, radix, precision);
        Rat.mulrat(term, tmp.deref(), precision);
        Exp.lograt(term, precision);

        RatPack.RAT ratRadix = Conv.i32torat(radix.toInt());
        tmp.set(RatPack.DUPRAT(ratRadix));
        Exp.lograt(tmp, precision);
        Rat.subrat(term, tmp.deref(), precision);
        precision += Conv.rattoi32(term.deref(), radix, precision);

        // Set up initial terms for series, refer to series in above comment block.
        factorial.set( RatPack.DUPRAT(Support.Global.rat_one) ); // Start factorial out with one
        count = Conv.i32tonum(0, RatPack.BASEX);

        mpy.set(RatPack.DUPRAT(a.deref()));
        Exp.powratcomp(mpy, pn.deref(), radix, precision);
        // a2=a^2
        a2.set(RatPack.DUPRAT(a.deref()));
        Rat.mulrat(a2, a.deref(), precision);

        // sum=(1/n)-(a/(n+1))
        sum.set(RatPack.DUPRAT(Support.Global.rat_one));
        Rat.divrat(sum, pn.deref(), precision);
        tmp.set(RatPack.DUPRAT(pn.deref()));
        Rat.addrat(tmp, Support.Global.rat_one, precision);
        term.set(RatPack.DUPRAT(a.deref()));
        Rat.divrat(term, tmp.deref(), precision);
        Rat.subrat(sum, term.deref(), precision);

        err.set(RatPack.DUPRAT(ratRadix));
        NEGATE(ratprec);
        Exp.powratcomp(err, ratprec, radix, precision);
        Rat.divrat(err, ratRadix, precision);

        // Just get something not tiny in term
        term.set(RatPack.DUPRAT(Support.Global.rat_two));

        // Loop until precision is reached, or asked to halt.
        while (!Rat.zerrat(term.deref()) && Support.rat_gt(term.deref(), err.deref(), precision))
        {
            Rat.addrat(pn, Support.Global.rat_two, precision);

            // WARNING: mixing numbers and  rationals here.
            // for speed and efficiency.
            RatPack.TYLOR.INC(count);
            final var lambdaCount = count; // variable must be final when ref inside lambda
            factorial.deref().ppPtr(ppp -> BaseX.mulnumx(ppp, lambdaCount));
            RatPack.TYLOR.INC(count);
            factorial.deref().ppPtr(ppp -> BaseX.mulnumx(ppp, lambdaCount));

            Rat.divrat(factorial, a2.deref(), precision);

            tmp.set( RatPack.DUPRAT(pn.deref()) );
            Rat.addrat(tmp, Support.Global.rat_one, precision);

            term.set( Conv.createrat() );
            term.deref().pp = RatPack.DUPNUM(count);
            term.deref().pq = RatPack.DUPNUM(Support.Global.num_one);
            Rat.addrat(term, Support.Global.rat_one, precision);
            Rat.mulrat(term, tmp.deref(), precision);
            tmp.set(RatPack.DUPRAT(a.deref()));
            Rat.divrat(tmp, term.deref(), precision);

            term.set(RatPack.DUPRAT(Support.Global.rat_one));
            Rat.divrat(term, pn.deref(), precision);
            Rat.subrat(term, tmp.deref(), precision);

            Rat.divrat(term, factorial.deref(), precision);
            Rat.addrat(sum, term.deref(), precision);
            ABSRAT(term.deref());
        }

        // Multiply by factor.
        Rat.mulrat(sum, mpy.deref(), precision);

        pn.set(RatPack.DUPRAT(sum.deref()));
    }

    public static void factrat(Ptr<RatPack.RAT> px, uint radix, int precision)
    {
        Ptr<RatPack.RAT> fact = new Ptr<>();
        Ptr<RatPack.RAT> frac = new Ptr<>();
        Ptr<RatPack.RAT> neg_rat_one = new Ptr<>();

        if (Support.rat_gt(px.deref(), Support.Global.rat_max_fact, precision) || Support.rat_lt(px.deref(), Support.Global.rat_min_fact, precision))
        {
            // Don't attempt factorial of anything too large or small.
            throw new ErrorCodeException(CalcErr.CALC_E_OVERFLOW);
        }

        fact.set(RatPack.DUPRAT(Support.Global.rat_one));

        neg_rat_one.set(RatPack.DUPRAT(Support.Global.rat_one));
        neg_rat_one.deref().pp.sign *= -1;

        frac.set(RatPack.DUPRAT(px.deref()));
        Rat.fracrat(frac, radix, precision);

        // Check for negative integers and throw an error.
        if ((Rat.zerrat(frac.deref()) || (frac.deref().LOGRATRADIX() <= -precision)) && (px.deref().SIGN() == -1))
        {
            throw new ErrorCodeException(CalcErr.CALC_E_DOMAIN);
        }
        while (Support.rat_gt(px.deref(), Support.Global.rat_zero, precision) && (px.deref().LOGRATRADIX() > -precision))
        {
            Rat.mulrat(fact, px.deref(), precision);
            Rat.subrat(px, Support.Global.rat_one, precision);
        }

        // Added to make numbers 'close enough' to integers use integer factorial.
        if (px.deref().LOGRATRADIX() <= -precision)
        {
            px.set(RatPack.DUPRAT(Support.Global.rat_zero));
            Support.intrat(fact, radix, precision);
        }

        while (Support.rat_lt(px.deref(), neg_rat_one.deref(), precision))
        {
            Rat.addrat(px, Support.Global.rat_one, precision);
            Rat.divrat(fact, px.deref(), precision);
        }

        if (Support.rat_neq(px.deref(), Support.Global.rat_zero, precision))
        {
            Rat.addrat(px, Support.Global.rat_one, precision);
            _gamma(px, radix, precision);
            Rat.mulrat(px, fact.deref(), precision);
        }
        else
        {
            px.set(RatPack.DUPRAT(fact.deref()));
        }
    }
}
