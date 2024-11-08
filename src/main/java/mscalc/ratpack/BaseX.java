package mscalc.ratpack;

import mscalc.cpp.ArrayPtrUInt;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.cpp.ulong;
import mscalc.ratpack.RatPack.NUMBER;

import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.RatPack.*;

public interface BaseX {
    //----------------------------------------------------------------------------
    //
    //    FUNCTION: mulnumx
    //
    //    ARGUMENTS: pointer to a number and a second number, the
    //               base is always BASEX.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa *= b.
    //    This is a stub which prevents multiplication by 1, this is a big speed
    //    improvement.
    //
    //----------------------------------------------------------------------------
    static void mulnumx(Ptr<NUMBER> pa, NUMBER b)
    {
        if (b.cdigit > 1 || b.mant.at(0).notEq(1) || b.exp != 0)
        {
            // If b is not one we multiply
            if (pa.deref().cdigit > 1 || pa.deref().mant.at(0).notEq(1) || pa.deref().exp != 0)
            {
                // pa and b are both non-one.
                _mulnumx(pa, b);
            }
            else
            {
                // if pa is one and b isn't => just copy b and adjust the sign.
                int sign = pa.deref().sign;
                pa.set(DUPNUM(b));
                pa.deref().sign *= sign;
            }
        }
        else
        {
            // B is +/- 1, But we do have to set the sign.
            pa.deref().sign *= b.sign;
        }
    }

