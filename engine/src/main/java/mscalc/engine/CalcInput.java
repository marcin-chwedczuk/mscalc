package mscalc.engine;

import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack;

import static mscalc.engine.ratpack.Conv.StringToRat;

public class CalcInput {
    // Space to hold enough digits for a quadword binary number (64) plus digit separator strings for that number (20)
    private static final int MAX_STRLEN = 84;
    private static final int C_NUM_MAX_DIGITS = MAX_STRLEN;
    private static final int C_EXP_MAX_DIGITS = 4;

    private boolean hasExponent = false;
    private boolean hasDecimal = false;
    private int decimalPointIndex = 0;
    private char decimalSymbol = '.';
    private CalcNumberSign base = new CalcNumberSign();
    private CalcNumberSign exponent = new CalcNumberSign();

    public void clear() {
        base.clear();
        exponent.clear();
        hasExponent = hasDecimal = false;
        decimalPointIndex = 0;
    }

    public boolean tryToggleSign(boolean isIntegerMode, String maxNum)
    {
        // Zero is always positive
        if (base.isEmpty())
        {
            base.setNegative(false);
            exponent.setNegative(false);
        }
        else if (hasExponent)
        {
            exponent.setNegative(!exponent.isNegative());
        }
        else
        {
            // When in integer only mode, it isn't always allowed to toggle, as toggling can cause the num to be out of
            // bounds. For eg. in byte -128 is valid, but when it toggled it becomes 128, which is more than 127.
            if (isIntegerMode && base.isNegative())
            {
                // Decide if this additional digit will fit for the given bit width
                if (base.value().length() >= maxNum.length() && lastChar(base.value()) > lastChar(maxNum))
                {
                    // Last digit is more than the allowed positive number. Fail
                    return false;
                }
            }
            base.setNegative(!base.isNegative());
        }

        return true;
    }

    public boolean TryAddDigit(int value, uint radix, boolean isIntegerMode, String maxNumStr, int wordBitWidth, int maxDigits)
    {
        // Convert from an integer into a character
        // This includes both normal digits and alpha 'digits' for radixes > 10
        var chDigit = (char)((value < 10) ? ('0' + value) : ('A' + value - 10));

        CalcNumberSign pNumSec = null;
        long maxCount;
        if (hasExponent)
        {
            pNumSec = exponent;
            maxCount = C_EXP_MAX_DIGITS;
        }
        else
        {
            pNumSec = base;
            maxCount = maxDigits;
            // Don't include the decimal point in the count. In that way you can enter the maximum allowed precision.
            // Precision doesn't include decimal point.
            if (hasDecimal)
            {
                maxCount++;
            }
            // First leading 0 is not counted in input restriction as the output can be of that form
            // See NumberToString algorithm. REVIEW: We don't have such input restriction mimicking based on output of NumberToString for exponent
            // NumberToString can give 10 digit exponent, but we still restrict the exponent here to be only 4 digits.
            if (!pNumSec.isEmpty() && firstChar(pNumSec.value()) == '0')
            {
                maxCount++;
            }
        }

        // Ignore leading zeros
        if (pNumSec.isEmpty() && (value == 0))
        {
            return true;
        }

        if (pNumSec.value().length() < maxCount)
        {
            pNumSec.value().append(chDigit);
            return true;
        }

        // if we are in integer mode, within the base, and we're on the last digit then
        // there are special cases where we can actually add one more digit.
        if (isIntegerMode && pNumSec.value().length() == maxCount && !hasExponent)
        {
            boolean allowExtraDigit = false;

            if (radix.toInt() == 8)
            {
                switch (wordBitWidth % 3)
                {
                    case 1:
                        // in 16 or 64bit word size, if the first digit is a 1 we can enter 6 (16bit) or 22 (64bit) digits
                        allowExtraDigit = (firstChar(pNumSec.value()) == '1');
                        break;

                    case 2:
                        // in 8 or 32bit word size, if the first digit is a 3 or less we can enter 3 (8bit) or 11 (32bit) digits
                        allowExtraDigit = (firstChar(pNumSec.value()) <= '3');
                        break;
                }
            }
            else if (radix.toInt() == 10)
            {
                // If value length is at least the max, we know we can't add another digit.
                if (pNumSec.value().length() < maxNumStr.length())
                {
                    // Compare value to substring of maxNumStr of value.size() length.
                    // If cmpResult > 0:
                    // eg. max is "127", and the current number is "20". first digit itself says we are out.
                    // Additional digit is not possible

                    // If cmpResult < 0:
                    // Success case. eg. max is "127", and current number is say "11". The second digit '1' being <
                    // corresponding digit '2', means all digits are possible to append, like 119 will still be < 127

                    // If cmpResult == 0:
                    // Undecided still. The case when max is "127", and current number is "12". Look for the new number being 7 or less to allow
                    var cmpResult = pNumSec.value().toString().compareTo(maxNumStr.substring(0, pNumSec.value().length()));
                    if (cmpResult < 0)
                    {
                        allowExtraDigit = true;
                    }
                    else if (cmpResult == 0)
                    {
                        var lastChar = maxNumStr.charAt(pNumSec.value().length());
                        if (chDigit <= lastChar)
                        {
                            allowExtraDigit = true;
                        }
                        else if (pNumSec.isNegative() && chDigit <= lastChar + 1)
                        {
                            // Negative value case, eg. max is "127", and current number is "-12". Then 8 is also valid, as the range
                            // is always from -(max+1)...max in signed mode
                            allowExtraDigit = true;
                        }
                    }
                }
            }

            if (allowExtraDigit)
            {
                pNumSec.value().append(chDigit);
                return true;
            }
        }

        return false;
    }

