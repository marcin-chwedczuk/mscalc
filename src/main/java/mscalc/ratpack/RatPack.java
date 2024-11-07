package mscalc.ratpack;

import mscalc.cpp.uint;
import mscalc.cpp.uintArray;

import java.util.Arrays;

import static mscalc.ratpack.Conv.*;

public interface RatPack {
    uint MAX_LONG_SIZE = uint.of(33); // Base 2 requires 32 'digits'

    // Internal log2(BASEX)
    int BASEXPWR = 31;

    // Internal radix used in calculations, hope to raise
    // this to 2^32 after solving scaling problems with
    // overflow detection esp. in mul
    uint BASEX = uint.of(0x80000000);

    // TODO: See defs
    // typedef uint32_t MANTTYPE;
    int SIZEOF_MANTTYPE = 4;
    // typedef uint64_t TWO_MANTTYPE;
    int SIZEOF_TWO_MANTTYPE = 8;

    enum NumberFormat
    {
        Float,      // returns floating point, or exponential if number is too big
        Scientific, // always returns scientific notation
        Engineering // always returns engineering notation such that exponent is a multiple of 3
    };

    enum AngleType
    {
        Degrees, // Calculate trig using 360 degrees per revolution
        Radians, // Calculate trig using 2 pi radians per revolution
        Gradians // Calculate trig using 400 gradians per revolution
    };

    // TODO:  NUMBER, *PNUMBER, **PPNUMBER;
    class NUMBER implements Cloneable// _number
    {
        public static int SIZE_OF = 4 + 4 + 4 + 0;

        public int sign;   // The sign of the mantissa, +1, or -1
        public int cdigit; // The number of digits, or what passes for digits in the
        // radix being used.
        public int exp;    // The offset of digits from the radix point
        // (decimal point in radix 10)

        // Array of UNSIGNED values;
        public uintArray mant;
        // This is actually allocated as a continuation of the
        // NUMBER structure.

        public NUMBER() {
            this.sign = 0;
            this.cdigit = 0;
            this.exp = 0;
            this.mant = new uintArray(0);
        }

        public NUMBER(int sign, int cdigit, int exp, uintArray mant) {
            this.sign = sign;
            this.cdigit = cdigit;
            this.exp = exp;
            this.mant = (mant == null) ? null : mant.clone();
        }

        @Override
        protected NUMBER clone() {
            return new NUMBER(sign, cdigit, exp, mant.clone());
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  RAT type is a representation radix  on 2 NUMBER types.
    //  pp/pq, where pp and pq are pointers to integral NUMBER types.
    //
    //-----------------------------------------------------------------------------
    class RAT // _rat
    {
        NUMBER pp;
        NUMBER pq;
    }

    // DUPNUM Duplicates a number taking care of allocation and internals
    static NUMBER DUPNUM(NUMBER b) {
        NUMBER a = createnum(b.cdigit);
        dupnum(a, b);
        return a;
    }

    // DUPRAT Duplicates a rational taking care of allocation and internals
    static RAT DUPRAT(RAT b) {
        RAT a = createrat();
        a.pp = DUPNUM(b.pp);
        a.pq = DUPNUM(b.pq);
        return a;
    }
}
