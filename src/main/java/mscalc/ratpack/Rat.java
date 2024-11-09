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
import static mscalc.ratpack.RatPack.BASEX;
import static mscalc.ratpack.RatPack.DUPNUM;
import static mscalc.ratpack.Support.num_one;
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
        if (!zernum(pa.deref().pp) && !equnum(pa.deref().pq, num_one))
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
            pa.deref().pp = DUPNUM(num_one);
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
                pa.deref().pq = DUPNUM(num_one);
            }
        }

        // gcdrat(pa);
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