    public boolean tryAddDecimalPt()
    {
        // Already have a decimal pt or we're in the exponent
        if (hasDecimal || hasExponent)
        {
            return false;
        }

        if (base.isEmpty())
        {
            base.value().append('0'); // Add a leading zero
        }

        decimalPointIndex = base.value().length();
        base.value().append(decimalSymbol);
        hasDecimal = true;

        return true;
    }

    public boolean hasDecimalPoint() {
        return hasDecimal;
    }

    public boolean tryBeginExponent()
    {
        // For compatibility, add a trailing dec point to base num if it doesn't have one
        tryAddDecimalPt();

        if (hasExponent) // Already entering exponent
        {
            return false;
        }

        hasExponent = true; // Entering exponent
        return true;
    }

    public void backspace()
    {
        if (hasExponent)
        {
            if (!exponent.isEmpty())
            {
                exponent.removeLastCharacter();

                if (exponent.isEmpty())
                {
                    exponent.clear();
                }
            }
            else
            {
                hasExponent = false;
            }
        }
        else
        {
            if (!base.isEmpty())
            {
                base.removeLastCharacter();
                if (base.value().toString().equals("0"))
                {
                    base.removeLastCharacter();
                }
            }

            if (base.value().length() <= decimalPointIndex)
            {
                // Backed up over decimal point
                hasDecimal = false;
                decimalPointIndex = 0;
            }

            if (base.isEmpty())
            {
                base.clear();
            }
        }
    }

    public void setDecimalSymbol(char decimalSymbol)
    {
        if (this.decimalSymbol != decimalSymbol)
        {
            this.decimalSymbol = decimalSymbol;

            if (hasDecimal)
            {
                // Change to new decimal pt
                base.value().setCharAt(decimalPointIndex, decimalSymbol);
            }
        }
    }

    public boolean isEmpty()
    {
        return base.isEmpty() && !hasExponent && exponent.isEmpty() && !hasDecimal;
    }

    public String toString(uint radix)
    {
        // In theory both the base and exponent could be C_NUM_MAX_DIGITS long.
        if ((base.value().length() > MAX_STRLEN) || (hasExponent && exponent.value().length() > MAX_STRLEN))
        {
            return "";
        }

        StringBuilder result = new StringBuilder();

        if (base.isNegative())
        {
            result.append('-');
        }

        if (base.isEmpty())
        {
            result.append('0');
        }
        else
        {
            result.append(base.value().toString());
        }

        if (hasExponent)
        {
            // Add a decimal point if it is not already there
            if (!hasDecimal)
            {
                result.append(decimalSymbol);
            }

            result.append((radix.toInt() == 10) ? 'e' : '^');
            result.append(exponent.isNegative() ? '-' : '+');

            if (exponent.isEmpty())
            {
                result.append('0');
            }
            else
            {
                result.append(exponent.value().toString());
            }
        }

        // Base and Exp can each be up to C_NUM_MAX_DIGITS in length, plus 4 characters for sign, dec, exp, and expSign.
        if (result.length() > C_NUM_MAX_DIGITS * 2 + 4)
        {
            return "";
        }

        return result.toString();
    }

    public Rational toRational(uint radix, int precision)
    {
        RatPack.RAT rat = StringToRat(base.isNegative(), base.value().toString(),
                exponent.isNegative(), exponent.value().toString(), radix, precision);
        if (rat == null)
        {
            return null;
        }

        return Rational.fromCRational(rat);
    }

    private static char firstChar(StringBuilder sb) {
        return sb.charAt(0);
    }

    private static char lastChar(String s) {
        return s.charAt(s.length()-1);
    }

    private static char lastChar(StringBuilder sb) {
        return sb.charAt(sb.length()-1);
    }
}
