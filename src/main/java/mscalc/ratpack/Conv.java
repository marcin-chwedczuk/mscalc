package mscalc.ratpack;

import mscalc.cpp.*;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import java.util.concurrent.atomic.AtomicInteger;

import static mscalc.WinErrorCrossPlatform.SUCCEEDED;
import static mscalc.WinErrorCrossPlatform.S_OK;
import static mscalc.ratpack.BaseX.mulnumx;
import static mscalc.ratpack.BaseX.numpowi32x;
import static mscalc.ratpack.CalcErr.CALC_E_INVALIDRANGE;
import static mscalc.ratpack.CalcErr.CALC_E_OUTOFMEMORY;
import static mscalc.ratpack.Num.addnum;
import static mscalc.ratpack.RatPack.*;

public interface Conv {
    int UINT32_MAX = (int) 0xffffffff;

    int MAX_ZEROS_AFTER_DECIMAL = 2;

    // digits 0..64 used by bases 2 .. 64
    String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_@";

    // ratio of internal 'digits' to output 'digits'
    // Calculated elsewhere as part of initialization and when base is changed
    AtomicInteger g_ratio = new AtomicInteger(); // int(log(2L^BASEXPWR)/log(radix))
    // Default decimal separator
    Ptr<Character> g_decimalSeparator = new Ptr<>('.');

    int CALC_INTSAFE_E_ARITHMETIC_OVERFLOW = (0x80070216); // 0x216 = 534 = ERROR_ARITHMETIC_OVERFLOW
    int CALC_ULONG_ERROR = ((int) 0xffffffff);

    static int Calc_ULongAdd(uint ulAugend, uint ulAddend, Ptr<uint> pulResult) {
        int hr = CALC_INTSAFE_E_ARITHMETIC_OVERFLOW;
        pulResult.set(uint.of(CALC_ULONG_ERROR));

        if (ulAugend.add(ulAddend).compareTo(ulAugend) >= 0) {
            pulResult.set(ulAugend.add(ulAddend));
            hr = S_OK;
        }

        return hr;
    }

    static int Calc_ULongLongToULong(ulong ullOperand, Ptr<uint> pulResult) {
        int hr = CALC_INTSAFE_E_ARITHMETIC_OVERFLOW;
        pulResult.set(uint.of(CALC_ULONG_ERROR));

        if (ullOperand.compareTo(0xffffffffL) <= 0) {
            pulResult.set(ullOperand.toUInt());
            hr = S_OK;
        }

        return hr;
    }

    static int Calc_ULongMult(uint ulMultiplicand, uint ulMultiplier, Ptr<uint> pulResult) {
        ulong ull64Result = ulMultiplicand.toULong().multiply(ulMultiplier.toULong());
        return Calc_ULongLongToULong(ull64Result, pulResult);
    }

    // Used to strip trailing zeros, and prevent combinatorial explosions
    // TODO: bool stripzeroesnum(_Inout_ PNUMBER pnum, int32_t starting);