    //----------------------------------------------------------------------------
    //
    //    FUNCTION: _mulnumx
    //
    //    ARGUMENTS: pointer to a number and a second number, the
    //               base is always BASEX.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa *= b.
    //    Assumes the base is BASEX of both numbers.  This algorithm is the
    //    same one you learned in grade school, except the base isn't 10 it's
    //    BASEX.
    //
    //----------------------------------------------------------------------------
    static void _mulnumx(Ptr<NUMBER> pa, NUMBER b)
    {
        NUMBER c = null;  // c will contain the result.
        NUMBER a = null;  // a is the dereferenced number pointer from *pa
        ArrayPtrUInt ptra;       // ptra is a pointer to the mantissa of a.
        ArrayPtrUInt ptrb;       // ptrb is a pointer to the mantissa of b.
        ArrayPtrUInt ptrc;       // ptrc is a pointer to the mantissa of c.
        ArrayPtrUInt ptrcoffset; // ptrcoffset, is the anchor location of the next
        // single digit multiply partial result.
        int iadigit = 0;  // Index of digit being used in the first number.
        int ibdigit = 0;  // Index of digit being used in the second number.
        uint da = uint.of(0);      // da is the digit from the fist number.
        ulong cy = ulong.of(0);  // cy is the carry resulting from the addition of
        // a multiplied row into the result.
        ulong mcy = ulong.of(0); // mcy is the resultant from a single
        // multiply, AND the carry of that multiply.
        int icdigit = 0;  // Index of digit being calculated in final result.

        a = pa.deref().clone();

        ibdigit = a.cdigit + b.cdigit - 1;
        c = createnum(uint.of(ibdigit + 1));
        c.cdigit = ibdigit;
        c.sign = a.sign * b.sign;

        c.exp = a.exp + b.exp;
        ptra = a.mant.pointer();
        ptrcoffset = c.mant.pointer();

        for (iadigit = a.cdigit; iadigit > 0; iadigit--)
        {
            da = ptra.derefPlusPlus();
            ptrb = b.mant.pointer();

            // Shift ptrc, and ptrcoffset, one for each digit
            ptrc = ptrcoffset.clone(); ptrcoffset.advance();

            for (ibdigit = b.cdigit; ibdigit > 0; ibdigit--)
            {
                cy = ulong.of(0);
                mcy = da.toULong().multiply(ptrb.deref());
                if (mcy.notEq(0))
                {
                    icdigit = 0;
                    if (ibdigit == 1 && iadigit == 1)
                    {
                        c.cdigit++;
                    }
                }

                // If result is nonzero, or while result of carry is nonzero...
                while (mcy.notEq(0) || cy.notEq(0))
                {
                    // update carry from addition(s) and multiply.
                    cy = cy.plus(ptrc.at(icdigit).toULong().plus(mcy.toUInt().bitAnd(BASEX.bitNeg())));

                    // update result digit from
                    ptrc.setAt(icdigit++, cy.toUInt().bitAnd(BASEX.bitNeg()));

                    // update carries from
                    mcy = mcy.shiftRight(BASEXPWR);
                    cy = cy.shiftRight(BASEXPWR);
                }

                ptrb.advance();
                ptrc.advance();
            }
        }

        // prevent different kinds of zeros, by stripping leading duplicate zeros.
        // digits are in order of increasing significance.
        while (c.cdigit > 1 && c.mant.at(c.cdigit - 1).isZero())
        {
            c.cdigit--;
        }

        pa.set(c);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: numpowi32x
    //
    //    ARGUMENTS: root as number power as int32_t
    //               number.
    //
    //    RETURN: None root is changed.
    //
    //    DESCRIPTION: changes numeric representation of root to
    //    root ** power. Assumes base BASEX
    //    decomposes the exponent into it's sums of powers of 2, so on average
    //    it will take n+n/2 multiplies where n is the highest on bit.
    //
    //-----------------------------------------------------------------------------
    static void numpowi32x(Ptr<NUMBER> proot, int power)
    {
        Ptr<NUMBER> lret = new Ptr<>(i32tonum(1, BASEX));

        // Once the power remaining is zero we are done.
        while (power > 0)
        {
            // If this bit in the power decomposition is on, multiply the result
            // by the root number.
            if ((power & 1) != 0)
            {
                mulnumx(lret, proot.deref());
            }

            // multiply the root number by itself to scale for the next bit (i.e.
            // square it.
            mulnumx(proot, proot.deref());

            // move the next bit of the power into place.
            power >>= 1;
        }

        proot.set(lret.deref());
    }

    //----------------------------------------------------------------------------
    //
    //    FUNCTION: divnumx
    //
    //    ARGUMENTS: pointer to a number, a second number and precision.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa /= b.
    //    Assumes radix is the internal radix representation.
    //    This is a stub which prevents division by 1, this is a big speed
    //    improvement.
    //
    //----------------------------------------------------------------------------
    static void divnumx(Ptr<NUMBER> pa, NUMBER b, int precision)
    {
        if (b.cdigit > 1 || b.mant.at(0).notEq(1) || b.exp != 0)
        {
            // b is not one.
            if (pa.deref().cdigit > 1 || pa.deref().mant.at(0).notEq(1) || pa.deref().exp != 0)
            {
                // pa and b are both not one.
                _divnumx(pa, b, precision);
            }
        else
            {
                // if pa is one and b is not one, just copy b, and adjust the sign.
                int sign = pa.deref().sign;
                pa.set(DUPNUM(b));
                pa.deref().sign *= sign;
            }
        }
        else
        {
            // b is one so don't divide, but set the sign.
            pa.deref().sign *= b.sign;
        }
    }

    static void _divnumx(Ptr<NUMBER> pa, NUMBER b, int precision) {

    }


    //----------------------------------------------------------------------------
    //
    //    FUNCTION: _divnumx
    //
    //    ARGUMENTS: pointer to a number, a second number and precision.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa /= b.
    //    Assumes radix is the internal radix representation.
    //
    //----------------------------------------------------------------------------
    /*
    static void _divnumx(Ptr<NUMBER> pa, NUMBER b, int precision)
    {
        NUMBER a = null;       // a is the dereferenced number pointer from *pa
        NUMBER c = null;       // c will contain the result.
        NUMBER lasttmp = null; // lasttmp allows a backup when the algorithm
        // guesses one bit too far.
        NUMBER tmp = null;     // current guess being worked on for divide.
        NUMBER rem = null;     // remainder after applying guess.
        int cdigits;           // count of digits for answer.
        ArrayPtrUInt ptrc;            // ptrc is a pointer to the mantissa of c.

        int thismax = precision + g_ratio.get(); // set a maximum number of internal digits
        // to shoot for in the divide.

        a = pa.deref();
        if (thismax < a.cdigit)
        {
            // a has more digits than precision specified, bump up digits to shoot
            // for.
            thismax = a.cdigit;
        }

        if (thismax < b.cdigit)
        {
            // b has more digits than precision specified, bump up digits to shoot
            // for.
            thismax = b.cdigit;
        }

        // Create c (the divide answer) and set up exponent and sign.
        c = createnum(uint.of(thismax + 1));
        c.exp = (a.cdigit + a.exp) - (b.cdigit + b.exp) + 1;
        c.sign = a.sign * b.sign;

        ptrc = c.mant.pointer();
        ptrc.advance(thismax);

        cdigits = 0;

        rem = DUPNUM(a);
        rem.sign = b.sign;
        rem.exp = b.cdigit + b.exp - rem.cdigit;

        while (cdigits++ < thismax && !zernum(rem))
        {
            int32_t digit = 0;
        *ptrc = 0;
            while (!lessnum(rem, b))
            {
                digit = 1;
                DUPNUM(tmp, b);
                destroynum(lasttmp);
                lasttmp = i32tonum(0, BASEX);
                while (lessnum(tmp, rem))
                {
                    destroynum(lasttmp);
                    DUPNUM(lasttmp, tmp);
                    addnum(&tmp, tmp, BASEX);
                    digit *= 2;
                }
                if (lessnum(rem, tmp))
                {
                    // too far, back up...
                    destroynum(tmp);
                    digit /= 2;
                    tmp = lasttmp;
                    lasttmp = nullptr;
                }

                tmp->sign *= -1;
                addnum(&rem, tmp, BASEX);
                destroynum(tmp);
                destroynum(lasttmp);
            *ptrc |= digit;
            }
            rem->exp++;
            ptrc--;
        }
        cdigits--;
        if (c->mant != ++ptrc)
        {
            memmove(c->mant, ptrc, (int)(cdigits * sizeof(MANTTYPE)));
        }

        if (!cdigits)
        {
            // A zero, make sure no weird exponents creep in
            c->exp = 0;
            c->cdigit = 1;
        }
        else
        {
            c->cdigit = cdigits;
            c->exp -= cdigits;
            // prevent different kinds of zeros, by stripping leading duplicate
            // zeros. digits are in order of increasing significance.
            while (c->cdigit > 1 && c->mant[c->cdigit - 1] == 0)
            {
                c->cdigit--;
            }
        }

        destroynum(rem);

        destroynum(*pa);
    *pa = c;
    }
    */
}
