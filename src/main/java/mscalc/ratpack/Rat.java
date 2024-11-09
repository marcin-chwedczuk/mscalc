package mscalc.ratpack;

import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.divnumx;
import static mscalc.ratpack.Conv.flatrat;
import static mscalc.ratpack.Conv.gcd;
import static mscalc.ratpack.Num.*;
import static mscalc.ratpack.RatPack.BASEX;
import static mscalc.ratpack.Support.num_one;

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

}