    static void SetDecimalSeparator(char decimalSeparator) {
        g_decimalSeparator.set(decimalSeparator);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: _dupnum
    //
    //    ARGUMENTS: pointer to a number, pointer to a number
    //
    //    RETURN: None
    //
    //    DESCRIPTION: Copies the source to the destination
    //
    //-----------------------------------------------------------------------------
    static void dupnum(NUMBER dest, NUMBER src) {
        dest.sign = src.sign;
        for (int i = 0; i < src.mant.length(); i++) {
            dest.mant.set(i, src.mant.at(i));
        }
        dest.exp = src.exp;
        dest.cdigit = src.cdigit;
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: _destroynum
    //
    //    ARGUMENTS: pointer to a number
    //
    //    RETURN: None
    //
    //    DESCRIPTION: Deletes the number and associated allocation
    //
    //-----------------------------------------------------------------------------
    static void destroynum(NUMBER num) {
        // No OP on Java
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: _destroyrat
    //
    //    ARGUMENTS: pointer to a rational
    //
    //    RETURN: None
    //
    //    DESCRIPTION: Deletes the rational and associated
    //    allocations.
    //
    //-----------------------------------------------------------------------------
    static void destroyrat(RAT rat) {
        // No OP on java
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: _createnum
    //
    //    ARGUMENTS: size of number in 'digits'
    //
    //    RETURN: pointer to a number
    //
    //    DESCRIPTION: allocates and zeros out number type.
    //
    //-----------------------------------------------------------------------------
    static NUMBER createnum(uint size) {
        NUMBER pnumret = null;
        Ptr<uint> cbAlloc = new Ptr<>();

        // sizeof( MANTTYPE ) is the size of a 'digit'
        if (SUCCEEDED(Calc_ULongAdd(size, uint.of(1), cbAlloc))
                && SUCCEEDED(Calc_ULongMult(cbAlloc.deref(), uint.of(SIZEOF_MANTTYPE), cbAlloc))
                && SUCCEEDED(Calc_ULongAdd(cbAlloc.deref(), uint.of(NUMBER.SIZE_OF), cbAlloc))) {
            try {
                pnumret = new NUMBER();
                pnumret.mant = new uintArray(size.raw() + 1); // TODO: Is this one really needed?
            } catch (OutOfMemoryError e) {
                throw new ErrorCodeException(CALC_E_OUTOFMEMORY);
            }
        } else {
            throw new ErrorCodeException(CALC_E_INVALIDRANGE);
        }

        return (pnumret);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: _createrat
    //
    //    ARGUMENTS: none
    //
    //    RETURN: pointer to a rational
    //
    //    DESCRIPTION: allocates a rational structure but does not
    //    allocate the numbers that make up the rational p over q
    //    form.  These number pointers are left pointing to null.
    //
    //-----------------------------------------------------------------------------
    static RAT createrat() {
        RAT prat;

        try {
            prat = new RAT();
        } catch (OutOfMemoryError e) {
            throw new ErrorCodeException(CALC_E_OUTOFMEMORY);
        }

        prat.pp = null;
        prat.pq = null;
        return (prat);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: numtorat
    //
    //    ARGUMENTS: pointer to a number, radix number is in.
    //
    //    RETURN: Rational representation of number.
    //
    //    DESCRIPTION: The rational representation of the number
    //    is guaranteed to be in the form p (number with internal
    //    base   representation) over q (number with internal base
    //    representation)  Where p and q are integers.
    //
    //-----------------------------------------------------------------------------
    static RAT numtorat(NUMBER pin, uint radix) {
        NUMBER pnRadixn = DUPNUM(pin);

        NUMBER qnRadixn = i32tonum(1, radix);

        // Ensure p and q start out as integers.
        if (pnRadixn.exp < 0) {
            qnRadixn.exp -= pnRadixn.exp;
            pnRadixn.exp = 0;
        }

        RAT pout = createrat();

        // There is probably a better way to do this.
        pout.pp = numtonRadixx(pnRadixn, radix);
        pout.pq = numtonRadixx(qnRadixn, radix);

        destroynum(pnRadixn);
        destroynum(qnRadixn);

        return (pout);
    }


    //-----------------------------------------------------------------------------
//
//    FUNCTION: numtonRadixx
//
//    ARGUMENTS: pointer to a number, radix of that number.
//
//    RETURN: number representation in internal radix.
//
//    DESCRIPTION: Does a radix conversion on a number from
//    specified radix to requested radix.  Assumes the radix
//    specified is the radix of the number passed in.
//
//-----------------------------------------------------------------------------
    static NUMBER numtonRadixx(NUMBER a, uint radix) {
        Ptr<NUMBER> pnumret = new Ptr<>(i32tonum(0, BASEX)); // pnumret is the number in internal form.
        Ptr<NUMBER> num_radix = new Ptr<>(i32tonum(radix.toInt(), BASEX));
        ArrayPtrUInt ptrdigit = a.mant.pointer(); // pointer to digit being worked on.

        // Digits are in reverse order, back over them LSD first.
        ptrdigit.advance(a.cdigit - 1);

        NUMBER thisdigit = null; // thisdigit holds the current digit of a
        for (int idigit = 0; idigit < a.cdigit; idigit++) {
            mulnumx(pnumret, num_radix.deref());
            // WARNING:
            // This should just smack in each digit into a 'special' thisdigit.
            // and not do the overhead of recreating the number type each time.
            thisdigit = i32tonum(ptrdigit.deref().raw(), BASEX);
            ptrdigit.advance(-1);

            addnum(pnumret, thisdigit, BASEX);
            destroynum(thisdigit);
        }

        // Calculate the exponent of the external base for scaling.
        numpowi32x(num_radix, a.exp);

        // ... and scale the result.
        mulnumx(pnumret, num_radix.deref());

        // And propagate the sign.
        pnumret.deref().sign = a.sign;

        return (pnumret.deref());
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: i32tonum
    //
    //    ARGUMENTS: int32_t input and radix requested.
    //
    //    RETURN: number
    //
    //    DESCRIPTION: Returns a number representation in the
    //    base   requested of the int32_t value passed in.
    //
    //-----------------------------------------------------------------------------
    static NUMBER i32tonum(int ini32, uint radix) {
        NUMBER pnumret = createnum(MAX_LONG_SIZE);

        ArrayPtrUInt pmant = pnumret.mant.pointer();
        pnumret.cdigit = 0;
        pnumret.exp = 0;

        if (ini32 < 0) {
            pnumret.sign = -1;
            ini32 *= -1;
        } else {
            pnumret.sign = 1;
        }

        do {
            pmant.set(uint.of(ini32).modulo(radix));
            pmant.advance();

            ini32 = uint.of(ini32).divide(radix).raw();
            pnumret.cdigit++;
        } while (ini32 != 0);

        return (pnumret);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: numtoi32
    //
    //    ARGUMENTS: number input and base of that number.
    //
    //    RETURN: int32_t
    //
    //    DESCRIPTION: returns the int32_t representation of the
    //    number input.  Assumes that the number is really in the
    //    base   claimed.
    //
    //-----------------------------------------------------------------------------
    static int numtoi32(NUMBER pnum, uint radix)
    {
        int lret = 0;

        ArrayPtrUInt pmant = pnum.mant.pointer();
        pmant.advance(pnum.cdigit - 1);

        int expt = pnum.exp;
        for (int length = pnum.cdigit; length > 0 && length + expt > 0; length--)
        {
            lret *= radix.toInt();
            lret += pmant.derefMinusMinus().toInt();
        }

        while (expt-- > 0)
        {
            lret *= radix.toInt();
        }

        lret *= pnum.sign;

        return lret;
    }
}
