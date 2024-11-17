package mscalc.engine.ratpack;

import mscalc.engine.cpp.UIntArrayPtr;
import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack.NUMBER;
import mscalc.engine.ratpack.RatPack.RAT;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static mscalc.engine.ratpack.ITrans.asinrat;
import static mscalc.engine.ratpack.Num.equnum;
import static mscalc.engine.ratpack.Num.zernum;
import static mscalc.engine.ratpack.RatPack.*;
import static mscalc.engine.ratpack.Support.Global.*;

public interface Support {
    int RATIO_FOR_DECIMAL = 9;
    int DECIMAL = 10;
    int CALC_DECIMAL_DIGITS_DEFAULT = 32;

    AtomicInteger cbitsofprecision = new AtomicInteger(RATIO_FOR_DECIMAL * DECIMAL * CALC_DECIMAL_DIGITS_DEFAULT);

    AtomicBoolean g_ftrueinfinite = new AtomicBoolean(false); // Set to true if you don't want
    // chopping internally
    // precision used internally
    
    //---------------------------------------------------------------------------
    //
    //  FUNCTION: trimit
    //
    //  ARGUMENTS:  RAT *px, int32_t precision
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
    //  RETURN: none, modifies the pointed to RAT
    //
    //---------------------------------------------------------------------------
    static void trimit(Ptr<RAT> px, int precision)
    {
        if (!g_ftrueinfinite.get())
        {
            NUMBER pp = px.deref().pp;
            NUMBER pq = px.deref().pq;
            int trim = Conv.g_ratio.get() * (Math.min((pp.cdigit + pp.exp), (pq.cdigit + pq.exp)) - 1) - precision;
            if (trim > Conv.g_ratio.get())
            {
                trim /= Conv.g_ratio.get();

                if (trim <= pp.exp)
                {
                    pp.exp -= trim;
                }
                else
                {
                    // memmove(pp->mant, &(pp->mant[trim - pp->exp]), sizeof(MANTTYPE) * (pp->cdigit - trim + pp->exp));
                    UIntArrayPtr tmp = pp.mant.pointer();
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
                    UIntArrayPtr tmp = pq.mant.pointer();
                    tmp.advance(trim - pq.exp);
                    for (int k = 0; k < pq.cdigit - trim + pq.exp; k++) {
                        pq.mant.set(k, tmp.derefPlusPlus());
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

    private static double log2(double x) {
        // TODO: Try https://github.com/evanphx/ulysses-libc/blob/master/src/math/log2.c
        return Math.log10(x) / Math.log10(2);
    }

    //----------------------------------------------------------------------------
    //
    //  FUNCTION: ChangeConstants
    //
    //  ARGUMENTS:  base changing to, and precision to use.
    //
    //  RETURN: None
    //
    //  SIDE EFFECTS: sets a mess of constants.
    //
    //
    //----------------------------------------------------------------------------
    static void ChangeConstants(uint radix, int precision)
    {
        // ratio is set to the number of digits in the current radix, you can get
        // in the internal BASEX radix, this is important for length calculations
        // in translating from radix to BASEX and back.

        Conv.g_ratio.set( (int)Math.ceil(BASEXPWR / log2(radix.toULong().raw())) - 1 );

        Global.rat_nRadix = Conv.i32torat(radix.toInt());

        // Check to see what we have to recalculate and what we don't
        if (cbitsofprecision.get() < (Conv.g_ratio.get() * radix.toInt() * precision))
        {
            g_ftrueinfinite.set(false);

            num_one = Conv.i32tonum(1, BASEX);
            Global.num_two = Conv.i32tonum(2, BASEX);
            Global.num_five = Conv.i32tonum(5, BASEX);
            Global.num_six = Conv.i32tonum(6, BASEX);
            Global.num_ten = Conv.i32tonum(10, BASEX);
            Global.rat_six = Conv.i32torat(6);
            Global.rat_two = Conv.i32torat(2);
            Global.rat_zero = Conv.i32torat(0);
            rat_one = Conv.i32torat(1);
            Global.rat_neg_one = Conv.i32torat(-1);
            Global.rat_ten = Conv.i32torat(10);
            Global.rat_word = Conv.i32torat(0xffff);
            Global.rat_word = Conv.i32torat(0xff);
            Global.rat_400 = Conv.i32torat(400);
            Global.rat_360 = Conv.i32torat(360);
            Global.rat_200 = Conv.i32torat(200);
            Global.rat_180 = Conv.i32torat(180);
            Global.rat_max_exp = Conv.i32torat(100000);

            // 3248, is the max number for which calc is able to compute factorial, after that it is unable to compute due to overflow.
            // Hence restricted factorial range as at most 3248.Beyond that calc will throw overflow error immediately.
            Global.rat_max_fact = Conv.i32torat(3249);

            // -1000, is the min number for which calc is able to compute factorial, after that it takes too long to compute.
            Global.rat_min_fact = Conv.i32torat(-1000);

            Global.rat_smallest = DUPRAT(Global.rat_nRadix);
            Ptr<RAT> tmp = new Ptr<>(Global.rat_smallest);
            Conv.ratpowi32(tmp, -precision, precision);
            Global.rat_smallest = tmp.deref();

            Global.rat_negsmallest = DUPRAT(Global.rat_smallest);
            Global.rat_negsmallest.pp.sign = -1;

            if (rat_half == null)
            {
                rat_half = Conv.createrat();
                rat_half.pp =  DUPNUM(num_one);
                rat_half.pq = DUPNUM(Global.num_two);
            }

            if (Global.pt_eight_five == null)
            {
                Global.pt_eight_five = Conv.createrat();
                Global.pt_eight_five.pp = Conv.i32tonum(85, BASEX);
                Global.pt_eight_five.pq = Conv.i32tonum(100, BASEX);
            }

            Global.rat_qword = DUPRAT(Global.rat_two);
            Ptr<NUMBER> ppp = new Ptr<>(Global.rat_qword.pp);
            Conv.numpowi32(ppp, 64, BASEX, precision);
            Global.rat_qword.pp = ppp.deref();
            Ptr<RAT> ratp = new Ptr<>(Global.rat_qword);
            Rat.subrat(ratp, rat_one, precision);
            Global.rat_qword = ratp.deref();

            Global.rat_dword = DUPRAT(Global.rat_two);
            ppp = new Ptr<>(Global.rat_dword.pp);
            Conv.numpowi32(ppp, 32, BASEX, precision);
            Global.rat_dword.pp = ppp.deref();
            ratp = new Ptr<>(Global.rat_dword);
            Rat.subrat(ratp, rat_one, precision);
            Global.rat_dword = ratp.deref();

            Global.rat_max_i32 = DUPRAT(Global.rat_two);
            ppp = new Ptr<>(Global.rat_max_i32.pp);
            Conv.numpowi32(ppp, 31, BASEX, precision);
            Global.rat_max_i32.pp = ppp.deref();
            Global.rat_min_i32 = DUPRAT(Global.rat_max_i32);
            ratp = new Ptr<>(Global.rat_max_i32);
            Rat.subrat(ratp, rat_one, precision); // rat_max_i32 = 2^31 -1
            Global.rat_max_i32 = ratp.deref();

            Global.rat_min_i32.pp.sign *= -1; // rat_min_i32 = -2^31

            Global.rat_min_exp = DUPRAT(Global.rat_max_exp);
            Global.rat_min_exp.pp.sign *= -1;

            cbitsofprecision.set( Conv.g_ratio.get() * radix.toInt() * precision );

            // Apparently when dividing 180 by pi, another (internal) digit of
            // precision is needed.
            int extraPrecision = precision + Conv.g_ratio.get();
            Global.pi = DUPRAT(rat_half);
            ratp = new Ptr<>(Global.pi);
            asinrat(ratp, radix, extraPrecision);
            Rat.mulrat(ratp, Global.rat_six, extraPrecision);
            Global.pi = ratp.deref();

            Global.two_pi = DUPRAT(Global.pi);
            Global.pi_over_two = DUPRAT(Global.pi);
            Global.one_pt_five_pi = DUPRAT(Global.pi);

            ratp = new Ptr<>(Global.two_pi);
            Rat.addrat(ratp, Global.pi, extraPrecision);
            Global.two_pi = ratp.deref();

            ratp = new Ptr<>(Global.pi_over_two);
            Rat.divrat(ratp, Global.rat_two, extraPrecision);
            Global.pi_over_two = ratp.deref();

            ratp = new Ptr<>(Global.one_pt_five_pi);
            Rat.addrat(ratp, Global.pi_over_two, extraPrecision);
            Global.one_pt_five_pi = ratp.deref();

            Global.e_to_one_half = DUPRAT(rat_half);
            ratp = new Ptr<>(Global.e_to_one_half);
            Exp._exprat(ratp, radix, extraPrecision);
            Global.e_to_one_half = ratp.deref();

            Global.rat_exp = DUPRAT(rat_one);
            ratp = new Ptr<>(Global.rat_exp);
            Exp._exprat(ratp, radix, extraPrecision);
            Global.rat_exp = ratp.deref();

            // WARNING: remember lograt uses exponent constants calculated above...

            Global.ln_ten = DUPRAT(Global.rat_ten);
            ratp = new Ptr<>(Global.ln_ten);
            Exp.lograt(ratp, extraPrecision);
            Global.ln_ten = ratp.deref();

            Global.ln_two = DUPRAT(Global.rat_two);
            ratp = new Ptr<>(Global.ln_two);
            Exp.lograt(ratp, extraPrecision);
            Global.ln_two = ratp.deref();

            Global.rad_to_deg = Conv.i32torat(180);
            ratp = new Ptr<>(Global.rad_to_deg);
            Rat.divrat(ratp, Global.pi, extraPrecision);
            Global.rad_to_deg = ratp.deref();

            Global.rad_to_grad = Conv.i32torat(200);
            ratp = new Ptr<>(Global.rad_to_grad);
            Rat.divrat(ratp, Global.pi, extraPrecision);
            Global.rad_to_grad = ratp.deref();
        }
        else
        {
            _readconstants();

            Global.rat_smallest = DUPRAT(Global.rat_nRadix);
            Ptr<RAT> ratp = new Ptr<>(Global.rat_smallest);
            Conv.ratpowi32(ratp, -precision, precision);
            Global.rat_smallest = ratp.deref();

            Global.rat_negsmallest = DUPRAT(Global.rat_smallest);
            Global.rat_negsmallest.pp.sign = -1;
        }
    }

    static void _readconstants() {
        num_one = RatConst.init_num_one.clone();
        Global.num_two = RatConst.init_num_two.clone();
        Global.num_five = RatConst.init_num_five.clone();
        Global.num_six = RatConst.init_num_six.clone();
        Global.num_ten = RatConst.init_num_six.clone();

        Global.pt_eight_five = new RAT(RatConst.init_p_pt_eight_five, RatConst.init_q_pt_eight_five);
        Global.rat_six = new RAT(RatConst.init_p_rat_six, RatConst.init_q_rat_six);
        Global.rat_two = new RAT(RatConst.init_p_rat_two, RatConst.init_q_rat_two);
        Global.rat_zero = new RAT(RatConst.init_p_rat_zero, RatConst.init_q_rat_zero);
        Global.rat_one = new RAT(RatConst.init_p_rat_one, RatConst.init_q_rat_one);
        Global.rat_neg_one = new RAT(RatConst.init_p_rat_neg_one, RatConst.init_q_rat_neg_one);
        rat_half = new RAT(RatConst.init_p_rat_half, RatConst.init_q_rat_half);
        Global.rat_ten = new RAT(RatConst.init_p_rat_ten, RatConst.init_q_rat_ten);
        Global.pi = new RAT(RatConst.init_p_pi, RatConst.init_q_pi);
        Global.two_pi = new RAT(RatConst.init_p_two_pi, RatConst.init_q_two_pi);
        Global.pi_over_two = new RAT(RatConst.init_p_pi_over_two, RatConst.init_q_pi_over_two);
        Global.one_pt_five_pi = new RAT(RatConst.init_p_one_pt_five_pi, RatConst.init_q_one_pt_five_pi);
        Global.e_to_one_half = new RAT(RatConst.init_p_e_to_one_half, RatConst.init_q_e_to_one_half);
        Global.rat_exp = new RAT(RatConst.init_p_rat_exp, RatConst.init_q_rat_exp);
        Global.ln_ten = new RAT(RatConst.init_p_ln_ten, RatConst.init_q_ln_ten);
        Global.ln_two = new RAT(RatConst.init_p_ln_two, RatConst.init_q_ln_two);
        Global.rad_to_deg = new RAT(RatConst.init_p_rad_to_deg, RatConst.init_q_rad_to_deg);
        Global.rad_to_grad = new RAT(RatConst.init_p_rad_to_grad, RatConst.init_q_rad_to_grad);
        Global.rat_qword = new RAT(RatConst.init_p_rat_qword, RatConst.init_q_rat_qword);
        Global.rat_dword = new RAT(RatConst.init_p_rat_dword, RatConst.init_q_rat_dword);
        Global.rat_word = new RAT(RatConst.init_p_rat_word, RatConst.init_q_rat_word);
        Global.rat_byte = new RAT(RatConst.init_p_rat_byte, RatConst.init_q_rat_byte);
        Global.rat_360 = new RAT(RatConst.init_p_rat_360, RatConst.init_q_rat_360);
        Global.rat_400 = new RAT(RatConst.init_p_rat_400, RatConst.init_q_rat_400);
        Global.rat_180 = new RAT(RatConst.init_p_rat_180, RatConst.init_q_rat_180);
        Global.rat_200 = new RAT(RatConst.init_p_rat_200, RatConst.init_q_rat_200);
        Global.rat_smallest = new RAT(RatConst.init_p_rat_smallest, RatConst.init_q_rat_smallest);
        Global.rat_negsmallest = new RAT(RatConst.init_p_rat_negsmallest, RatConst.init_q_rat_negsmallest);
        Global.rat_max_exp = new RAT(RatConst.init_p_rat_max_exp, RatConst.init_q_rat_max_exp);
        Global.rat_min_exp = new RAT(RatConst.init_p_rat_min_exp, RatConst.init_q_rat_min_exp);
        Global.rat_max_fact = new RAT(RatConst.init_p_rat_max_fact, RatConst.init_q_rat_max_fact);
        Global.rat_min_fact = new RAT(RatConst.init_p_rat_min_fact, RatConst.init_q_rat_min_fact);
        Global.rat_min_i32 = new RAT(RatConst.init_p_rat_min_i32, RatConst.init_q_rat_min_i32);
        Global.rat_max_i32 = new RAT(RatConst.init_p_rat_max_i32, RatConst.init_q_rat_max_i32);
    }

    //----------------------------------------------------------------------------
    //
    //  FUNCTION: intrat
    //
    //  ARGUMENTS:  pointer to x PRAT representation of number
    //
    //  RETURN: no return value x PRAT is smashed with integral number
    //
    //
    //----------------------------------------------------------------------------
    static void intrat(Ptr<RAT> px, uint radix, int precision)
    {
        // Only do the intrat operation if number is nonzero.
        // and only if the bottom part is not one.
        if (!zernum(px.deref().pp) && !equnum(px.deref().pq, num_one))
        {
            Conv.flatrat(px, radix, precision);

            // Subtract the fractional part of the rational
            Ptr<RAT> pret = new Ptr<>(DUPRAT(px.deref()));
            Logic.remrat(pret, rat_one);

            Rat.subrat(px, pret.deref(), precision);

            // Simplify the value if possible to resolve rounding errors
            Conv.flatrat(px, radix, precision);
        }
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: rat_equ
    //
    //  ARGUMENTS:  PRAT a and PRAT b
    //
    //  RETURN: true if equal false otherwise.
    //
    //
    //---------------------------------------------------------------------------
    static boolean rat_equ(RAT a, RAT b, int precision)
    {
        Ptr<RAT> rattmp = new Ptr<>(DUPRAT(a));

        rattmp.deref().pp.sign *= -1;
        Rat.addrat(rattmp, b, precision);

        boolean bret = zernum(rattmp.deref().pp);
        return (bret);
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: rat_ge
    //
    //  ARGUMENTS:  PRAT a, PRAT b and int32_t precision
    //
    //  RETURN: true if a is greater than or equal to b
    //
    //
    //---------------------------------------------------------------------------
    static boolean rat_ge(RAT a, RAT b, int precision)
    {
        Ptr<RAT> rattmp = new Ptr<>(DUPRAT(a));

        b.pp.sign *= -1;
        Rat.addrat(rattmp, b, precision);
        b.pp.sign *= -1;

        boolean bret = (zernum(rattmp.deref().pp) || rattmp.deref().SIGN() == 1);
        return (bret);
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: rat_gt
    //
    //  ARGUMENTS:  PRAT a and PRAT b
    //
    //  RETURN: true if a is greater than b
    //
    //
    //---------------------------------------------------------------------------
    static boolean rat_gt(RAT a, RAT b, int precision)
    {
        Ptr<RAT> rattmp = new Ptr<>(DUPRAT(a));
        b.pp.sign *= -1;
        Rat.addrat(rattmp, b, precision);
        b.pp.sign *= -1;

        boolean bret = (!zernum(rattmp.deref().pp) && rattmp.deref().SIGN() == 1);
        return (bret);
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: rat_le
    //
    //  ARGUMENTS:  PRAT a, PRAT b and int32_t precision
    //
    //  RETURN: true if a is less than or equal to b
    //
    //
    //---------------------------------------------------------------------------
    static boolean rat_le(RAT a, RAT b, int precision)
    {
        Ptr<RAT> rattmp = new Ptr<>(DUPRAT(a));
        b.pp.sign *= -1;
        Rat.addrat(rattmp, b, precision);
        b.pp.sign *= -1;

        boolean bret = (zernum(rattmp.deref().pp) || rattmp.deref().SIGN() == -1);
        return (bret);
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: rat_lt
    //
    //  ARGUMENTS:  PRAT a, PRAT b and int32_t precision
    //
    //  RETURN: true if a is less than b
    //
    //
    //---------------------------------------------------------------------------
    static boolean rat_lt(RAT a, RAT b, int precision)
    {
        Ptr<RAT> rattmp = new Ptr<>(DUPRAT(a));
        b.pp.sign *= -1;
        Rat.addrat(rattmp, b, precision);
        b.pp.sign *= -1;

        boolean bret = (!zernum(rattmp.deref().pp) && rattmp.deref().SIGN() == -1);
        return (bret);
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: rat_neq
    //
    //  ARGUMENTS:  PRAT a and PRAT b
    //
    //  RETURN: true if a is not equal to b
    //
    //
    //---------------------------------------------------------------------------
    static boolean rat_neq(RAT a, RAT b, int precision)
    {
        Ptr<RAT> rattmp = new Ptr<>(DUPRAT(a));
        rattmp.deref().pp.sign *= -1;
        Rat.addrat(rattmp, b, precision);

        boolean bret = !(zernum(rattmp.deref().pp));
        return (bret);
    }

    //---------------------------------------------------------------------------
    //
    //  function: scale
    //
    //  ARGUMENTS:  pointer to x PRAT representation of number, and scaling factor
    //
    //  RETURN: no return, value x PRAT is smashed with a scaled number in the
    //          range of the scalefact.
    //
    //---------------------------------------------------------------------------
    static void scale(Ptr<RAT> px, RAT scalefact, uint radix, int precision)
    {
        Ptr<RAT> pret = new Ptr<>(DUPRAT(px.deref()));

        // Logscale is a quick way to tell how much extra precision is needed for
        // scaling by scalefact.
        int logscale = Conv.g_ratio.get() * ((pret.deref().pp.cdigit + pret.deref().pp.exp) - (pret.deref().pq.cdigit + pret.deref().pq.exp));
        if (logscale > 0)
        {
            precision += logscale;
        }

        Rat.divrat(pret, scalefact, precision);
        intrat(pret, radix, precision);
        Rat.mulrat(pret, scalefact, precision);
        pret.deref().pp.sign *= -1;
        Rat.addrat(px, pret.deref(), precision);
    }

    //---------------------------------------------------------------------------
    //
    //  function: scale2pi
    //
    //  ARGUMENTS:  pointer to x PRAT representation of number
    //
    //  RETURN: no return, value x PRAT is smashed with a scaled number in the
    //          range of 0..2pi
    //
    //---------------------------------------------------------------------------
    static void scale2pi(Ptr<RAT> px, uint radix, int precision)
    {
        Ptr<RAT> pret = new Ptr<>();
        Ptr<RAT> my_two_pi = new Ptr<>();
        pret.set(DUPRAT(px.deref()));

        // Logscale is a quick way to tell how much extra precision is needed for
        // scaling by 2 pi.
        int logscale = Conv.g_ratio.get() * ((pret.deref().pp.cdigit + pret.deref().pp.exp) - (pret.deref().pq.cdigit + pret.deref().pq.exp));
        if (logscale > 0)
        {
            precision += logscale;

            my_two_pi.set(DUPRAT(rat_half));
            asinrat(my_two_pi, radix, precision);
            Rat.mulrat(my_two_pi, rat_six, precision);
            Rat.mulrat(my_two_pi, rat_two, precision);
        }
        else
        {
            my_two_pi.set(DUPRAT(two_pi));
            logscale = 0;
        }

        Rat.divrat(pret, my_two_pi.deref(), precision);
        intrat(pret, radix, precision);
        Rat.mulrat(pret, my_two_pi.deref(), precision);
        pret.deref().pp.sign *= -1;
        Rat.addrat(px, pret.deref(), precision);
    }

    //---------------------------------------------------------------------------
    //
    //  FUNCTION: inbetween
    //
    //  ARGUMENTS:  PRAT *px, and PRAT range.
    //
    //  RETURN: none, changes *px to -/+range, if px is outside -range..+range
    //
    //---------------------------------------------------------------------------
    static void inbetween(Ptr<RAT> px, RAT range, int precision)
    {
        if (rat_gt(px.deref(), range, precision))
        {
            px.set(DUPRAT(range));
        }
        else
        {
            range.pp.sign *= -1;
            if (rat_lt(px.deref(), range, precision))
            {
                px.set(DUPRAT(range));
            }
            range.pp.sign *= -1;
        }
    }

    class Global {
        public static NUMBER num_two = null;
        public static NUMBER num_one = null;
        public static NUMBER num_five = null;
        public static NUMBER num_six = null;
        public static NUMBER num_ten = null;

        public static RAT ln_ten = null;
        static RAT ln_two = null;
        static RAT rat_zero = null;
        static RAT rat_one = null;
        static RAT rat_neg_one = null;
        static RAT rat_two = null;
        static RAT rat_six = null;
        static RAT rat_half = null;
        static RAT rat_ten = null;
        static RAT pt_eight_five = null;
        static RAT pi = null;
        static RAT pi_over_two = null;
        static RAT two_pi = null;
        static RAT one_pt_five_pi = null;
        static RAT e_to_one_half = null;
        static RAT rat_exp = null;
        static RAT rad_to_deg = null;
        static RAT rad_to_grad = null;
        public static RAT rat_qword = null;
        public static RAT rat_dword = null; // unsigned max ui32
        public static RAT rat_word = null;
        public static RAT rat_byte = null;
        static RAT rat_360 = null;
        static RAT rat_400 = null;
        static RAT rat_180 = null;
        static RAT rat_200 = null;
        static RAT rat_nRadix = null;
        static RAT rat_smallest = null;
        static RAT rat_negsmallest = null;
        static RAT rat_max_exp = null;
        static RAT rat_min_exp = null;
        static RAT rat_max_fact = null;
        static RAT rat_min_fact = null;
        static RAT rat_min_i32 = null; // min signed i32
        static RAT rat_max_i32 = null; // max signed i32
    }
}
