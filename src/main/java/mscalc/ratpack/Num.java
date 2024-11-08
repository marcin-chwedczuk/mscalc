package mscalc.ratpack;

import mscalc.cpp.ArrayPtrUInt;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.cpp.ulong;
import mscalc.ratpack.RatPack.NUMBER;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static mscalc.ratpack.Conv.createnum;
import static mscalc.ratpack.RatPack.DUPNUM;

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
    static void addnum(Ptr<NUMBER> pa, NUMBER b, uint radix)
    {
        if (b.cdigit > 1 || b.mant.at(0).notEq(0))
        { // If b is zero we are done.
            if (pa.deref().cdigit > 1 || pa.deref().mant.at(0).notEq(0))
            { // pa and b are both nonzero.
                _addnum(pa, b, radix);
            }
        else
            { // if pa is zero and b isn't just copy b.
                pa.set(DUPNUM(b));
            }
        }
    }

    static void _addnum(Ptr<NUMBER> pa, NUMBER b, uint radix)

    {
        NUMBER c = null; // c will contain the result.
        NUMBER a = null; // a is the dereferenced number pointer from *pa
        ArrayPtrUInt pcha;      // pcha is a pointer to the mantissa of a.
        ArrayPtrUInt pchb;      // pchb is a pointer to the mantissa of b.
        ArrayPtrUInt pchc;      // pchc is a pointer to the mantissa of c.
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

        c = createnum(uint.of(cdigits + 1));
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
    static void mulnum(Ptr<NUMBER> pa, NUMBER b, uint radix)
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
                pa.set(DUPNUM(b));
                pa.deref().sign *= sign;
            }
        }
        else
        { // But we do have to set the sign.
            pa.deref().sign *= b.sign;
        }
    }

    static void _mulnum(Ptr<NUMBER> pa, NUMBER b, uint radix)
    {
        NUMBER c = null;  // c will contain the result.
        NUMBER a = null;  // a is the dereferenced number pointer from *pa
        ArrayPtrUInt pcha;       // pcha is a pointer to the mantissa of a.
        ArrayPtrUInt pchb;       // pchb is a pointer to the mantissa of b.
        ArrayPtrUInt pchc;       // pchc is a pointer to the mantissa of c.
        ArrayPtrUInt pchcoffset; // pchcoffset, is the anchor location of the next
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
        c = createnum(uint.of(ibdigit + 1));
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
            pchc = pchcoffset.clone();
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
}
