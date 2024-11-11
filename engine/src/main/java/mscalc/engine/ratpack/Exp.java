package mscalc.engine.ratpack;

import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.NUMBER;
import mscalc.engine.ratpack.RatPack.RAT;
import mscalc.engine.ratpack.RatPack.TYLOR;

import static mscalc.engine.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.engine.ratpack.Num.addnum;
import static mscalc.engine.ratpack.RatPack.*;
import static mscalc.engine.ratpack.Support.*;
import static mscalc.engine.ratpack.Support.Global.*;

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
        t.n2 = Conv.i32tonum(0, radix);

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

        final int intpwr = Conv.rattoi32(pint.deref(), radix, precision);
        Conv.ratpowi32(pwr, intpwr, precision);

        Rat.subrat(px, pint.deref(), precision);

        // It just so happens to be an integral power of e.
        if (rat_gt(px.deref(), rat_negsmallest, precision) && rat_lt(px.deref(), rat_smallest, precision))
        {
            px.set(DUPRAT(pwr.deref()));
        }
        else
        {
            _exprat(px, radix, precision);
            Rat.mulrat(px, pwr.deref(), precision);
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

        t.thisterm = Conv.createrat();

        // sub one from x
        px.deref().pq.sign *= -1;
        px.deref().ppPtr(ppp -> addnum(ppp, px.deref().pq, BASEX));
        px.deref().pq.sign *= -1;

        t.pret = DUPRAT(px.deref());
        t.thisterm = DUPRAT(px.deref());

        t.n2 = Conv.i32tonum(1, BASEX);
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
            pwr.set( Conv.i32torat(intpwr * BASEXPWR) );
            Rat.mulrat(pwr, ln_two, precision);
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
            Rat.divrat(px, e_to_one_half, precision);
            Rat.addrat(offset, rat_one, precision);
        }

        _lograt(px, precision);

        // Add the large and small scaling factors, take into account
        // small scaling was done in e_to_one_half chunks.
        Rat.divrat(offset, rat_two, precision);
        Rat.addrat(pwr, offset.deref(), precision);

        // And add the resulting scaling factor to the answer.
        Rat.addrat(px, pwr.deref(), precision);

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
        Rat.divrat(px, ln_ten, precision);
    }

    //
    // return if the given x is even number. The assumption here is its denominator is 1 and we are testing the numerator is
    // even or not
    static boolean IsEven(RAT x, uint radix, int precision)
    {
        Ptr<RAT> tmp = new Ptr<>();
        boolean bRet = false;

        tmp.set( DUPRAT(x) );
        Rat.divrat(tmp, rat_two, precision);
        Rat.fracrat(tmp, radix, precision);
        Rat.addrat(tmp, tmp.deref(), precision);
        Rat.subrat(tmp, rat_one, precision);
        if (rat_lt(tmp.deref(), rat_zero, precision))
        {
            bRet = true;
        }

        return bRet;
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: powrat
    //
    //  ARGUMENTS: PRAT *px, PRAT y, uint32_t radix, int32_t precision
    //
    //  RETURN: none, sets *px to *px to the y.
    //
    //  EXPLANATION: Calculates the power of both px and
    //  handles special cases where px is a perfect root.
    //  Assumes, all checking has been done on validity of numbers.
    //
    //
    //---------------------------------------------------------------------------
    static void powrat(Ptr<RAT> px, RAT y, uint radix, int precision)
    {
        // Handle cases where px or y is 0 by calling powratcomp directly
        if (Rat.zerrat(px.deref()) || Rat.zerrat(y))
        {
            powratcomp(px, y, radix, precision);
            return;
        }
        // When y is 1, return px
        if (rat_equ(y, rat_one, precision))
        {
            return;
        }

        try
        {
            powratNumeratorDenominator(px, y, radix, precision);
        }
        catch (ErrorCodeException e)
        {
            e.printStackTrace();

            // If calculating the power using numerator/denominator
            // failed, fall back to the less accurate method of
            // passing in the original y
            powratcomp(px, y, radix, precision);
        }
    }

    static void powratNumeratorDenominator(Ptr<RAT> px, RAT y, uint radix, int precision)
    {
        // Prepare rationals
        Ptr<RAT> yNumerator = new Ptr<>();
        Ptr<RAT> yDenominator = new Ptr<>();
        yNumerator.set( DUPRAT(rat_zero) );   // yNumerator->pq is 1 one
        yDenominator.set( DUPRAT(rat_zero) ); // yDenominator->pq is 1 one
        yNumerator.deref().pp = DUPNUM(y.pp);
        yDenominator.deref().pp = DUPNUM(y.pq);

        // Calculate the following use the Powers of Powers rule:
        // px ^ (yNum/yDenom) == px ^ yNum ^ (1/yDenom)
        // 1. For px ^ yNum, we call powratcomp directly which will call ratpowi32
        //    and store the result in pxPowNum
        // 2. For pxPowNum ^ (1/yDenom), we call powratcomp
        // 3. Validate the result of 2 by adding/subtracting 0.5, flooring and call powratcomp with yDenom
        //    on the floored result.

        // 1. Initialize result.
        Ptr<RAT> pxPow = new Ptr<>(DUPRAT(px.deref()));

        // 2. Calculate pxPow = px ^ yNumerator
        // if yNumerator is not 1
        if (!rat_equ(yNumerator.deref(), rat_one, precision))
        {
            powratcomp(pxPow, yNumerator.deref(), radix, precision);
        }

        // 2. Calculate pxPowNumDenom = pxPowNum ^ (1/yDenominator),
        // if yDenominator is not 1
        if (!rat_equ(yDenominator.deref(), rat_one, precision))
        {
            // Calculate 1 over y
            Ptr<RAT> oneoveryDenom = new Ptr<>( DUPRAT(rat_one) );
            Rat.divrat(oneoveryDenom, yDenominator.deref(), precision);

            // ##################################
            // Take the oneoveryDenom power
            // ##################################
            Ptr<RAT> originalResult = new Ptr<>(DUPRAT(pxPow.deref()));
            powratcomp(originalResult, oneoveryDenom.deref(), radix, precision);

            // ##################################
            // Round the originalResult to roundedResult
            // ##################################
            Ptr<RAT> roundedResult = new Ptr<>(DUPRAT(originalResult.deref()));
            if (roundedResult.deref().pp.sign == -1)
            {
                Rat.subrat(roundedResult, rat_half, precision);
            }
            else
            {
                Rat.addrat(roundedResult, rat_half, precision);
            }
            intrat(roundedResult, radix, precision);

            // ##################################
            // Take the yDenom power of the roundedResult.
            // ##################################
            Ptr<RAT> roundedPower = new Ptr<>(DUPRAT(roundedResult.deref()));
            powratcomp(roundedPower, yDenominator.deref(), radix, precision);

            // ##################################
            // if roundedPower == px,
            // we found an exact power in roundedResult
            // ##################################
            if (rat_equ(roundedPower.deref(), pxPow.deref(), precision))
            {
                px.set(DUPRAT(roundedResult.deref()));
            }
            else
            {
                px.set(DUPRAT(originalResult.deref()));
            }
        }
        else
        {
            px.set(DUPRAT(pxPow.deref()));
        }
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: powratcomp
    //
    //  ARGUMENTS: PRAT *px, and PRAT y
    //
    //  RETURN: none, sets *px to *px to the y.
    //
    //  EXPLANATION: This uses x^y=e(y*ln(x)), or a more exact calculation where
    //  y is an integer.
    //  Assumes, all checking has been done on validity of numbers.
    //
    //
    //---------------------------------------------------------------------------
    static void powratcomp(Ptr<RAT> px, RAT y, uint radix, int precision)
    {
        int sign = px.deref().SIGN();

        // Take the absolute value
        px.deref().pp.sign = 1;
        px.deref().pq.sign = 1;

        if (Rat.zerrat(px.deref()))
        {
            // *px is zero.
            if (rat_lt(y, rat_zero, precision))
            {
                throw new ErrorCodeException(CALC_E_DOMAIN);
            }
            else if (Rat.zerrat(y))
            {
                // *px and y are both zero, special case a 1 return.
                px.set( DUPRAT(rat_one) );
                // Ensure sign is positive.
                sign = 1;
            }
        }
        else
        {
            Ptr<RAT> pxint = new Ptr<>();
            pxint.set( DUPRAT(px.deref()) );

            Rat.subrat(pxint, rat_one, precision);

            if (rat_gt(pxint.deref(), rat_negsmallest, precision) && rat_lt(pxint.deref(), rat_smallest, precision) && (sign == 1))
            {
                // *px is one, special case a 1 return.
                px.set( DUPRAT(rat_one) );
                // Ensure sign is positive.
                sign = 1;
            }
            else
            {
                // Only do the exp if the number isn't zero or one
                Ptr<RAT> podd = new Ptr<>(DUPRAT(y));
                Rat.fracrat(podd, radix, precision);

                if (rat_gt(podd.deref(), rat_negsmallest, precision) && rat_lt(podd.deref(), rat_smallest, precision))
                {
                    // If power is an integer let ratpowi32 deal with it.
                    Ptr<RAT> iy = new Ptr<>(DUPRAT(y));
                    Rat.subrat(iy, podd.deref(), precision);
                    int inty = Conv.rattoi32(iy.deref(), radix, precision);

                    Ptr<RAT> plnx = new Ptr<>(DUPRAT(px.deref()));
                    lograt(plnx, precision);
                    Rat.mulrat(plnx, iy.deref(), precision);

                    if (rat_gt(plnx.deref(), rat_max_exp, precision) || rat_lt(plnx.deref(), rat_min_exp, precision))
                    {
                        // Don't attempt exp of anything large or small.A
                        throw new ErrorCodeException(CALC_E_DOMAIN);
                    }
                    Conv.ratpowi32(px, inty, precision);
                    if ((inty & 1) == 0)
                    {
                        sign = 1;
                    }
                }
                else
                {
                    // power is a fraction
                    if (sign == -1)
                    {
                        // Need to throw an error if the exponent has an even denominator.
                        // As a first step, the numerator and denominator must be divided by 2 as many times as
                        //     possible, so that 2/6 is allowed.
                        // If the final numerator is still even, the end result should be positive.
                        Ptr<RAT> pNumerator = new Ptr<>();
                        Ptr<RAT> pDenominator = new Ptr<>();
                        boolean fBadExponent = false;

                        // Get the numbers in arbitrary precision rational number format
                        pNumerator.set( DUPRAT(rat_zero) );   // pNumerator->pq is 1 one
                        pDenominator.set( DUPRAT(rat_zero) ); // pDenominator->pq is 1 one

                        pNumerator.deref().pp = DUPNUM(y.pp);
                        pNumerator.deref().pp.sign = 1;

                        pDenominator.deref().pp = DUPNUM(y.pq);
                        pDenominator.deref().pp.sign = 1;

                        while (IsEven(pNumerator.deref(), radix, precision) && IsEven(pDenominator.deref(), radix, precision)) // both Numerator & denominator is even
                        {
                            Rat.divrat(pNumerator, rat_two, precision);
                            Rat.divrat(pDenominator, rat_two, precision);
                        }
                        if (IsEven(pDenominator.deref(), radix, precision)) // denominator is still even
                        {
                            fBadExponent = true;
                        }
                        if (IsEven(pNumerator.deref(), radix, precision)) // numerator is still even
                        {
                            sign = 1;
                        }

                        if (fBadExponent)
                        {
                            throw new ErrorCodeException(CALC_E_DOMAIN);
                        }
                    }
                    else
                    {
                        // If the exponent is not odd disregard the sign.
                        sign = 1;
                    }

                    lograt(px, precision);
                    Rat.mulrat(px, y, precision);
                    exprat(px, radix, precision);
                }
            }
        }
        px.deref().pp.sign *= sign;
    }
}
