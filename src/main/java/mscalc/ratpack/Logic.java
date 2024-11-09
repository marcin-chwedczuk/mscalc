package mscalc.ratpack;

import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.mulnumx;
import static mscalc.ratpack.CalcErr.CALC_E_INDEFINITE;
import static mscalc.ratpack.Num.remnum;
import static mscalc.ratpack.Rat.zerrat;
import static mscalc.ratpack.RatPack.BASEX;
import static mscalc.ratpack.RatPack.DUPRAT;

public interface Logic {
    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: remrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes pointer.
    //
    //    DESCRIPTION: Calculate the remainder of *pa / b,
    //                 equivalent of 'pa % b' in C/C++ and produces a result
    //                 that is either zero or has the same sign as the dividend.
    //
    //-----------------------------------------------------------------------------
    static void remrat(Ptr<RAT> pa, RAT b)
    {
        if (zerrat(b))
        {
            throw new ErrorCodeException(CALC_E_INDEFINITE);
        }

        Ptr<RAT> tmp = new Ptr<>(DUPRAT(b));

        Ptr<RatPack.NUMBER> ppp = new Ptr<>(pa.deref().pp);
        mulnumx(ppp, tmp.deref().pq);
        pa.deref().pp = ppp.deref();

        ppp = new Ptr<>(tmp.deref().pp);
        mulnumx(ppp, pa.deref().pq);
        tmp.deref().pp = ppp.deref();

        ppp = new Ptr<>(pa.deref().pp);
        remnum(ppp, tmp.deref().pp, BASEX);
        pa.deref().pp = ppp.deref();

        Ptr<RatPack.NUMBER> ppq = new Ptr<>(pa.deref().pq);
        mulnumx(ppq, tmp.deref().pq);
        pa.deref().pq = ppq.deref();

        // Get *pa back in the integer over integer form.
        pa.deref().RENORMALIZE();
    }
}
