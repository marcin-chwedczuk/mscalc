package mscalc.engine.ratpack;

import mscalc.engine.cpp.UIntArrayPtr;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.cpp.ulong;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public interface Num {
    //----------------------------------------------------------------------------
    //
    //    FUNCTION: addnum
    //
    //    ARGUMENTS: pointer to a number a second number, and the
    //               radix.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa += b.
    //    Assumes radix is the base of both numbers.
    //
    //    ALGORITHM: Adds each digit from least significant to most
    //    significant.
    //
    //
    //----------------------------------------------------------------------------
    static void addnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix)
    {
        if (b.cdigit > 1 || b.mant.at(0).notEq(0))
        { // If b is zero we are done.
            if (pa.deref().cdigit > 1 || pa.deref().mant.at(0).notEq(0))
            { // pa and b are both nonzero.
                _addnum(pa, b, radix);
            }
        else
            { // if pa is zero and b isn't just copy b.
                pa.set(RatPack.DUPNUM(b));
            }
        }
    }

    static void _addnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix)

    {
        RatPack.NUMBER c = null; // c will contain the result.
        RatPack.NUMBER a = null; // a is the dereferenced number pointer from *pa
        UIntArrayPtr pcha;      // pcha is a pointer to the mantissa of a.
        UIntArrayPtr pchb;      // pchb is a pointer to the mantissa of b.
        UIntArrayPtr pchc;      // pchc is a pointer to the mantissa of c.
        int cdigits;     // cdigits is the max count of the digits results used as a counter.
        int mexp;        // mexp is the exponent of the result.
        uint da;         // da is a single 'digit' after possible padding.
        uint db;         // db is a single 'digit' after possible padding.
        uint cy = uint.of(0);     // cy is the value of a carry after adding two 'digits'
        int fcompla = 0; // fcompla is a flag to signal a is negative.
        int fcomplb = 0; // fcomplb is a flag to signal b is negative.

        a = pa.deref();

        // Calculate the overlap of the numbers after alignment, this includes
        // necessary padding 0's
        cdigits = max(a.cdigit + a.exp, b.cdigit + b.exp) - min(a.exp, b.exp);

        c = Conv.createnum(uint.of(cdigits + 1));
        c.exp = min(a.exp, b.exp);
        mexp = c.exp;
        c.cdigit = cdigits;
        pcha = a.mant.pointer();
        pchb = b.mant.pointer();
        pchc = c.mant.pointer();

        // Figure out the sign of the numbers
        if (a.sign != b.sign)
        {
            cy = uint.of(1);
            fcompla = (a.sign == -1) ? 1 : 0;
            fcomplb = (b.sign == -1) ? 1 : 0;
        }

        // Loop over all the digits, real and 0 padded. Here we know a and b are
        // aligned
        for (; cdigits > 0; cdigits--, mexp++)
        {
            // Get digit from a, taking padding into account.
            da = (((mexp >= a.exp) && (cdigits + a.exp - c.exp > (c.cdigit - a.cdigit))) ? pcha.derefPlusPlus() : uint.ZERO);
            // Get digit from b, taking padding into account.
            db = (((mexp >= b.exp) && (cdigits + b.exp - c.exp > (c.cdigit - b.cdigit))) ? pchb.derefPlusPlus() : uint.ZERO);

            // Handle complementing for a and b digit. Might be a better way, but
            // haven't found it yet.
            if (fcompla != 0)
            {
                da = radix.subtract(uint.ONE).subtract(da);
            }
            if (fcomplb != 0)
            {
                db = radix.subtract(uint.ONE).subtract(db);
            }

            // Update carry as necessary
            cy = da.add(db).add(cy);

            pchc.set(cy.modulo(radix));
            pchc.advance();

            cy = cy.divide(radix);
        }

        // Handle carry from last sum as extra digit
        if (cy.toBool() && !(fcompla != 0 || fcomplb != 0))
        {
            pchc.set(cy); pchc.advance();
            c.cdigit++;
        }

        // Compute sign of result
        if (!(fcompla != 0 || fcomplb != 0))
        {
            c.sign = a.sign;
        }
        else
        {
            if (cy.toBool())
            {
                c.sign = 1;
            }
            else
            {
                // In this particular case an overflow or underflow has occurred
                // and all the digits need to be complemented, at one time an
                // attempt to handle this above was made, it turned out to be much
                // slower on average.
                c.sign = -1;
                cy = uint.ONE;
                pchc = c.mant.pointer();
                for (cdigits = c.cdigit; cdigits > 0; cdigits--)
                {
                    cy = radix.subtract(uint.ONE).subtract(pchc.deref()).add(cy);
                    pchc.set(cy.modulo(radix));
                    pchc.advance();
                    cy = cy.divide(radix);
                }
            }
        }

        // Remove leading zeros, remember digits are in order of
        // increasing significance. i.e. 100 would be 0,0,1
        while (c.cdigit > 1 && pchc.minusMinusDeref().isZero())
        {
            c.cdigit--;
        }

        pa.set(c);
    }


    //----------------------------------------------------------------------------
    //
    //    FUNCTION: mulnum
    //
    //    ARGUMENTS: pointer to a number a second number, and the
    //               radix.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa *= b.
    //    Assumes radix is the radix of both numbers.  This algorithm is the
    //    same one you learned in grade school.
    //
    //----------------------------------------------------------------------------
    static void mulnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix)
    {
        if (b.cdigit > 1 || b.mant.at(0).notEq(1) || b.exp != 0)
        { // If b is one we don't multiply exactly.
            if (pa.deref().cdigit > 1 || pa.deref().mant.at(0).notEq(1) || pa.deref().exp != 0)
            { // pa and b are both non-one.
                _mulnum(pa, b, radix);
            }
        else
            { // if pa is one and b isn't just copy b, and adjust the sign.
                int sign = pa.deref().sign;
                pa.set(RatPack.DUPNUM(b));
                pa.deref().sign *= sign;
            }
        }
        else
        { // But we do have to set the sign.
            pa.set(RatPack.DUPNUM(pa.deref())); // mc: Make a defensive copy to not modify original argument
            pa.deref().sign *= b.sign;
        }
    }

    static void _mulnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix)
    {
        RatPack.NUMBER c = null;  // c will contain the result.
        RatPack.NUMBER a = null;  // a is the dereferenced number pointer from *pa
        UIntArrayPtr pcha;       // pcha is a pointer to the mantissa of a.
        UIntArrayPtr pchb;       // pchb is a pointer to the mantissa of b.
        UIntArrayPtr pchc;       // pchc is a pointer to the mantissa of c.
        UIntArrayPtr pchcoffset; // pchcoffset, is the anchor location of the next
        // single digit multiply partial result.
        int iadigit = 0;  // Index of digit being used in the first number.
        int ibdigit = 0;  // Index of digit being used in the second number.
        uint da = uint.ZERO;      // da is the digit from the fist number.
        ulong cy = ulong.ZERO;  // cy is the carry resulting from the addition of
        // a multiplied row into the result.
        ulong mcy = ulong.ZERO; // mcy is the resultant from a single
        // multiply, AND the carry of that multiply.
        int icdigit = 0;  // Index of digit being calculated in final result.

        a = pa.deref();
        ibdigit = a.cdigit + b.cdigit - 1;
        c = Conv.createnum(uint.of(ibdigit + 1));
        c.cdigit = ibdigit;
        c.sign = a.sign * b.sign;

        c.exp = a.exp + b.exp;
        pcha = a.mant.pointer();
        pchcoffset = c.mant.pointer();

        for (iadigit = a.cdigit; iadigit > 0; iadigit--)
        {
            da = pcha.derefPlusPlus();
            pchb = b.mant.pointer();

            // Shift pchc, and pchcoffset, one for each digit
            pchc = pchcoffset.copy();
            pchcoffset.advance();

            for (ibdigit = b.cdigit; ibdigit > 0; ibdigit--)
            {
                cy = ulong.ZERO;
                mcy = da.multiply(pchb.deref());
                if (mcy.toBool())
                {
                    icdigit = 0;
                    if (ibdigit == 1 && iadigit == 1)
                    {
                        c.cdigit++;
                    }
                }
                // If result is nonzero, or while result of carry is nonzero...
                while (mcy.toBool() || cy.toBool())
                {
                    // update carry from addition(s) and multiply.
                    cy = cy.plus( pchc.at(icdigit).toULong().plus(mcy.modulo(radix.toULong())) );

                    // update result digit from
                    pchc.setAt(icdigit++, (cy.modulo(radix)).toUInt());

                    // update carries from
                    mcy = mcy.divide(radix);
                    cy = cy.divide(radix);
                }

                pchb.advance();
                pchc.advance();
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


    //----------------------------------------------------------------------------
    //
    //    FUNCTION: remnum
    //
    //    ARGUMENTS: pointer to a number a second number, and the
    //               radix.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa %= b.
    //            Repeatedly subtracts off powers of 2 of b until *pa < b.
    //
    //
    //----------------------------------------------------------------------------
    static void remnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix)
    {
        Ptr<RatPack.NUMBER> tmp = new Ptr<>(null);     // tmp is the working remainder.
        RatPack.NUMBER lasttmp = null; // lasttmp is the last remainder which worked.

        // Once *pa is less than b, *pa is the remainder.
        while (!lessnum(pa.deref(), b))
        {
            tmp.set(RatPack.DUPNUM(b));
            if (lessnum(tmp.deref(), pa.deref()))
            {
                // Start off close to the right answer for subtraction.
                tmp.deref().exp = pa.deref().cdigit + pa.deref().exp - tmp.deref().cdigit;
                if (pa.deref().MSD().compareTo(tmp.deref().MSD()) <= 0)
                {
                    // Don't take the chance that the numbers are equal.
                    tmp.deref().exp--;
                }
            }

            lasttmp = Conv.i32tonum(0, radix);

            while (lessnum(tmp.deref(), pa.deref()))
            {
                lasttmp = RatPack.DUPNUM(tmp.deref());
                addnum(tmp, tmp.deref(), radix);
            }

            if (lessnum(pa.deref(), tmp.deref()))
            {
                // too far, back up...
                tmp.set(lasttmp);
                lasttmp = null;
            }

            // Subtract the working remainder from the remainder holder.
            tmp.deref().sign = -1 * pa.deref().sign;
            addnum(pa, tmp.deref(), radix);
        }
    }

    //---------------------------------------------------------------------------
    //
    //    FUNCTION: divnum
    //
    //    ARGUMENTS: pointer to a number a second number, and the
    //               radix.
    //
    //    RETURN: None, changes first pointer.
    //
    //    DESCRIPTION: Does the number equivalent of *pa /= b.
    //    Assumes radix is the radix of both numbers.
    //
    //---------------------------------------------------------------------------
    static void divnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix, int precision)
    {
        if (b.cdigit > 1 || b.mant.at(0).notEq(1) || b.exp != 0)
        {
            // b is not one
            _divnum(pa, b, radix, precision);
        }
        else
        { // But we do have to set the sign.
            pa.set(RatPack.DUPNUM(pa.deref()));
            pa.deref().sign *= b.sign;
        }
    }

    static void _divnum(Ptr<RatPack.NUMBER> pa, RatPack.NUMBER b, uint radix, int precision)
    {
        RatPack.NUMBER a = pa.deref();
        int thismax = precision + 2;
        if (thismax < a.cdigit)
        {
            thismax = a.cdigit;
        }

        if (thismax < b.cdigit)
        {
            thismax = b.cdigit;
        }

        RatPack.NUMBER c = Conv.createnum(uint.of(thismax + 1));
        c.exp = (a.cdigit + a.exp) - (b.cdigit + b.exp) + 1;
        c.sign = a.sign * b.sign;

        UIntArrayPtr ptrc = c.mant.pointer();
        ptrc.advance(thismax);

        Ptr<RatPack.NUMBER> rem = new Ptr<>(null);
        RatPack.NUMBER tmp = null;
        rem.set(RatPack.DUPNUM(a));
        tmp = RatPack.DUPNUM(b);
        tmp.sign = a.sign;
        rem.deref().exp = b.cdigit + b.exp - rem.deref().cdigit;

        // Build a table of multiplications of the divisor, this is quicker for
        // more than radix 'digits'
        List<RatPack.NUMBER> numberList = new ArrayList<>();
        numberList.add(Conv.i32tonum(0, radix));

        for (long i = 1; i < radix.toULong().raw(); i++)
        {
            // TODO: Very inefficient
            Ptr<RatPack.NUMBER> newValue = new Ptr<>(RatPack.DUPNUM(numberList.getFirst()));
            addnum(newValue, tmp, radix);

            numberList.addFirst(newValue.deref());
        }
        tmp = null;

        int digit;
        int cdigits = 0;
        while (cdigits++ < thismax && !zernum(rem.deref()))
        {
            digit = radix.toInt() - 1;
            RatPack.NUMBER multiple = null;
            for (RatPack.NUMBER num : numberList)
            {
                if (!lessnum(rem.deref(), num) || 0 == --digit)
                {
                    multiple = num;
                    break;
                }
            }

            if (digit != 0)
            {
                multiple.sign *= -1;
                addnum(rem, multiple, radix);
                multiple.sign *= -1;
            }
            rem.deref().exp++;

            ptrc.set(uint.of(digit));
            ptrc.advance(-1);
        }
        cdigits--;

        ptrc.advance();
        if (!ptrc.atBeginning())
        {
            //  memmove(c->mant, ptrc, (int)(cdigits * sizeof(MANTTYPE)));
            for (int k = 0; k < cdigits; k++) {
                c.mant.set(k, ptrc.at(k));
            }
        }

        if (cdigits == 0)
        {
            c.cdigit = 1;
            c.exp = 0;
        }
        else
        {
            c.cdigit = cdigits;
            c.exp -= cdigits;
            while (c.cdigit > 1 && c.mant.at(c.cdigit - 1).isZero())
            {
                c.cdigit--;
            }
        }

        pa.set(c);
    }

    //---------------------------------------------------------------------------
    //
    //    FUNCTION: equnum
    //
    //    ARGUMENTS: two numbers.
    //
    //    RETURN: Boolean
    //
    //    DESCRIPTION: Does the number equivalent of ( a == b )
    //    Only assumes that a and b are the same radix.
    //
    //---------------------------------------------------------------------------
    static boolean equnum(RatPack.NUMBER a, RatPack.NUMBER b)
    {
        int diff;
        UIntArrayPtr pa;
        UIntArrayPtr pb;
        int cdigits;
        int ccdigits;
        uint da;
        uint db;

        diff = (a.cdigit + a.exp) - (b.cdigit + b.exp);
        if (diff < 0)
        {
            // If the exponents are different, these are different numbers.
            return false;
        }
        else
        {
            if (diff > 0)
            {
                // If the exponents are different, these are different numbers.
                return false;
            }
            else
            {
                // OK the exponents match.
                pa = a.mant.pointer();
                pb = b.mant.pointer();
                pa.advance(a.cdigit - 1);
                pb.advance(b.cdigit - 1);
                cdigits = max(a.cdigit, b.cdigit);
                ccdigits = cdigits;

                // Loop over all digits until we run out of digits or there is a
                // difference in the digits.
                for (; cdigits > 0; cdigits--)
                {
                    da = ((cdigits > (ccdigits - a.cdigit)) ? pa.derefMinusMinus() : uint.ZERO);
                    db = ((cdigits > (ccdigits - b.cdigit)) ? pb.derefMinusMinus() : uint.ZERO);
                    if (da.compareTo(db) != 0)
                    {
                        return false;
                    }
                }

                // In this case, they are equal.
                return true;
            }
        }
    }

    //---------------------------------------------------------------------------
    //
    //    FUNCTION: lessnum
    //
    //    ARGUMENTS: two numbers.
    //
    //    RETURN: Boolean
    //
    //    DESCRIPTION: Does the number equivalent of ( abs(a) < abs(b) )
    //    Only assumes that a and b are the same radix, WARNING THIS IS AN.
    //    UNSIGNED COMPARE!
    //
    //---------------------------------------------------------------------------
    static boolean lessnum(RatPack.NUMBER a, RatPack.NUMBER b)
    {
        int diff = (a.cdigit + a.exp) - (b.cdigit + b.exp);
        if (diff < 0)
        {
            // The exponent of a is less than b
            return true;
        }
        if (diff > 0)
        {
            return false;
        }
        UIntArrayPtr pa = a.mant.pointer();
        UIntArrayPtr pb = b.mant.pointer();
        pa.advance( a.cdigit - 1 );
        pb.advance( b.cdigit - 1 );
        int cdigits = max(a.cdigit, b.cdigit);
        int ccdigits = cdigits;
        for (; cdigits > 0; cdigits--)
        {
            uint da = ((cdigits > (ccdigits - a.cdigit)) ? pa.derefMinusMinus() : uint.ZERO);
            uint db = ((cdigits > (ccdigits - b.cdigit)) ? pb.derefMinusMinus() : uint.ZERO);
            diff = da.subtract(db).toInt();
            if (diff != 0)
            {
                return (diff < 0);
            }
        }
        // In this case, they are equal.
        return false;
    }

    //----------------------------------------------------------------------------
    //
    //    FUNCTION: zernum
    //
    //    ARGUMENTS: number
    //
    //    RETURN: Boolean
    //
    //    DESCRIPTION: Does the number equivalent of ( !a )
    //
    //----------------------------------------------------------------------------
    static boolean zernum(RatPack.NUMBER a)
    {
        int length;
        UIntArrayPtr pcha;
        length = a.cdigit;
        pcha = a.mant.pointer();

        // loop over all the digits until you find a nonzero or until you run
        // out of digits
        while (length-- > 0)
        {
            if (pcha.derefPlusPlus().notEq(0))
            {
                // One of the digits isn't zero, therefore the number isn't zero
                return false;
            }
        }
        // All of the digits are zero, therefore the number is zero
        return true;
    }
}
