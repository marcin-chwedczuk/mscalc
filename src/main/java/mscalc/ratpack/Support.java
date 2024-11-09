package mscalc.ratpack;

import mscalc.cpp.ArrayPtrUInt;
import mscalc.cpp.Ptr;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import java.util.concurrent.atomic.AtomicBoolean;

import static mscalc.ratpack.Conv.g_ratio;
import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.RatPack.BASEX;

public interface Support {
    AtomicBoolean g_ftrueinfinite = new AtomicBoolean(false); // Set to true if you don't want
    // chopping internally
    // precision used internally

    NUMBER num_two = i32tonum(2, BASEX);
    NUMBER num_one = i32tonum(1, BASEX);


    //---------------------------------------------------------------------------
    //
    //  FUNCTION: trimit
    //
    //  ARGUMENTS:  PRAT *px, int32_t precision
    //
    //
    //  DESCRIPTION: Chops off digits from rational numbers to avoid time
    //  explosions in calculations of functions using series.
    //  It can be shown that it is enough to only keep the first n digits
    //  of the largest of p or q in the rational p over q form, and of course
    //  scale the smaller by the same number of digits.  This will give you
    //  n-1 digits of accuracy.  This dramatically speeds up calculations
    //  involving hundreds of digits or more.
    //  The last part of this trim dealing with exponents never affects accuracy
    //
    //  RETURN: none, modifies the pointed to PRAT
    //
    //---------------------------------------------------------------------------
    static void trimit(Ptr<RAT> px, int precision)
    {
        if (!g_ftrueinfinite.get())
        {
            NUMBER pp = px.deref().pp;
            NUMBER pq = px.deref().pq;
            int trim = g_ratio.get() * (Math.min((pp.cdigit + pp.exp), (pq.cdigit + pq.exp)) - 1) - precision;
            if (trim > g_ratio.get())
            {
                trim /= g_ratio.get();

                if (trim <= pp.exp)
                {
                    pp.exp -= trim;
                }
                else
                {
                    // memmove(pp->mant, &(pp->mant[trim - pp->exp]), sizeof(MANTTYPE) * (pp->cdigit - trim + pp->exp));
                    ArrayPtrUInt tmp = pp.mant.pointer();
                    tmp.advance(trim - pp.exp);
                    for (int k = 0; k < pp.cdigit - trim + pp.exp; k++) {
                        pp.mant.set(k, tmp.derefPlusPlus());
                    }

                    pp.cdigit -= trim - pp.exp;
                    pp.exp = 0;
                }

                if (trim <= pq.exp)
                {
                    pq.exp -= trim;
                }
                else
                {
                    // memmove(pq->mant, &(pq->mant[trim - pq->exp]), sizeof(MANTTYPE) * (pq->cdigit - trim + pq->exp));
                    ArrayPtrUInt tmp = pq.mant.pointer();
                    tmp.advance(trim - pq.exp);
                    for (int k = 0; k < pq.cdigit - trim + pq.exp; k++) {
                        pp.mant.set(k, tmp.derefPlusPlus());
                    }

                    pq.cdigit -= trim - pq.exp;
                    pq.exp = 0;
                }
            }
            trim = Math.min(pp.exp, pq.exp);
            pp.exp -= trim;
            pq.exp -= trim;
        }
    }
}
