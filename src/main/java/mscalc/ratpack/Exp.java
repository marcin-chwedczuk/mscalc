package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;
import mscalc.ratpack.RatPack.TYLOR;

import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.Num.addnum;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.*;
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
        TYLOR t = new TYLOR(px.deref(), precision);

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

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: lograt, _lograt
    //
    //  ARGUMENTS: x PRAT representation of number to logarithim
    //
    //  RETURN: log  of x in PRAT form.
    //
    //  EXPLANATION: This uses Taylor series
    //
    //    n
    //   ___
    //   \  ]                                             j*(1-X)
    //    \   thisterm  ; where thisterm   = thisterm  * ---------
    //    /           j                 j+1          j      j+1
    //   /__]
    //   j=0
    //
    //   thisterm  = X ;  and stop when thisterm < precision used.
    //           0                              n
    //
    //   Number is scaled between one and e_to_one_half prior to taking the
    //   log. This is to keep execution time from exploding.
    //
    //
    //-----------------------------------------------------------------------------
    static void _lograt(Ptr<RAT> px, int precision)
    {
        TYLOR t = new TYLOR(px.deref(), precision);

        t.thisterm = createrat();

        // sub one from x
        px.deref().pq.sign *= -1;
        px.deref().ppPtr(ppp -> addnum(ppp, px.deref().pq, BASEX));
        px.deref().pq.sign *= -1;

        t.pret = DUPRAT(px.deref());
        t.thisterm = DUPRAT(px.deref());

        t.n2 = i32tonum(1, BASEX);
        px.deref().pp.sign *= -1;

        do
        {
            t.NEXTTERM(px.deref(), () -> {
                t.MULNUM(t.n2);
                t.n2 = t.INC(t.n2);
                t.DIVNUM(t.n2);
            }, precision);

            px.deref().TRIMTOP(precision);
        } while (!t.thisterm.SMALL_ENOUGH_RAT(precision));

        px.set( t.RESULT() );
    }

    static void lograt(Ptr<RAT> px, int precision)
    {
        Ptr<RAT> pwr = new Ptr<>();    // pwr is the large scaling factor.
        Ptr<RAT> offset = new Ptr<>(); // offset is the incremental scaling factor.

        // Check for someone taking the log of zero or a negative number.
        if (rat_le(px.deref(), rat_zero, precision))
        {
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }

        // Get number > 1, for scaling
        boolean fneglog = rat_lt(px.deref(), rat_one, precision);
        if (fneglog)
        {
            NUMBER pnumtemp = px.deref().pp;
            px.deref().pp = px.deref().pq;
            px.deref().pq = pnumtemp;
        }

        // Scale the number within BASEX factor of 1, for the large scale.
        // log(x*2^(BASEXPWR*k)) = BASEXPWR*k*log(2)+log(x)
        if (px.deref().LOGRAT2() > 1)
        {
            final int intpwr = px.deref().LOGRAT2() - 1;
            px.deref().pq.exp += intpwr;
            pwr.set( i32torat(intpwr * BASEXPWR) );
            mulrat(pwr, ln_two, precision);
            // ln(x+e)-ln(x) looks close to e when x is close to one using some
            // expansions.  This means we can trim past precision digits+1.
            px.deref().TRIMTOP(precision);
        }
        else
        {
            pwr.set( DUPRAT(rat_zero) );
        }

        offset.set( DUPRAT(rat_zero) );
        // Scale the number between 1 and e_to_one_half, for the small scale.
        while (rat_gt(px.deref(), e_to_one_half, precision))
        {
            divrat(px, e_to_one_half, precision);
            addrat(offset, rat_one, precision);
        }

        _lograt(px, precision);

        // Add the large and small scaling factors, take into account
        // small scaling was done in e_to_one_half chunks.
        divrat(offset, rat_two, precision);
        addrat(pwr, offset.deref(), precision);

        // And add the resulting scaling factor to the answer.
        addrat(px, pwr.deref(), precision);

        trimit(px, precision);

        // If number started out < 1 rescale answer to negative.
        if (fneglog)
        {
            px.deref().pp.sign *= -1;
        }
    }

    static void log10rat(Ptr<RAT> px, int precision)
    {
        lograt(px, precision);
        divrat(px, ln_ten, precision);
    }

    //
    // return if the given x is even number. The assumption here is its denominator is 1 and we are testing the numerator is
    // even or not
    static boolean IsEven(RAT x, uint radix, int precision)
    {
        Ptr<RAT> tmp = new Ptr<>();
        boolean bRet = false;

        tmp.set( DUPRAT(x) );
        divrat(tmp, rat_two, precision);
        fracrat(tmp, radix, precision);
        addrat(tmp, tmp.deref(), precision);
        subrat(tmp, rat_one, precision);
        if (rat_lt(tmp.deref(), rat_zero, precision))
        {
            bRet = true;
        }

        return bRet;
    }

}
