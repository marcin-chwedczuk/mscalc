package mscalc.engine;

import mscalc.engine.cpp.UIntArray;
import mscalc.engine.cpp.UIntArrayPtr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack;

import static mscalc.engine.ratpack.Conv.createnum;

public class Number {
    private final int sign;
    private final int exp;
    private final UIntArray mantissa;

    public Number() {
        this(1, 0, UIntArray.ofValues(0));
    }

    public Number(int sign, int exp, UIntArray mantissa) {
        this.sign = sign;
        this.exp = exp;
        this.mantissa = mantissa.copy();
    }

    public static Number fromCNumber(RatPack.NUMBER cnumber) {
        return new Number(
                cnumber.sign,
                cnumber.exp,
                cnumber.mant
        );
    }

    public RatPack.NUMBER toCNumber() {
        RatPack.NUMBER cnumber = createnum(uint.of(this.mantissa.length()).add(uint.ONE));
        cnumber.sign = this.sign;
        cnumber.exp = this.exp;
        cnumber.cdigit = this.mantissa.length();
        UIntArray.copyTo(cnumber.mant, this.mantissa);
        return null;
    }

    public int sign() {
        return sign;
    }

    public int exp() {
        return exp;
    }

    public UIntArray mantiassa() {
        return mantissa.copy();
    }

    public boolean isZero() {
        for (int i = 0; i < mantissa.length(); i++) {
            if (!mantissa.at(i).isZero())
                return false;
        }

        return true;
    }
}
