package mscalc.ratpack;

import mscalc.cpp.ArrayPtrUInt;
import mscalc.cpp.ErrorCodeException;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import static mscalc.ratpack.BaseX.mulnumx;
import static mscalc.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.ratpack.CalcErr.CALC_E_INDEFINITE;
import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.Logic.BOOL_FUNCS.*;
import static mscalc.ratpack.Num.remnum;
import static mscalc.ratpack.Num.zernum;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.BASEX;
import static mscalc.ratpack.RatPack.DUPRAT;
import static mscalc.ratpack.Support.*;
import static mscalc.ratpack.Support.Global.*;

public interface Logic {

    static void lshrat(Ptr<RAT> pa, RAT b, uint radix, int precision)
    {
        Ptr<RAT> pwr = new Ptr<>();

        intrat(pa, radix, precision);
        if (!zernum(pa.deref().pp))
        {
            // If input is zero we're done.
            if (rat_gt(b, rat_max_exp, precision))
            {
                // Don't attempt lsh of anything big
                throw new ErrorCodeException(CALC_E_DOMAIN);
            }

            final int intb = rattoi32(b, radix, precision);
            pwr.set(DUPRAT(rat_two));
            ratpowi32(pwr, intb, precision);
            mulrat(pa, pwr.deref(), precision);
        }
    }

    static void rshrat(Ptr<RAT> pa, RAT b, uint radix, int precision)
    {
        Ptr<RAT> pwr = new Ptr<>();

        intrat(pa, radix, precision);
        if (!zernum(pa.deref().pp))
        {
            // If input is zero we're done.
            if (rat_lt(b, rat_min_exp, precision))
            {
                // Don't attempt rsh of anything big and negative.
                throw new ErrorCodeException(CALC_E_DOMAIN);
            }

            final int intb = rattoi32(b, radix, precision);
            pwr.set(DUPRAT(rat_two));
            ratpowi32(pwr, intb, precision);
            divrat(pa, pwr.deref(), precision);
        }
    }

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

        Ptr<NUMBER> ppp = new Ptr<>(pa.deref().pp);
        mulnumx(ppp, tmp.deref().pq);
        pa.deref().pp = ppp.deref();

        ppp = new Ptr<>(tmp.deref().pp);
        mulnumx(ppp, pa.deref().pq);
        tmp.deref().pp = ppp.deref();

        ppp = new Ptr<>(pa.deref().pp);
        remnum(ppp, tmp.deref().pp, BASEX);
        pa.deref().pp = ppp.deref();

        Ptr<NUMBER> ppq = new Ptr<>(pa.deref().pq);
        mulnumx(ppq, tmp.deref().pq);
        pa.deref().pq = ppq.deref();

        // Get *pa back in the integer over integer form.
        pa.deref().RENORMALIZE();
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: modrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes pointer.
    //
    //    DESCRIPTION: Calculate the remainder of *pa / b, with the sign of the result
    //                 either zero or has the same sign as the divisor.
    //    NOTE: When *pa or b are negative, the result won't be the same as
    //          the C/C++ operator %, use remrat if it's the behavior you expect.
    //
    //-----------------------------------------------------------------------------
    static void modrat(Ptr<RAT> pa, RAT b)
    {
        // contrary to remrat(X, 0) returning 0, modrat(X, 0) must return X
        if (zerrat(b))
        {
            return;
        }

        Ptr<RAT> tmp = new Ptr<>(DUPRAT(b));

        var needAdjust = (pa.deref().SIGN() == -1 ? (b.SIGN() == 1) : (b.SIGN() == -1));

        pa.deref().ppPtr(ppp -> mulnumx(ppp, tmp.deref().pq));
        tmp.deref().ppPtr(ppp -> mulnumx(ppp, pa.deref().pq));
        pa.deref().ppPtr(ppp -> remnum(ppp, tmp.deref().pp, BASEX));
        pa.deref().pqPtr(ppq -> mulnumx(ppq, tmp.deref().pq));

        if (needAdjust && !zerrat(pa.deref()))
        {
            addrat(pa, b, BASEX.toInt());
        }

        // Get *pa back in the integer over integer form.
        pa.deref().RENORMALIZE();
    }

    static void andrat(Ptr<RAT> pa, RAT b, uint radix, int precision)
    {
        boolrat(pa, b, FUNC_AND, radix, precision);
    }

    static void orrat(Ptr<RAT> pa, RAT b, uint radix, int precision)
    {
        boolrat(pa, b, FUNC_OR, radix, precision);
    }

    static void xorrat(Ptr<RAT> pa, RAT b, uint radix, int precision)
    {
        boolrat(pa, b, FUNC_XOR, radix, precision);
    }

    //---------------------------------------------------------------------------
    //
    //    FUNCTION: boolrat
    //
    //    ARGUMENTS: pointer to a rational a second rational.
    //
    //    RETURN: None, changes pointer.
    //
    //    DESCRIPTION: Does the rational equivalent of *pa op= b;
    //
    //---------------------------------------------------------------------------
    static void boolrat(Ptr<RAT> pa, RAT b, BOOL_FUNCS func, uint radix, int precision)
    {
        Ptr<RAT> tmp = new Ptr<>();
        intrat(pa, radix, precision);
        tmp.set(DUPRAT(b));
        intrat(tmp, radix, precision);

        pa.deref().ppPtr(ppp -> boolnum(ppp, tmp.deref().pp, func));
    }


    //---------------------------------------------------------------------------
    //
    //    FUNCTION: boolnum
    //
    //    ARGUMENTS: pointer to a number a second number
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa &= b.
    //    radix doesn't matter for logicals.
    //    WARNING: Assumes numbers are unsigned.
    //
    //---------------------------------------------------------------------------
    static void boolnum(Ptr<NUMBER> pa, NUMBER b, BOOL_FUNCS func)
    {
        NUMBER c = null;
        NUMBER a = null;
        ArrayPtrUInt pcha;
        ArrayPtrUInt pchb;
        ArrayPtrUInt pchc;
        int cdigits;
        int mexp;
        uint da;
        uint db;

        a = pa.deref();
        cdigits = Math.max(a.cdigit + a.exp, b.cdigit + b.exp) - Math.min(a.exp, b.exp);
        c = createnum(uint.of(cdigits));
        c.exp = Math.min(a.exp, b.exp);
        mexp = c.exp;
        c.cdigit = cdigits;
        pcha = a.mant.pointer();
        pchb = b.mant.pointer();
        pchc = c.mant.pointer();
        for (; cdigits > 0; cdigits--, mexp++)
        {
            da = (((mexp >= a.exp) && (cdigits + a.exp - c.exp > (c.cdigit - a.cdigit))) ? pcha.derefPlusPlus() : uint.ZERO);
            db = (((mexp >= b.exp) && (cdigits + b.exp - c.exp > (c.cdigit - b.cdigit))) ? pchb.derefPlusPlus() : uint.ZERO);
            switch (func)
            {
                case FUNC_AND:
                    pchc.set(da.bitAnd(db));
                    break;
                case FUNC_OR:
                    pchc.set(da.bitOr(db));
                    break;
                case FUNC_XOR:
                    pchc.set(da.bitXor(db));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid function: " + func);
            }
            pchc.advance();
        }

        c.sign = a.sign;
        while (c.cdigit > 1 && pchc.minusMinusDeref().isZero())
        {
            c.cdigit--;
        }

        pa.set(c);
    }

    enum BOOL_FUNCS
    {
        FUNC_AND,
        FUNC_OR,
        FUNC_XOR
    }


}
