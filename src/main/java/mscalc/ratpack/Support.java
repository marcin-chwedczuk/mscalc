package mscalc.ratpack;

import mscalc.cpp.ArrayPtrUInt;
import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.ceil;
import static mscalc.ratpack.Conv.*;
import static mscalc.ratpack.Exp._exprat;
import static mscalc.ratpack.Exp.lograt;
import static mscalc.ratpack.ITrans.asinrat;
import static mscalc.ratpack.Logic.remrat;
import static mscalc.ratpack.Num.equnum;
import static mscalc.ratpack.Num.zernum;
import static mscalc.ratpack.Rat.*;
import static mscalc.ratpack.RatPack.*;
import static mscalc.ratpack.Support.Global.num_one;
import static mscalc.ratpack.Support.Global.rat_one;

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
            int trim = g_ratio.get() * (Math.min((pp.cdigit + pp.exp), (pq.cdigit + pq.exp)) - 1) - precision;
            if (trim > g_ratio.get())
            {
                trim /= g_ratio.get();

                if (trim <= pp.exp)
                {
                    pp.exp -= trim;
                }
                else
                {
                    // memmove(pp->mant, &(pp->mant[trim - pp->exp]), sizeof(MANTTYPE) * (pp->cdigit - trim + pp->exp));
                    ArrayPtrUInt tmp = pp.mant.pointer();
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
                    ArrayPtrUInt tmp = pq.mant.pointer();
                    tmp.advance(trim - pq.exp);
                    for (int k = 0; k < pq.cdigit - trim + pq.exp; k++) {
                        pp.mant.set(k, tmp.derefPlusPlus());
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

        g_ratio.set( (int)Math.ceil(BASEXPWR / log2(radix.toULong().raw())) - 1 );

        Global.rat_nRadix = i32torat(radix.toInt());

        // Check to see what we have to recalculate and what we don't
        if (cbitsofprecision.get() < (g_ratio.get() * radix.toInt() * precision))
        {
            g_ftrueinfinite.set(false);

            num_one = i32tonum(1, BASEX);
            Global.num_two = i32tonum(2, BASEX);
            Global.num_five = i32tonum(5, BASEX);
            Global.num_six = i32tonum(6, BASEX);
            Global.num_ten = i32tonum(10, BASEX);
            Global.rat_six = i32torat(6);
            Global.rat_two = i32torat(2);
            Global.rat_zero = i32torat(0);
            rat_one = i32torat(1);
            Global.rat_neg_one = i32torat(-1);
            Global.rat_ten = i32torat(10);
            Global.rat_word = i32torat(0xffff);
            Global.rat_word = i32torat(0xff);
            Global.rat_400 = i32torat(400);
            Global.rat_360 = i32torat(360);
            Global.rat_200 = i32torat(200);
            Global.rat_180 = i32torat(180);
            Global.rat_max_exp = i32torat(100000);

            // 3248, is the max number for which calc is able to compute factorial, after that it is unable to compute due to overflow.
            // Hence restricted factorial range as at most 3248.Beyond that calc will throw overflow error immediately.
            Global.rat_max_fact = i32torat(3249);

            // -1000, is the min number for which calc is able to compute factorial, after that it takes too long to compute.
            Global.rat_min_fact = i32torat(-1000);

            Global.rat_smallest = DUPRAT(Global.rat_nRadix);
            Ptr<RAT> tmp = new Ptr<>(Global.rat_smallest);
            ratpowi32(tmp, -precision, precision);
            Global.rat_smallest = tmp.deref();

            Global.rat_negsmallest = DUPRAT(Global.rat_smallest);
            Global.rat_negsmallest.pp.sign = -1;

            if (Global.rat_half == null)
            {
                Global.rat_half = createrat();
                Global.rat_half.pp =  DUPNUM(num_one);
                Global.rat_half.pq = DUPNUM(Global.num_two);
            }

            if (Global.pt_eight_five == null)
            {
                Global.pt_eight_five = createrat();
                Global.pt_eight_five.pp = i32tonum(85, BASEX);
                Global.pt_eight_five.pq = i32tonum(100, BASEX);
            }

            Global.rat_qword = DUPRAT(Global.rat_two);
            Ptr<NUMBER> ppp = new Ptr<>(Global.rat_qword.pp);
            numpowi32(ppp, 64, BASEX, precision);
            Global.rat_qword.pp = ppp.deref();
            Ptr<RAT> ratp = new Ptr<>(Global.rat_qword);
            subrat(ratp, rat_one, precision);
            Global.rat_qword = ratp.deref();

            Global.rat_dword = DUPRAT(Global.rat_two);
            ppp = new Ptr<>(Global.rat_dword.pp);
            numpowi32(ppp, 32, BASEX, precision);
            Global.rat_dword.pp = ppp.deref();
            ratp = new Ptr<>(Global.rat_dword);
            subrat(ratp, rat_one, precision);
            Global.rat_dword = ratp.deref();

            Global.rat_max_i32 = DUPRAT(Global.rat_two);
            ppp = new Ptr<>(Global.rat_max_i32.pp);
            numpowi32(ppp, 31, BASEX, precision);
            Global.rat_max_i32.pp = ppp.deref();
            Global.rat_min_i32 = DUPRAT(Global.rat_max_i32);
            ratp = new Ptr<>(Global.rat_max_i32);
            subrat(ratp, rat_one, precision); // rat_max_i32 = 2^31 -1
            Global.rat_max_i32 = ratp.deref();

            Global.rat_min_i32.pp.sign *= -1; // rat_min_i32 = -2^31

            Global.rat_min_exp = DUPRAT(Global.rat_max_exp);
            Global.rat_min_exp.pp.sign *= -1;

            cbitsofprecision.set( g_ratio.get() * radix.toInt() * precision );

            // Apparently when dividing 180 by pi, another (internal) digit of
            // precision is needed.
            int extraPrecision = precision + g_ratio.get();
            Global.pi = DUPRAT(Global.rat_half);
            ratp = new Ptr<>(Global.pi);
            asinrat(ratp, radix, extraPrecision);
            mulrat(ratp, Global.rat_six, extraPrecision);
            Global.pi = ratp.deref();

            Global.two_pi = DUPRAT(Global.pi);
            Global.pi_over_two = DUPRAT(Global.pi);
            Global.one_pt_five_pi = DUPRAT(Global.pi);

            ratp = new Ptr<>(Global.two_pi);
            addrat(ratp, Global.pi, extraPrecision);
            Global.two_pi = ratp.deref();

            ratp = new Ptr<>(Global.pi_over_two);
            divrat(ratp, Global.rat_two, extraPrecision);
            Global.pi_over_two = ratp.deref();

            ratp = new Ptr<>(Global.one_pt_five_pi);
            addrat(ratp, Global.pi_over_two, extraPrecision);
            Global.one_pt_five_pi = ratp.deref();

            Global.e_to_one_half = DUPRAT(Global.rat_half);
            ratp = new Ptr<>(Global.e_to_one_half);
            _exprat(ratp, extraPrecision);
            Global.e_to_one_half = ratp.deref();

            Global.rat_exp = DUPRAT(rat_one);
            ratp = new Ptr<>(Global.rat_exp);
            _exprat(ratp, extraPrecision);
            Global.rat_exp = ratp.deref();

            // WARNING: remember lograt uses exponent constants calculated above...

            Global.ln_ten = DUPRAT(Global.rat_ten);
            ratp = new Ptr<>(Global.ln_ten);
            lograt(ratp, extraPrecision);
            Global.ln_ten = ratp.deref();

            Global.ln_two = DUPRAT(Global.rat_two);
            ratp = new Ptr<>(Global.ln_two);
            lograt(ratp, extraPrecision);
            Global.ln_two = ratp.deref();

            Global.rad_to_deg = i32torat(180);
            ratp = new Ptr<>(Global.rad_to_deg);
            divrat(ratp, Global.pi, extraPrecision);
            Global.rad_to_deg = ratp.deref();

            Global.rad_to_grad = i32torat(200);
            ratp = new Ptr<>(Global.rad_to_grad);
            divrat(ratp, Global.pi, extraPrecision);
            Global.rad_to_grad = ratp.deref();
        }
        else
        {
            _readconstants();

            Global.rat_smallest = DUPRAT(Global.rat_nRadix);
            Ptr<RAT> ratp = new Ptr<>(Global.rat_smallest);
            ratpowi32(ratp, -precision, precision);
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
        rat_one = new RAT(RatConst.init_p_rat_one, RatConst.init_q_rat_one);
        Global.rat_neg_one = new RAT(RatConst.init_p_rat_neg_one, RatConst.init_q_rat_neg_one);
        Global.rat_half = new RAT(RatConst.init_p_rat_half, RatConst.init_q_rat_half);
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
            flatrat(px, radix, precision);

            // Subtract the fractional part of the rational
            Ptr<RAT> pret = new Ptr<>(DUPRAT(px.deref()));
            remrat(pret, rat_one);

            subrat(px, pret.deref(), precision);

            // Simplify the value if possible to resolve rounding errors
            flatrat(px, radix, precision);
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
        addrat(rattmp, b, precision);

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
        addrat(rattmp, b, precision);
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
        addrat(rattmp, b, precision);
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
        addrat(rattmp, b, precision);
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
        addrat(rattmp, b, precision);
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
        addrat(rattmp, b, precision);

        boolean bret = !(zernum(rattmp.deref().pp));
        return (bret);
    }

    class Global {
        static NUMBER num_two = null;
        static NUMBER num_one = null;
        static NUMBER num_five = null;
        static NUMBER num_six = null;
        static NUMBER num_ten = null;

        static RAT ln_ten = null;
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
        static RAT rat_qword = null;
        static RAT rat_dword = null; // unsigned max ui32
        static RAT rat_word = null;
        static RAT rat_byte = null;
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
