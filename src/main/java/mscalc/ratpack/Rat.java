package mscalc.ratpack;

import mscalc.cpp.Ptr;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.divnumx;
import static mscalc.ratpack.Conv.gcd;
import static mscalc.ratpack.Num.zernum;

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
}
