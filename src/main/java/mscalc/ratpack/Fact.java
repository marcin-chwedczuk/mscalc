package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.mulnumx;
import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.CalcErr.CALC_E_OVERFLOW;
import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.Exp.*;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.*;
import static mscalc.ratpack.Support.*;
import static mscalc.ratpack.Support.Global.*;

public class Fact {
    static void ABSRAT(RAT x) {
        x.pp.sign = 1;
        x.pq.sign = 1;
    }

    static void NEGATE(RAT x) {
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
    static void _gamma(Ptr<RAT> pn, uint radix, int precision)
    {
        Ptr<RAT> factorial = new Ptr<>();
        NUMBER count = null;
        Ptr<RAT> tmp = new Ptr<>();
        Ptr<RAT> one_pt_five = new Ptr<>();
        Ptr<RAT> a = new Ptr<>();
        Ptr<RAT> a2 = new Ptr<>();
        Ptr<RAT> term = new Ptr<>();
        Ptr<RAT> sum = new Ptr<>();
        Ptr<RAT> err = new Ptr<>();
        Ptr<RAT> mpy = new Ptr<>();

        // Set up constants and initial conditions
        RAT ratprec = i32torat(precision);

        // Find the best 'A' for convergence to the required precision.
        a.set( i32torat(radix.toInt()) );
        lograt(a, precision);
        mulrat(a, ratprec, precision);

        // Really is -ln(n)+1, but -ln(n) will be < 1
        // if we scale n between 0.5 and 1.5
        addrat(a, rat_two, precision);
        tmp.set(DUPRAT(a.deref()));
        lograt(tmp, precision);
        mulrat(tmp, pn.deref(), precision);
        addrat(a, tmp.deref(), precision);
        addrat(a, rat_one, precision);

        // Calculate the necessary bump in precision and up the precision.
        // The following code is equivalent to
        // precision += ln(exp(a)*pow(a,n+1.5))-ln(radix));
        tmp.set( DUPRAT(pn.deref()) );
        one_pt_five.set( i32torat(3) );
        divrat(one_pt_five, rat_two, precision);
        addrat(tmp, one_pt_five.deref(), precision);
        term.set( DUPRAT(a.deref()) );
        powratcomp(term, tmp.deref(), radix, precision);
        tmp.set( DUPRAT(a.deref()) );
        exprat(tmp, radix, precision);
        mulrat(term, tmp.deref(), precision);
        lograt(term, precision);

        RAT ratRadix = i32torat(radix.toInt());
        tmp.set(DUPRAT(ratRadix));
        lograt(tmp, precision);
        subrat(term, tmp.deref(), precision);
        precision += rattoi32(term.deref(), radix, precision);

        // Set up initial terms for series, refer to series in above comment block.
        factorial.set( DUPRAT(rat_one) ); // Start factorial out with one
        count = i32tonum(0, BASEX);

        mpy.set(DUPRAT(a.deref()));
        powratcomp(mpy, pn.deref(), radix, precision);
        // a2=a^2
        a2.set(DUPRAT(a.deref()));
        mulrat(a2, a.deref(), precision);

        // sum=(1/n)-(a/(n+1))
        sum.set(DUPRAT(rat_one));
        divrat(sum, pn.deref(), precision);
        tmp.set(DUPRAT(pn.deref()));
        addrat(tmp, rat_one, precision);
        term.set(DUPRAT(a.deref()));
        divrat(term, tmp.deref(), precision);
        subrat(sum, term.deref(), precision);

        err.set(DUPRAT(ratRadix));
        NEGATE(ratprec);
        powratcomp(err, ratprec, radix, precision);
        divrat(err, ratRadix, precision);

        // Just get something not tiny in term
        term.set(DUPRAT(rat_two));

        // Loop until precision is reached, or asked to halt.
        while (!zerrat(term.deref()) && rat_gt(term.deref(), err.deref(), precision))
        {
            addrat(pn, rat_two, precision);

            // WARNING: mixing numbers and  rationals here.
            // for speed and efficiency.
            RatPack.TYLOR.INC(count);
            final var lambdaCount = count; // variable must be final when ref inside lambda
            factorial.deref().ppPtr(ppp -> mulnumx(ppp, lambdaCount));
            RatPack.TYLOR.INC(count);
            factorial.deref().ppPtr(ppp -> mulnumx(ppp, lambdaCount));

            divrat(factorial, a2.deref(), precision);

            tmp.set( DUPRAT(pn.deref()) );
            addrat(tmp, rat_one, precision);

            term.set( createrat() );
            term.deref().pp = DUPNUM(count);
            term.deref().pq = DUPNUM(num_one);
            addrat(term, rat_one, precision);
            mulrat(term, tmp.deref(), precision);
            tmp.set(DUPRAT(a.deref()));
            divrat(tmp, term.deref(), precision);

            term.set(DUPRAT(rat_one));
            divrat(term, pn.deref(), precision);
            subrat(term, tmp.deref(), precision);

            divrat(term, factorial.deref(), precision);
            addrat(sum, term.deref(), precision);
            ABSRAT(term.deref());
        }

        // Multiply by factor.
        mulrat(sum, mpy.deref(), precision);

        pn.set(DUPRAT(sum.deref()));
    }

    static void factrat(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> fact = new Ptr<>();
        Ptr<RAT> frac = new Ptr<>();
        Ptr<RAT> neg_rat_one = new Ptr<>();

        if (rat_gt(px.deref(), rat_max_fact, precision) || rat_lt(px.deref(), rat_min_fact, precision))
        {
            // Don't attempt factorial of anything too large or small.
            throw new ErrorCodeException(CALC_E_OVERFLOW);
        }

        fact.set(DUPRAT(rat_one));

        neg_rat_one.set(DUPRAT(rat_one));
        neg_rat_one.deref().pp.sign *= -1;

        frac.set(DUPRAT(px.deref()));
        fracrat(frac, radix, precision);

        // Check for negative integers and throw an error.
        if ((zerrat(frac.deref()) || (frac.deref().LOGRATRADIX() <= -precision)) && (px.deref().SIGN() == -1))
        {
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }
        while (rat_gt(px.deref(), rat_zero, precision) && (px.deref().LOGRATRADIX() > -precision))
        {
            mulrat(fact, px.deref(), precision);
            subrat(px, rat_one, precision);
        }

        // Added to make numbers 'close enough' to integers use integer factorial.
        if (px.deref().LOGRATRADIX() <= -precision)
        {
            px.set(DUPRAT(rat_zero));
            intrat(fact, radix, precision);
        }

        while (rat_lt(px.deref(), neg_rat_one.deref(), precision))
        {
            addrat(px, rat_one, precision);
            divrat(fact, px.deref(), precision);
        }

        if (rat_neq(px.deref(), rat_zero, precision))
        {
            addrat(px, rat_one, precision);
            _gamma(px, radix, precision);
            mulrat(px, fact.deref(), precision);
        }
        else
        {
            px.set(DUPRAT(fact.deref()));
        }
    }
}
