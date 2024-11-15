package mscalc.engine;

import mscalc.engine.cpp.*;
import mscalc.engine.ratpack.Num;
import mscalc.engine.ratpack.RatPack;

import static mscalc.engine.ratpack.Conv.*;
import static mscalc.engine.ratpack.Logic.*;
import static mscalc.engine.ratpack.Rat.*;
import static mscalc.engine.ratpack.Support.*;

public class Rational {
    // Default Base/Radix to use for Rational calculations
    // RatPack calculations currently support up to Base64.
    public static final uint RATIONAL_BASE = uint.of(10);

    // Default Precision to use for Rational calculations
    public static final int RATIONAL_PRECISION = 128;

    private final Number p, q;

    public Rational() {
        this.p = new Number();
        this.q = new Number(1, 0, UIntArray.ofValues(1));
    }

    public Rational(Number n) {
        int qExp = 0;
        if (n.exp() < 0)
        {
            qExp -= n.exp();
        }

        this.p = new Number(n.sign(), 0, n.mantissa());
        this.q = new Number(1, qExp, UIntArray.ofValues(1));
    }

    public Rational(Number p, Number q) {
        this.p = p;
        this.q = q;
    }

    public static Rational of(int i) {
        return new Rational(i);
    }

    public Rational(int i) {
        RatPack.RAT rat = i32torat(i);
        this.p = Number.fromCNumber(rat.pp);
        this.q = Number.fromCNumber(rat.pq);
    }

    public Rational(uint i) {
        RatPack.RAT pr = Ui32torat(i);

        this.p = Number.fromCNumber(pr.pp);
        this.q = Number.fromCNumber(pr.pq);
    }

    public Rational(ulong ul) {
        int hi = (int)(ul.raw() >>> 32);
        int lo = (int)(ul.raw());

        Rational temp = (Rational.of(hi).shiftedLeft(Rational.of(32))).bitOr(Rational.of(lo));

        this.p = temp.p();
        this.q = temp.q();
    }

    public static Rational fromCRational(RatPack.RAT cRational) {
        return new Rational(
                Number.fromCNumber(cRational.pp),
                Number.fromCNumber(cRational.pq));
    }

    public RatPack.RAT toCRational() {
        RatPack.RAT ret = createrat();
        ret.pp = this.p.toCNumber();
        ret.pq = this.q.toCNumber();
        return ret;
    }

    public Number p() {
        return this.p;
    }

    public Number q() {
        return this.q;
    }

    public Rational negated() {
        return new Rational(p.negated(), q);
    }

    public Rational plus(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        addrat(lhsRat, rhsRat, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational minus(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        subrat(lhsRat, rhsRat, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational times(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        mulrat(lhsRat, rhsRat, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational dividedBy(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        divrat(lhsRat, rhsRat, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational modulo(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        remrat(lhsRat, rhsRat);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational shiftedLeft(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        lshrat(lhsRat, rhsRat, RATIONAL_BASE, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational shiftedRight(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        rshrat(lhsRat, rhsRat, RATIONAL_BASE, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational bitAnd(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        andrat(lhsRat, rhsRat, RATIONAL_BASE, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational bitOr(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        orrat(lhsRat, rhsRat, RATIONAL_BASE, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public Rational bitXor(Rational other) {
        Ptr<RatPack.RAT> lhsRat = new Ptr<>(this.toCRational());
        RatPack.RAT rhsRat = other.toCRational();

        xorrat(lhsRat, rhsRat, RATIONAL_BASE, RATIONAL_PRECISION);

        return Rational.fromCRational(lhsRat.deref());
    }

    public boolean isEqual(Rational other) {
        RatPack.RAT lhs = this.toCRational();
        RatPack.RAT rhs = other.toCRational();

        return rat_equ(lhs, rhs, RATIONAL_PRECISION);
    }

    public boolean isNotEqual(Rational other) {
        return !isEqual(other);
    }

    public boolean isLessThan(Rational other) {
        RatPack.RAT lhs = this.toCRational();
        RatPack.RAT rhs = other.toCRational();

        return rat_lt(lhs, rhs, RATIONAL_PRECISION);
    }

    public boolean isGreaterThan(Rational other) {
        RatPack.RAT lhs = this.toCRational();
        RatPack.RAT rhs = other.toCRational();

        return rat_gt(lhs, rhs, RATIONAL_PRECISION);
    }

    public boolean isLessOrEqual(Rational other) {
        RatPack.RAT lhs = this.toCRational();
        RatPack.RAT rhs = other.toCRational();

        return rat_le(lhs, rhs, RATIONAL_PRECISION);
    }

    public boolean isGreaterOrEqual(Rational other) {
        RatPack.RAT lhs = this.toCRational();
        RatPack.RAT rhs = other.toCRational();

        return rat_ge(lhs, rhs, RATIONAL_PRECISION);
    }

    public String toString(uint radix, RatPack.NumberFormat fmt, int precision) {
        return RatToString(new Ptr<>(this.toCRational()), fmt, radix, precision);
    }

    public ulong toULong() {
        return rattoUi64(this.toCRational(), RATIONAL_BASE, RATIONAL_PRECISION);
    }
}
