package mscalc.engine.ratpack;

import mscalc.engine.Rational;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.cpp.UIntArray;

import java.util.function.Consumer;

import static mscalc.engine.ratpack.Num.addnum;
import static mscalc.engine.ratpack.Num.zernum;

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
        NUMBER a = Conv.createnum(uint.of(b.cdigit));
        Conv.dupnum(a, b);
        return a;
    };

        // DUPRAT Duplicates a rational taking care of allocation and internals
    static RAT DUPRAT(RAT b) {
        RAT a = Conv.createrat();
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
        Gradians; // Calculate trig using 400 gradians per revolution

        public static AngleType fromInt(int n) {
            return switch (n) {
                case 0 -> Degrees;
                case 1 -> Radians;
                case 2 -> Gradians;
                default -> throw new IllegalArgumentException("invalid value: " + n);
            };
        }
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
        public UIntArray mant;
        // This is actually allocated as a continuation of the
        // NUMBER structure.

        public NUMBER() {
            this.sign = 0;
            this.cdigit = 0;
            this.exp = 0;
            this.mant = new UIntArray(0);
        }

        public NUMBER(int sign, int cdigit, int exp, UIntArray mant) {
            this.sign = sign;
            this.cdigit = cdigit;
            this.exp = exp;
            this.mant = (mant == null) ? null : mant.copy();
        }

        @Override
        protected NUMBER clone() {
            return new NUMBER(sign, cdigit, exp, mant.copy());
        }

        // Most significand digit
        public uint MSD() {
            return this.mant.at(this.cdigit - 1);
        }

        // LOG*RADIX calculates the integral portion of the log of a number in
        // the base currently being used, only accurate to within g_ratio
        public int LOGNUMRADIX() {
            return ((this.cdigit + this.exp) * Conv.g_ratio.get());
        }

        // LOG*2 calculates the integral portion of the log of a number in
        // the internal base being used, only accurate to within g_ratio
        public int LOGNUM2() {
            return (this.cdigit + this.exp);
        }

        // TRIMNUM ASSUMES the number is in radix form NOT INTERNAL BASEX!!!
        public void TRIMNUM(int precision) {
            if (!Support.g_ftrueinfinite.get()) {
                int trim = this.cdigit - precision - Conv.g_ratio.get();
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

        @Override
        public String toString() {
            // Debug only
            return "" + Conv.numtoi32(this, uint.of(10));
        }
    }

    //-----------------------------------------------------------------------------
    //
    //  RAT type is a representation radix  on 2 NUMBER types.
    //  pp/pq, where pp and pq are pointers to integral NUMBER types.
    //
    //-----------------------------------------------------------------------------
    class RAT implements Cloneable // _rat
    {
        public NUMBER pp;
        public NUMBER pq;

        public RAT() { }
        public RAT(NUMBER pp, NUMBER pq) {
            this.pp = pp.clone();
            this.pq = pq.clone();
        }

        public void ppPtr(Consumer<Ptr<NUMBER>> work) {
            Ptr<RatPack.NUMBER> ppp = new Ptr<>(this.pp);
            work.accept(ppp);
            pp = ppp.deref();
        }

        public void pqPtr(Consumer<Ptr<NUMBER>> work) {
            Ptr<RatPack.NUMBER> ppq = new Ptr<>(this.pq);
            work.accept(ppq);
            pq = ppq.deref();
        }

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
            if (!Support.g_ftrueinfinite.get()) {
                int trim = pp.cdigit - (precision / Conv.g_ratio.get()) - 2;
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
            return zernum(pp) || (((pq.cdigit + pq.exp) - (pp.cdigit + pp.exp) - 1) * Conv.g_ratio.get() > precision);
        }

        @Override
        public RAT clone() {
            RAT a = Conv.createrat();
            a.pp = this.pp.clone();
            a.pq = this.pq.clone();
            return a;
        }

        public RAT toBaseXFrom(uint fromRadix) {
            RAT a = Conv.createrat();
            a.pp = Conv.numtonRadixx(this.pp.clone(), fromRadix);
            a.pq = Conv.numtonRadixx(this.pq.clone(), fromRadix);
            return a;
        }

        public RAT fromBaseXTo(uint targetRadix, int precision) {
            RAT a = Conv.createrat();
            a.pp = Conv.nRadixxtonum(this.pp.clone(), targetRadix, precision);
            a.pq = Conv.nRadixxtonum(this.pq.clone(), targetRadix, precision);
            return a;
        }
    }

    class TYLOR {
        private final int precision;
        public RAT xx = null;
        public NUMBER n2 = null;
        public RAT pret = null;
        public RAT thisterm = null;

        public TYLOR(RAT px, int precision) {
            this.precision = precision;

            Ptr<RAT> pxx = new Ptr<>(DUPRAT(px));
            Rat.mulrat(pxx, px, precision);
            this.xx = pxx.deref();

            this.pret = new RAT();
            pret.pp = Conv.i32tonum(0, BASEX);
            pret.pq = Conv.i32tonum(0, BASEX);
        }

        // INC(a) is the rational equivalent of a++
        // Check to see if we can avoid doing this the hard way.
        public static NUMBER INC(NUMBER a) {
            if (a.mant.at(0).compareTo(BASEX.subtract(uint.ONE)) < 0) {
                a.mant.set(0, a.mant.at(0).add(uint.ONE));
                return a;
            } else {
                Ptr<NUMBER> tmp = new Ptr<>(a);
                addnum(tmp, Support.Global.num_one, BASEX);
                return tmp.deref();
            }
        }

        public RAT RESULT() {
            Ptr<RAT> prat = new Ptr<>(pret);
            Support.trimit(prat, precision);
            this.pret = prat.deref();

            return this.pret;
        }

        // MULNUM(b) is the rational equivalent of thisterm *= b where thisterm is
        // a rational and b is a number, NOTE this is a mixed type operation for
        // efficiency reasons.
        public void MULNUM(NUMBER b) {
            Ptr<NUMBER> ppp = new Ptr<>(thisterm.pp);
            BaseX.mulnumx(ppp, b);
            thisterm.pp = ppp.deref();
        }

        // DIVNUM(b) is the rational equivalent of thisterm /= b where thisterm is
        // a rational and b is a number, NOTE this is a mixed type operation for
        // efficiency reasons.
        public void DIVNUM(NUMBER b) {
            Ptr<NUMBER> ppq = new Ptr<>(thisterm.pq);
            BaseX.mulnumx(ppq, b);
            thisterm.pq = ppq.deref();
        }

        // NEXTTERM(p,d) is the rational equivalent of
        // thisterm *= p
        // d    <d is usually an expansion of operations to get thisterm updated.>
        // pret += thisterm
        public void NEXTTERM(RAT p, Runnable d, int precision) {
            Ptr<RAT> pthisterm = new Ptr<>(thisterm);
            Rat.mulrat(pthisterm, p, precision);
            thisterm = pthisterm.deref();

            d.run();

            Ptr<RAT> ppret = new Ptr<>(pret);
            Rat.addrat(ppret, thisterm, precision);
            pret = ppret.deref();
        }
    }
}
