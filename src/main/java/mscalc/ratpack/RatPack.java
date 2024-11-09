package mscalc.ratpack;

import mscalc.cpp.uint;
import mscalc.cpp.uintArray;

import java.util.Arrays;

import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.Num.zernum;
import static mscalc.ratpack.Support.g_ftrueinfinite;

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

        // DUPNUM Duplicates a number taking care of allocation and internals
    static NUMBER DUPNUM(NUMBER b) {
        NUMBER a = createnum(uint.of(b.cdigit));
        dupnum(a, b);
        return a;
    };

        // DUPRAT Duplicates a rational taking care of allocation and internals
    static RAT DUPRAT(RAT b) {
        RAT a = createrat();
        a.pp = DUPNUM(b.pp);
        a.pq = DUPNUM(b.pq);
        return a;
    };

enum NumberFormat
    {
        Float,      // returns floating point, or exponential if number is too big
        Scientific, // always returns scientific notation
        Engineering // always returns engineering notation such that exponent is a multiple of 3
    }

enum AngleType
    {
        Degrees, // Calculate trig using 360 degrees per revolution
        Radians, // Calculate trig using 2 pi radians per revolution
        Gradians // Calculate trig using 400 gradians per revolution
    }

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

        // Most significand digit
        public uint MSD() {
            return this.mant.at(this.cdigit - 1);
        }

        // LOG*RADIX calculates the integral portion of the log of a number in
        // the base currently being used, only accurate to within g_ratio
        public int LOGNUMRADIX() {
            return ((this.cdigit + this.exp) * g_ratio.get());
        }

        // LOG*2 calculates the integral portion of the log of a number in
        // the internal base being used, only accurate to within g_ratio
        public int LOGNUM2() {
            return (this.cdigit + this.exp);
        }

        // TRIMNUM ASSUMES the number is in radix form NOT INTERNAL BASEX!!!
        public void TRIMNUM(int precision) {
            if (!g_ftrueinfinite.get()) {
                int trim = this.cdigit - precision - g_ratio.get();
                if (trim > 1) {
                    // memmove((x) -> mant, & ((x) -> mant[trim]), sizeof(MANTTYPE) * ((x) -> cdigit - trim));
                    for (int k = 0; k < cdigit - trim; k++) {
                        mant.set(k, mant.at(k + trim));
                    }

                    this.cdigit -= trim;
                    this.exp += trim;
                }
            }
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

        public int LOGRATRADIX() {
            return pp.LOGNUMRADIX() - pq.LOGNUMRADIX();
        }

        public int LOGRAT2() {
            return pp.LOGNUM2() - pq.LOGNUM2();
        }

        // SIGN returns the sign of the rational
        public int SIGN() {
            return pp.sign * pq.sign;
        }

        // RENORMALIZE, gets the exponents non-negative.
        public void RENORMALIZE() {
            if (pp.exp < 0) {
                pq.exp -= pp.exp;
                pp.exp = 0;
            }
            if (pq.exp < 0) {
                pp.exp -= pq.exp;
                pq.exp = 0;
            }
        }

        // TRIMTOP ASSUMES the number is in INTERNAL BASEX!!!
        public void TRIMTOP(int precision) {
            if (!g_ftrueinfinite.get()) {
                int trim = pp.cdigit - (precision / g_ratio.get()) - 2;
                if (trim > 1) {
                    // memmove((x) -> pp -> mant, & ((x) -> pp -> mant[trim]), sizeof(MANTTYPE) * ((x) -> pp -> cdigit - trim));
                    for (int k = 0; k < pp.cdigit - trim; k++) {
                        pp.mant.set(k, pp.mant.at(k + trim));
                    }

                    pp.cdigit -= trim;
                    pp.exp += trim;
                }
                trim = Math.min(pp.exp, pq.exp);
                pp.exp -= trim;
                pq.exp -= trim;
            }
        }

        public boolean SMALL_ENOUGH_RAT(int precision) {
            return (zernum(pp) || (((pq.cdigit + pq.exp) - (pp.cdigit + pp.exp) - 1) * g_ratio.get() > precision));
        }
    }

    class TYLOR {
        public RAT xx = null;
        public NUMBER n2 = null;
        public RAT pret = null;
        public RAT thisterm = null;

        public TYLOR(RAT px) {
            xx = DUPRAT(px);
            // mulrat(&xx, *px, precision);
            // TODO: Finish
        }
    }
}
