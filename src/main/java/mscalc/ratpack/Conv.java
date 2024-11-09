package mscalc.ratpack;

import mscalc.cpp.*;
import mscalc.ratpack.RatPack.NUMBER;
import mscalc.ratpack.RatPack.RAT;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;
import static mscalc.WinErrorCrossPlatform.SUCCEEDED;
import static mscalc.WinErrorCrossPlatform.S_OK;
import static mscalc.ratpack.BaseX.*;
import static mscalc.ratpack.CalcErr.*;
import static mscalc.ratpack.Num.*;
import static mscalc.ratpack.Rat.divrat;
import static mscalc.ratpack.Rat.mulrat;
import static mscalc.ratpack.RatPack.*;
import static mscalc.ratpack.Support.*;
import static mscalc.ratpack.Support.Global.*;

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
    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: StringToNumber
    //
    //  ARGUMENTS:
    //              wstring_view numberString
    //              int radix
    //              int32_t precision
    //
    //  RETURN: pnumber representation of string input.
    //          Or nullptr if no number scanned.
    //
    //  EXPLANATION: This is a state machine,
    //
    //    State      Description            Example, ^shows just read position.
    //                                                which caused the transition
    //
    //    START      Start state            ^1.0
    //    MANTS      Mantissa sign          -^1.0
    //    LZ         Leading Zero           0^1.0
    //    LZDP       Post LZ dec. pt.       000.^1
    //    LD         Leading digit          1^.0
    //    DZ         Post LZDP Zero         000.0^1
    //    DD         Post Decimal digit     .01^2
    //    DDP        Leading Digit dec. pt. 1.^2
    //    EXPB       Exponent Begins        1.0e^2
    //    EXPS       Exponent sign          1.0e+^5
    //    EXPD       Exponent digit         1.0e1^2 or  even 1.0e0^1
    //    EXPBZ      Exponent begin post 0  0.000e^+1
    //    EXPSZ      Exponent sign post 0   0.000e+^1
    //    EXPDZ      Exponent digit post 0  0.000e+1^2
    //    ERR        Error case             0.0.^
    //
    //    Terminal   Description
    //
    //    DP         '.'
    //    ZR         '0'
    //    NZ         '1'..'9' 'A'..'Z' 'a'..'z' '@' '_'
    //    SG         '+' '-'
    //    EX         'e' '^' e is used for radix 10, ^ for all other radixes.
    //
    //-----------------------------------------------------------------------------
    final char DP = 0;
    final char ZR = 1;
    final char NZ = 2;

    // Used to strip trailing zeros, and prevent combinatorial explosions
    // TODO: bool stripzeroesnum(_Inout_ PNUMBER pnum, int32_t starting);
    final char SG = 3;
    final char EX = 4;
    final char START = 0;
    final char MANTS = 1;
    final char LZ = 2;
    final char LZDP = 3;
    final char LD = 4;
    final char DZ = 5;
    final char DD = 6;
    final char DDP = 7;
    final char EXPB = 8;
    final char EXPS = 9;
    final char EXPD = 10;
    final char EXPBZ = 11;
    final char EXPSZ = 12;
    final char EXPDZ = 13;
    final char ERR = 14;
    // New state is machine[state][terminal]
    char[][] machine = initMachine();

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
        for (int i = 0; i < src.cdigit; i++) {
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

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: bool stripzeroesnum
    //
    //    ARGUMENTS:            a number representation
    //
    //    RETURN: true if stripping done, modifies number in place.
    //
    //    DESCRIPTION: Strips off trailing zeros.
    //
    //-----------------------------------------------------------------------------
    static boolean stripzeroesnum(Ptr<NUMBER> pnum, int starting)
    {
        boolean fstrip = false;
        // point pmant to the LeastCalculatedDigit
        ArrayPtrUInt pmant = pnum.deref().mant.pointer();
        int cdigits = pnum.deref().cdigit;
        // point pmant to the LSD
        if (cdigits > starting)
        {
            pmant.advance( cdigits - starting );
            cdigits = starting;
        }

        // Check we haven't gone too far, and we are still looking at zeros.
        while ((cdigits > 0) && !pmant.deref().toBool())
        {
            // move to next significant digit and keep track of digits we can
            // ignore later.
            pmant.advance();
            cdigits--;
            fstrip = true;
        }

        // If there are zeros to remove.
        if (fstrip)
        {
            // Remove them.
            // memmove(pnum->mant, pmant, (int)(cdigits * sizeof(MANTTYPE)));
            for (int k = 0; k < cdigits; k++) {
                pnum.deref().mant.set(k, pmant.at(k));
            }

            // And adjust exponent and digit count accordingly.
            pnum.deref().exp += (pnum.deref().cdigit - cdigits);
            pnum.deref().cdigit = cdigits;
        }

        return (fstrip);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: NumberToString
    //
    //    ARGUMENTS: number representation
    //          fmt, one of NumberFormat::Float, NumberFormat::Scientific or
    //          NumberFormat::Engineering
    //          integer radix and int32_t precision value
    //
    //    RETURN: String representation of number.
    //
    //    DESCRIPTION: Converts a number to its string
    //    representation.
    //
    //-----------------------------------------------------------------------------
    static String NumberToString(Ptr<NUMBER> pnum, NumberFormat format, uint radix, int precision)
    {
        stripzeroesnum(pnum, precision + 2);
        int length = pnum.deref().cdigit;
        int exponent = pnum.deref().exp + length; // Actual number of digits to the left of decimal

        NumberFormat oldFormat = format;
        if (exponent > precision && format == NumberFormat.Float)
        {
            // Force scientific mode to prevent user from assuming 33rd digit is exact.
            format = NumberFormat.Scientific;
        }

        // Make length small enough to fit in pret.
        if (length > precision)
        {
            length = precision;
        }

        // If there is a chance a round has to occur, round.
        // - if number is zero no rounding
        // - if number of digits is less than the maximum output no rounding
        Ptr<NUMBER> round = new Ptr<>();
        if (!zernum(pnum.deref()) && (pnum.deref().cdigit >= precision || (length - exponent > precision && exponent >= -MAX_ZEROS_AFTER_DECIMAL)))
        {
            // Otherwise round.
            round.set(i32tonum(radix.toInt(), radix));
            divnum(round, num_two, radix, precision);

            // Make round number exponent one below the LSD for the number.
            if (exponent > 0 || format == NumberFormat.Float)
            {
                round.deref().exp = pnum.deref().exp + pnum.deref().cdigit - round.deref().cdigit - precision;
            }
            else
            {
                round.deref().exp = pnum.deref().exp + pnum.deref().cdigit - round.deref().cdigit - precision - exponent;
                length = precision + exponent;
            }

            round.deref().sign = pnum.deref().sign;
        }

        if (format == NumberFormat.Float)
        {
            // Figure out if the exponent will fill more space than the non-exponent field.
            if ((length - exponent > precision) || (exponent > precision + 3))
            {
                if (exponent >= -MAX_ZEROS_AFTER_DECIMAL)
                {
                    round.deref().exp -= exponent;
                    length = precision + exponent;
                }
                else
                {
                    // Case where too many zeros are to the right or left of the
                    // decimal pt. And we are forced to switch to scientific form.
                    format = NumberFormat.Scientific;
                }
            }
            else if (length + abs(exponent) < precision && !round.isNull())
            {
                // Minimum loss of precision occurs with listing leading zeros
                // if we need to make room for zeros sacrifice some digits.
                round.deref().exp -= exponent;
            }
        }

        if (!round.isNull())
        {
            addnum(pnum, round.deref(), radix);
            int offset = (pnum.deref().cdigit + pnum.deref().exp) - (round.deref().cdigit + round.deref().exp);
            if (stripzeroesnum(pnum, offset))
            {
                // WARNING: nesting/recursion, too much has been changed, need to
                // re-figure format.
                return NumberToString(pnum, oldFormat, radix, precision);
            }
        }
        else
        {
            stripzeroesnum(pnum, precision);
        }

        // Set up all the post rounding stuff.
        boolean useSciForm = false;
        int eout = exponent - 1; // Displayed exponent.
        ArrayPtrUInt pmant = pnum.deref().mant.pointer();
        pmant.advance(pnum.deref().cdigit - 1);
        // Case where too many digits are to the left of the decimal or
        // NumberFormat::Scientific or NumberFormat::Engineering was specified.
        if ((format == NumberFormat.Scientific) || (format == NumberFormat.Engineering))
        {
            useSciForm = true;
            if (eout != 0)
            {
                if (format == NumberFormat.Engineering)
                {
                    exponent = (eout % 3);
                    eout -= exponent;
                    exponent++;

                    // Fix the case where 0.02e-3 should really be 2.e-6 etc.
                    if (exponent < 0)
                    {
                        exponent += 3;
                        eout -= 3;
                    }
                }
                else
                {
                    exponent = 1;
                }
            }
        }
        else
        {
            eout = 0;
        }

        // Begin building the result string
        StringBuilder result = new StringBuilder();

        // Make sure negative zeros aren't allowed.
        if ((pnum.deref().sign == -1) && (length > 0))
        {
            result.append('-');
        }

        if (exponent <= 0 && !useSciForm)
        {
            result.append('0');
            result.append(g_decimalSeparator.deref());
            // Used up a digit unaccounted for.
        }

        while (exponent < 0)
        {
            result.append('0');
            exponent++;
        }

        while (length > 0)
        {
            exponent--;
            result.append( DIGITS.charAt(pmant.derefMinusMinus().toInt()) );
            length--;

            // Be more regular in using a decimal point.
            if (exponent == 0)
            {
                result.append(g_decimalSeparator.deref());
            }
        }

        while (exponent > 0)
        {
            result.append('0');
            exponent--;
            // Be more regular in using a decimal point.
            if (exponent == 0)
            {
                result.append(g_decimalSeparator.deref());
            }
        }

        if (useSciForm)
        {
            result.append(radix.toInt() == 10 ? 'e' : '^');
            result.append(eout < 0 ? '-' : '+');
            eout = abs(eout);

            StringBuilder expString = new StringBuilder();
            do
            {
                // TODO: Check % unsigned here?
                expString.append( DIGITS.charAt(eout % radix.toInt()) );
                eout /= radix.toInt();
            } while (eout > 0);

            // result.insert(result.end(), expString.crbegin(), expString.crend());
            // TODO: May be buggy
            result.append(expString.reverse());
        }

        // Remove trailing decimal
        if (!result.isEmpty() && result.charAt(result.length()-1) == g_decimalSeparator.deref())
        {
            result.setLength(result.length()-1);
        }

        return result.toString();
    }

    private static char[][] initMachine() {
        char[][] machine = new char[ERR + 1][EX + 1];

        final char[][] machine_tmp = {
                //        DP,     ZR,      NZ,      SG,     EX
                // START
                {LZDP, LZ, LD, MANTS, ERR},
                // MANTS
                {LZDP, LZ, LD, ERR, ERR},
                // LZ
                {LZDP, LZ, LD, ERR, EXPBZ},
                // LZDP
                {ERR, DZ, DD, ERR, EXPB},
                // LD
                {DDP, LD, LD, ERR, EXPB},
                // DZ
                {ERR, DZ, DD, ERR, EXPBZ},
                // DD
                {ERR, DD, DD, ERR, EXPB},
                // DDP
                {ERR, DD, DD, ERR, EXPB},
                // EXPB
                {ERR, EXPD, EXPD, EXPS, ERR},
                // EXPS
                {ERR, EXPD, EXPD, ERR, ERR},
                // EXPD
                {ERR, EXPD, EXPD, ERR, ERR},
                // EXPBZ
                {ERR, EXPDZ, EXPDZ, EXPSZ, ERR},
                // EXPSZ
                {ERR, EXPDZ, EXPDZ, ERR, ERR},
                // EXPDZ
                {ERR, EXPDZ, EXPDZ, ERR, ERR},
                // ERR
                {ERR, ERR, ERR, ERR, ERR}
        };

        for (int i = 0; i < machine_tmp.length; i++) {
            for (int j = 0; j < machine_tmp[i].length; j++) {
                machine[i][j] = machine_tmp[i][j];
            }
        }

        return machine;
    }

    private static char NormalizeCharDigit(char c, uint radix)
    {
        // Allow upper and lower case letters as equivalent, base
        // is in the range where this is not ambiguous.
        if (radix.toInt()  >= DIGITS.indexOf('A') && radix.toInt() <= DIGITS.indexOf('Z'))
        {
            return Character.toUpperCase(c);
        }

        return c;
    }

    static NUMBER StringToNumber(String numberString, uint radix, int precision)
    {
        int expSign = 1;  // expSign is exponent sign ( +/- 1 )
        int expValue = 0; // expValue is exponent mantissa, should be unsigned

        Ptr<NUMBER> pnumret = new Ptr<>();
        pnumret.set(createnum(uint.of(numberString.length())));
        pnumret.deref().sign = 1;
        pnumret.deref().cdigit = 0;
        pnumret.deref().exp = 0;
        ArrayPtrUInt pmant = pnumret.deref().mant.pointer();
        pmant.advance(numberString.length() - 1);

        char state = START; // state is the state of the input state machine.
        for (char c : numberString.toCharArray())
        {
            // If the character is the decimal separator, use L'.' for the purposes of the state machine.
            char curChar = (c == g_decimalSeparator.deref() ? '.' : c);

            // Switch states based on the character we encountered
            switch (curChar)
            {
                case '-':
                case '+':
                    state = machine[state][SG];
                    break;
                case '.':
                    state = machine[state][DP];
                    break;
                case '0':
                    state = machine[state][ZR];
                    break;
                case '^':
                case 'e':
                    if (curChar == '^' || radix.toInt() == 10)
                {
                    state = machine[state][EX];
                    break;
                }
                // Drop through in the 'e'-as-a-digit case
                // [[fallthrough]];
                default:
                    state = machine[state][NZ];
                    break;
            }

            // Now update our result value based on the state we are in
            switch (state)
            {
                case MANTS:
                    pnumret.deref().sign = (curChar == '-') ? -1 : 1;
                    break;
                case EXPSZ:
                case EXPS:
                    expSign = (curChar == '-') ? -1 : 1;
                    break;
                case EXPDZ:
                case EXPD:
                {
                    curChar = NormalizeCharDigit(curChar, radix);

                    int pos = DIGITS.indexOf(curChar);
                    if (pos != -1)
                    {
                        expValue *= radix.toInt();
                        expValue += pos;
                    }
                    else
                    {
                        state = ERR;
                    }
                }
                break;
                case LD:
                    pnumret.deref().exp++;
                    // [[fallthrough]];
                case DD:
                {
                    curChar = NormalizeCharDigit(curChar, radix);

                    int pos = DIGITS.indexOf(curChar);
                    if (pos != -1 && pos < radix.toInt())
                    {
                        pmant.set(uint.of(pos));
                        pmant.advance(-1);
                        pnumret.deref().exp--;
                        pnumret.deref().cdigit++;
                    }
                    else
                    {
                        state = ERR;
                    }
                }
                break;
                case DZ:
                    pnumret.deref().exp--;
                    break;
                case LZ:
                case LZDP:
                case DDP:
                    break;
            }
        }

        if (state == DZ || state == EXPDZ)
        {
            pnumret.deref().cdigit = 1;
            pnumret.deref().exp = 0;
            pnumret.deref().sign = 1;
        }
        else
        {
            while (pnumret.deref().cdigit < numberString.length())
            {
                pnumret.deref().cdigit++;
                pnumret.deref().exp--;
            }

            pnumret.deref().exp += expSign * expValue;
        }

        // If we don't have a number, clear our result.
        if (pnumret.deref().cdigit == 0)
        {
            pnumret.set(null);
        }
        else
        {
            stripzeroesnum(pnumret, precision);
        }

        return pnumret.deref();
    }


    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: gcd
    //
    //  ARGUMENTS:
    //              PNUMBER representation of a number.
    //              PNUMBER representation of a number.
    //              int for Radix
    //
    //  RETURN: Greatest common divisor in internal BASEX PNUMBER form.
    //
    //  DESCRIPTION: gcd uses remainders to find the greatest common divisor.
    //
    //  ASSUMPTIONS: gcd assumes inputs are integers.
    //
    //  NOTE: Before it was found that the TRIM macro actually kept the
    //        size down cheaper than GCD, this routine was used extensively.
    //        now it is not used but might be later.
    //
    //-----------------------------------------------------------------------------
    static NUMBER gcd(NUMBER a, NUMBER b)
    {
        NUMBER r = null;
        Ptr<NUMBER> larger = new Ptr<>();
        NUMBER smaller = null;

        if (zernum(a))
        {
            return b;
        }
        else if (zernum(b))
        {
            return a;
        }

        if (lessnum(a, b))
        {
            larger.set( DUPNUM(b) );
            smaller = DUPNUM(a);
        }
        else
        {
            larger.set( DUPNUM(a) );
            smaller = DUPNUM(b);
        }

        while (!zernum(smaller))
        {
            remnum(larger, smaller, BASEX);
            // swap larger and smaller
            r = larger.deref();
            larger.set(smaller);
            smaller = r;
        }

        return larger.deref();
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: numpowi32
    //
    //    ARGUMENTS: root as number power as int32_t and radix of
    //               number along with the precision value in int32_t.
    //
    //    RETURN: None root is changed.
    //
    //    DESCRIPTION: changes numeric representation of root to
    //    root ** power. Assumes radix is the radix of root.
    //
    //-----------------------------------------------------------------------------
    static void numpowi32(Ptr<NUMBER> proot, int power, uint radix, int precision)
    {
        Ptr<NUMBER> lret = new Ptr<>(i32tonum(1, radix));

        while (power > 0)
        {
            if ((power & 1) != 0)
            {
                mulnum(lret, proot.deref(), radix);
            }
            mulnum(proot, proot.deref(), radix);
            proot.deref().TRIMNUM(precision);
            power >>= 1;
        }

        proot.set(lret.deref());
    }

    //----------------------------------------------------------------------------
    //
    //    FUNCTION: nRadixxtonum
    //
    //    ARGUMENTS: pointer to a number, base requested.
    //
    //    RETURN: number representation in radix requested.
    //
    //    DESCRIPTION: Does a base conversion on a number from
    //    internal to requested base. Assumes number being passed
    //    in is really in internal base form.
    //

    static NUMBER nRadixxtonum(NUMBER a, uint radix, int precision)
    {
        Ptr<NUMBER> sum = new Ptr<>(i32tonum(0, radix));
        Ptr<NUMBER> powofnRadix = new Ptr<>(i32tonum(BASEX.toInt(), radix));

        // A large penalty is paid for conversion of digits no one will see anyway.
        // limit the digits to the minimum of the existing precision or the
        // requested precision.
        // TODO: Original uses uint's here. Do we really need more than 2 billions digits?
        int cdigits = precision + 1;
        if (cdigits > a.cdigit)
        {
            cdigits = a.cdigit;
        }

        // scale by the internal base to the internal exponent offset of the LSD
        numpowi32(powofnRadix, a.exp + (a.cdigit - cdigits), radix, precision);

        // Loop over all the relative digits from MSD to LSD
        ArrayPtrUInt ptr = a.mant.pointer();
        ptr.advance(a.cdigit - 1);
        for (; cdigits > 0; ptr.advance(-1), cdigits--)
        {
            // Loop over all the bits from MSB to LSB
            for (uint bitmask = BASEX.divide(uint.of(2)); !bitmask.isZero(); bitmask = bitmask.divide(uint.of(2)))
            {
                addnum(sum, sum.deref(), radix);
                if (ptr.deref().bitAnd(bitmask).toBool())
                {
                    uint tmp = sum.deref().mant.at(0).bitOr(1);
                    sum.deref().mant.set(0, tmp);
                }
            }
        }

        // Scale answer by power of internal exponent.
        mulnum(sum, powofnRadix.deref(), radix);

        sum.deref().sign = a.sign;
        return sum.deref();
    }

    static NUMBER RatToNumber(RAT prat, uint radix, int precision)
    {
        RAT temprat = DUPRAT(prat);
        // Convert p and q of rational form from internal base to requested base.
        // Scale by largest power of BASEX possible.
        int scaleby = Math.min(temprat.pp.exp, temprat.pq.exp);
        scaleby = Math.max(scaleby, 0);

        temprat.pp.exp -= scaleby;
        temprat.pq.exp -= scaleby;

        Ptr<NUMBER> p = new Ptr<>( nRadixxtonum(temprat.pp, radix, precision) );
        NUMBER q = nRadixxtonum(temprat.pq, radix, precision);

        // finally take the time hit to actually divide.
        divnum(p, q, radix, precision);

        return p.deref();
    }

    // Converts a PRAT to a PNUMBER and back to a PRAT, flattening/simplifying the rational in the process
    static void flatrat(Ptr<RAT> prat, uint radix, int precision)
    {
        NUMBER pnum = RatToNumber(prat.deref(), radix, precision);
        prat.set( numtorat(pnum, radix) );
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: i32torat
    //
    //    ARGUMENTS: int32_t
    //
    //    RETURN: Rational representation of int32_t input.
    //
    //    DESCRIPTION: Converts int32_t input to rational (p over q)
    //    form, where q is 1 and p is the int32_t.
    //
    //-----------------------------------------------------------------------------
    static RAT i32torat(int ini32)
    {
        RAT pratret = createrat();
        pratret.pp = i32tonum(ini32, BASEX);
        pratret.pq = i32tonum(1, BASEX);
        return (pratret);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: ratpowi32
    //
    //    ARGUMENTS: root as rational, power as int32_t and precision as int32_t.
    //
    //    RETURN: None root is changed.
    //
    //    DESCRIPTION: changes rational representation of root to
    //    root ** power.
    //
    //-----------------------------------------------------------------------------
    static void ratpowi32(Ptr<RAT> proot, int power, int precision)
    {
        if (power < 0)
        {
            // Take the positive power and invert answer.
            NUMBER pnumtemp = null;
            ratpowi32(proot, -power, precision);
            pnumtemp = proot.deref().pp;
            proot.deref().pp = proot.deref().pq;
            proot.deref().pq = pnumtemp;
        }
        else
        {
            Ptr<RAT> lret = new Ptr<>();
            lret.set( i32torat(1) );

            while (power > 0)
            {
                if ((power & 1) != 0)
                {
                    Ptr<NUMBER> ppp = new Ptr<>(lret.deref().pp);
                    mulnumx(ppp, proot.deref().pp);
                    lret.deref().pp = ppp.deref();

                    Ptr<NUMBER> ppq = new Ptr<>(lret.deref().pq);
                    mulnumx(ppq, proot.deref().pq);
                    lret.deref().pq = ppq.deref();
                }

                mulrat(proot, proot.deref(), precision);
                trimit(lret, precision);
                trimit(proot, precision);
                power >>>= 1;
            }

            proot.set(lret.deref());
        }
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: Ui32torat
    //
    //    ARGUMENTS: ui32
    //
    //    RETURN: Rational representation of uint32_t input.
    //
    //    DESCRIPTION: Converts uint32_t input to rational (p over q)
    //    form, where q is 1 and p is the uint32_t. Being unsigned cant take negative
    //    numbers, but the full range of unsigned numbers
    //
    //-----------------------------------------------------------------------------
    static RAT Ui32torat(uint inui32)
    {
        RAT pratret = createrat();
        pratret.pp = Ui32tonum(inui32, BASEX);
        pratret.pq = i32tonum(1, BASEX);
        return (pratret);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: Ui32tonum
    //
    //    ARGUMENTS: uint32_t input and radix requested.
    //
    //    RETURN: number
    //
    //    DESCRIPTION: Returns a number representation in the
    //    base   requested of the uint32_t value passed in. Being unsigned number it has no
    //    negative number and takes the full range of unsigned number
    //
    //-----------------------------------------------------------------------------
    static NUMBER Ui32tonum(uint ini32, uint radix)
    {
        NUMBER pnumret = createnum(MAX_LONG_SIZE);

        ArrayPtrUInt pmant = pnumret.mant.pointer();
        pnumret.cdigit = 0;
        pnumret.exp = 0;
        pnumret.sign = 1;

        do
        {
            pmant.set(ini32.modulo(radix));
            pmant.advance();

            ini32 = ini32.divide(radix);
            pnumret.cdigit++;
        } while (!ini32.isZero());

        return (pnumret);
    }


    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: rattoi32
    //
    //    ARGUMENTS: rational number in internal base, integer radix and int32_t precision.
    //
    //    RETURN: int32_t
    //
    //    DESCRIPTION: returns the int32_t representation of the
    //    number input.  Assumes that the number is in the internal
    //    base.
    //
    //-----------------------------------------------------------------------------
    static int rattoi32(RAT prat, uint radix, int precision)
    {
        if (rat_gt(prat, rat_max_i32, precision) || rat_lt(prat, rat_min_i32, precision))
        {
            // Don't attempt rattoi32 of anything too big or small
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }

        Ptr<RAT> pint = new Ptr<>(DUPRAT(prat));
        intrat(pint, radix, precision);

        Ptr<NUMBER> ppp = new Ptr<>(pint.deref().pp);
        divnumx(ppp, pint.deref().pq, precision);
        pint.deref().pp = ppp.deref();

        pint.deref().pq = DUPNUM(num_one);

        int lret = numtoi32(pint.deref().pp, BASEX);
        return (lret);
    }

    //-----------------------------------------------------------------------------
    //
    //    FUNCTION: rattoUi32
    //
    //    ARGUMENTS: rational number in internal base, integer radix and int32_t precision.
    //
    //    RETURN: Ui32
    //
    //    DESCRIPTION: returns the Ui32 representation of the
    //    number input.  Assumes that the number is in the internal
    //    base.
    //
    //-----------------------------------------------------------------------------
    static uint rattoUi32(RAT prat, uint radix, int precision)
    {
        if (rat_gt(prat, rat_dword, precision) || rat_lt(prat, rat_zero, precision))
        {
            // Don't attempt rattoui32 of anything too big or small
            throw new ErrorCodeException(CALC_E_DOMAIN);
        }

        Ptr<RAT> pint = new Ptr<>(DUPRAT(prat));
        intrat(pint, radix, precision);

        Ptr<NUMBER> ppp = new Ptr<>(pint.deref().pp);
        divnumx(ppp, pint.deref().pq, precision);
        pint.deref().pp = ppp.deref();

        pint.deref().pq = DUPNUM(num_one);

        int lret = numtoi32(pint.deref().pp, BASEX); // This happens to work even if it is only signed
        return uint.of(lret);
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: RatToString
    //
    //  ARGUMENTS:
    //              PRAT *representation of a number.
    //              i32 representation of base  to  dump to screen.
    //              fmt, one of NumberFormat::Float, NumberFormat::Scientific, or NumberFormat::Engineering
    //              precision uint32_t
    //
    //  RETURN: string
    //
    //  DESCRIPTION: returns a string representation of rational number passed
    //  in, at least to the precision digits.
    //
    //  NOTE: It may be that doing a GCD() could shorten the rational form
    //       And it may eventually be worthwhile to keep the result.  That is
    //       why a pointer to the rational is passed in.
    //
    //-----------------------------------------------------------------------------
    static String RatToString(Ptr<RAT> prat, NumberFormat format, uint radix, int precision)
    {
        NUMBER p = RatToNumber(prat.deref(), radix, precision);
        String result = NumberToString(new Ptr<>(p), format, radix, precision);
        return result;
    }

    //-----------------------------------------------------------------------------
    //
    //  FUNCTION: StringToRat
    //
    //  ARGUMENTS:
    //              mantissaIsNegative true if mantissa is less than zero
    //              mantissa a string representation of a number
    //              exponentIsNegative  true if exponent is less than zero
    //              exponent a string representation of a number
    //              radix is the number base used in the source string
    //
    //  RETURN: PRAT representation of string input.
    //          Or nullptr if no number scanned.
    //
    //  EXPLANATION: This is for calc.
    //
    //
    //-----------------------------------------------------------------------------
    static RAT StringToRat(boolean mantissaIsNegative, String mantissa,
                           boolean exponentIsNegative, String exponent,
                           uint radix, int precision)
    {
        Ptr<RAT> resultRat = new Ptr<>(); // holds exponent in rational form.

        // Deal with mantissa
        if (mantissa.isEmpty())
        {
            // Preset value if no mantissa
            if (exponent.isEmpty())
            {
                // Exponent not specified, preset value to zero
                resultRat.set(DUPRAT(rat_zero));
            }
            else
            {
                // Exponent specified, preset value to one
                resultRat.set(DUPRAT(rat_one));
            }
        }
        else
        {
            // Mantissa specified, convert to number form.
            NUMBER pnummant = StringToNumber(mantissa, radix, precision);
            if (pnummant == null)
            {
                return null;
            }

            resultRat.set( numtorat(pnummant, radix) );
            // convert to rational form, and cleanup.
            destroynum(pnummant);
        }

        // Deal with exponent
        int expt = 0;
        if (!exponent.isEmpty())
        {
            // Exponent specified, convert to number form.
            // Don't use native stuff, as it is restricted in the bases it can
            // handle.
            NUMBER numExp = StringToNumber(exponent, radix, precision);
            if (numExp == null)
            {
                return null;
            }

            // Convert exponent number form to native integral form,  and cleanup.
            expt = numtoi32(numExp, radix);
            destroynum(numExp);
        }

        // Convert native integral exponent form to rational multiplier form.
        Ptr<NUMBER> pnumexp = new Ptr<>(i32tonum(radix.toInt(), BASEX));
        numpowi32x(pnumexp, abs(expt));

        RAT pratexp = createrat();
        pratexp.pp = DUPNUM(pnumexp.deref());
        pratexp.pq = i32tonum(1, BASEX);

        if (exponentIsNegative)
        {
            // multiplier is less than 1, this means divide.
            divrat(resultRat, pratexp, precision);
        }
        else if (expt > 0)
        {
            // multiplier is greater than 1, this means multiply.
            mulrat(resultRat, pratexp, precision);
        }
        // multiplier can be 1, in which case it'd be a waste of time to multiply.

        if (mantissaIsNegative)
        {
            // A negative number was used, adjust the sign.
            resultRat.deref().pp.sign *= -1;
        }

        return resultRat.deref();
    }
}
