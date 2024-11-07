package mscalc.ratpack;

import java.util.Arrays;

public interface RatPack {
    // Internal log2(BASEX)
    int BASEXPWR = 31;

    // Internal radix used in calculations, hope to raise
    // this to 2^32 after solving scaling problems with
    // overflow detection esp. in mul
    int BASEX = 0x80000000;

    // TODO: See defs
    // typedef uint32_t MANTTYPE;
    // typedef uint64_t TWO_MANTTYPE;

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
    class NUMBER // _number
    {
        public int sign;   // The sign of the mantissa, +1, or -1
        public int cdigit; // The number of digits, or what passes for digits in the
        // radix being used.
        public int exp;    // The offset of digits from the radix point
        // (decimal point in radix 10)
        public int[] mant;
        // This is actually allocated as a continuation of the
        // NUMBER structure.

        public NUMBER() {
            this.sign = 0;
            this.cdigit = 0;
            this.exp = 0;
            this.mant = new int[0];
        }

        public NUMBER(int sign, int cdigit, int exp, int[] mant) {
            this.sign = sign;
            this.cdigit = cdigit;
            this.exp = exp;
            this.mant = (mant == null) ? null : mant.clone();
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


}
