package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.divnumx;
import static mscalc.ratpack.BaseX.mulnumx;
import static mscalc.ratpack.CalcErr.CALC_E_DIVIDEBYZERO;
import static mscalc.ratpack.CalcErr.CALC_E_INDEFINITE;
import static mscalc.ratpack.Conv.flatrat;
import static mscalc.ratpack.Conv.gcd;
import static mscalc.ratpack.Num.*;
import static mscalc.ratpack.RatPack.*;
import static mscalc.ratpack.Support.Global.num_one;
import static mscalc.ratpack.Support.Global.rat_one;
import static mscalc.ratpack.Support.trimit;

public interface Rat {
    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: gcdrat
    //
    //    ARGUMENTS: pointer to a rational.
    //
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Divides p and q in rational by the G.C.D.
    //    of both.  It was hoped this would speed up some
    //    calculations, and until the above trimming was done it
    //    did, but after trimming gcdratting, only slows things
    //    down.
    //
    //-----------------------------------------------------------------------------
    static void gcdrat(Ptr<RAT> pa, int precision)
    {
        Ptr<NUMBER> pgcd = new Ptr<>();
        RAT a = null;

        a = pa.deref();
        pgcd.set( gcd(a.pp, a.pq) );

        if (!zernum(pgcd.deref()))
        {
            Ptr<NUMBER> ppp = new Ptr<>(a.pp);
            divnumx(ppp, pgcd.deref(), precision);
            a.pp = ppp.deref();

            Ptr<NUMBER> ppq = new Ptr<>(a.pq);
            divnumx(ppq, pgcd.deref(), precision);
            a.pq = ppq.deref();
        }

        pa.set(a);
        a.RENORMALIZE();
    }


    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: fracrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes pointer.
    //
    //    DESCRIPTION: Does the rational equivalent of frac(*pa);
    //
    //-----------------------------------------------------------------------------
    static void fracrat(Ptr<RAT> pa, uint radix, int precision)
    {
        // Only do the flatrat operation if number is nonzero.
        // and only if the bottom part is not one.
        if (!zernum(pa.deref().pp) && !equnum(pa.deref().pq, Support.Global.num_one))
        {
            // flatrat(*pa, radix, precision);
            // *pa passed by reference
            flatrat(pa, radix, precision);
        }

        Ptr<NUMBER> ppp = new Ptr<>(pa.deref().pp);
        remnum(ppp, pa.deref().pq, BASEX);
        pa.deref().pp = ppp.deref();

        // Get *pa back in the integer over integer form.
        pa.deref().RENORMALIZE();
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: mulrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the rational equivalent of *pa *= b.
    //    Assumes radix is the radix of both numbers.
    //
    //-----------------------------------------------------------------------------
    static void mulrat(Ptr<RAT> pa, RAT b, int precision)
    {
        // Only do the multiply if it isn't zero.
        if (!zernum(pa.deref().pp))
        {
            Ptr<NUMBER> ppp = new Ptr<>(pa.deref().pp);
            mulnumx(ppp, b.pp);
            pa.deref().pp = ppp.deref();

            Ptr<NUMBER> ppq = new Ptr<>(pa.deref().pq);
            mulnumx(ppq, b.pq);
            pa.deref().pq = ppq.deref();

            trimit(pa, precision);
        }
        else
        {
            // If it is zero, blast a one in the denominator.
            pa.deref().pq = DUPNUM(Support.Global.num_one);
        }

        // gcdrat(pa);
    }


    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: divrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the rational equivalent of *pa /= b.
    //    Assumes radix is the radix of both numbers.
    //
    //-----------------------------------------------------------------------------
    static void divrat(Ptr<RAT> pa, RAT b, int precision)
    {
        if (!zernum(pa.deref().pp))
        {
            // Only do the divide if the top isn't zero.
            Ptr<NUMBER> ppp = new Ptr<>(pa.deref().pp);
            mulnumx(ppp, b.pq);
            pa.deref().pp = ppp.deref();

            Ptr<NUMBER> ppq = new Ptr<>(pa.deref().pq);
            mulnumx(ppq, b.pp);
            pa.deref().pq = ppq.deref();

            if (zernum(pa.deref().pq))
            {
                // raise an exception if the bottom is 0.
                throw new ErrorCodeException(CALC_E_DIVIDEBYZERO);
            }
            trimit(pa, precision);
        }
        else
        {
            // Top is zero.
            if (zerrat(b))
            {
                // If bottom is zero
                // 0 / 0 is indefinite, raise an exception.
                throw new ErrorCodeException(CALC_E_INDEFINITE);
            }
            else
            {
                // 0/x make a unique 0.
                pa.deref().pq = DUPNUM(Support.Global.num_one);
            }
        }

        // gcdrat(pa);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: subrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the rational equivalent of *pa += b.
    //    Assumes base is internal throughout.
    //
    //-----------------------------------------------------------------------------
    static void subrat(Ptr<RAT> pa, RAT b, int precision)
    {
        b.pp.sign *= -1;
        addrat(pa, b, precision);
        b.pp.sign *= -1;
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: addrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the rational equivalent of *pa += b.
    //    Assumes base is internal throughout.
    //
    //-----------------------------------------------------------------------------
    static void addrat(Ptr<RAT> pa, RAT b, int precision)
    {
        Ptr<NUMBER> bot = new Ptr<>();

        if (equnum(pa.deref().pq, b.pq))
        {
            // Very special case, q's match.,
            // make sure signs are involved in the calculation
            // we have to do this since the optimization here is only
            // working with the top half of the rationals.
            pa.deref().pp.sign *= pa.deref().pq.sign;
            pa.deref().pq.sign = 1;
            b.pp.sign *= b.pq.sign;
            b.pq.sign = 1;

            Ptr<NUMBER> ppp = new Ptr<>(pa.deref().pp);
            addnum(ppp, b.pp, BASEX);
            pa.deref().pp = ppp.deref();
        }
        else
        {
            // Usual case q's aren't the same.
            bot.set(DUPNUM(pa.deref().pq));
            mulnumx(bot, b.pq);

            Ptr<NUMBER> ppp = new Ptr<>(pa.deref().pp);
            mulnumx(ppp, b.pq);
            pa.deref().pp = ppp.deref();

            Ptr<NUMBER> ppq = new Ptr<>(pa.deref().pq);
            mulnumx(ppq, b.pp);
            pa.deref().pq = ppq.deref();

            ppp = new Ptr<>(pa.deref().pp);
            addnum(ppp, pa.deref().pq, BASEX);
            pa.deref().pp = ppp.deref();

            pa.deref().pq = bot.deref();
            trimit(pa, precision);

            // Get rid of negative zeros here.
            pa.deref().pp.sign *= pa.deref().pq.sign;
            pa.deref().pq.sign = 1;
        }

        // gcdrat(pa);
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: rootrat
    //
    //  PARAMETERS: y prat representation of number to take the root of
    //              n prat representation of the root to take.
    //
    //  RETURN: bth root of a in rat form.
    //
    //  EXPLANATION: This is now a stub function to powrat().
    //
    //-----------------------------------------------------------------------------
    static void rootrat(Ptr<RAT> py, RAT n, uint radix, int precision)
    {
        throw new RuntimeException("Not yet implemented");

        // Initialize 1/n
        // Ptr<RAT> oneovern = new Ptr<>(DUPRAT(rat_one));
        // divrat(oneovern, n, precision);
        // TODO: powrat(py, oneovern, radix, precision);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: zerrat
    //
    //    ARGUMENTS: Rational number.
    //
    //    RETURN: Boolean
    //
    //    DESCRIPTION: Returns true if input is zero.
    //    False otherwise.
    //
    //-----------------------------------------------------------------------------
    static boolean zerrat(RAT a)
    {
        return (zernum(a.pp));
    }
}
