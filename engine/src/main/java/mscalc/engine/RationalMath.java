package mscalc.engine;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.AngleType;

import static mscalc.engine.Rational.RATIONAL_BASE;
import static mscalc.engine.Rational.RATIONAL_PRECISION;
import static mscalc.engine.ratpack.Exp.*;
import static mscalc.engine.ratpack.Fact.factrat;
import static mscalc.engine.ratpack.ITrans.*;
import static mscalc.engine.ratpack.ITransH.*;
import static mscalc.engine.ratpack.Logic.modrat;
import static mscalc.engine.ratpack.Rat.fracrat;
import static mscalc.engine.ratpack.Support.intrat;
import static mscalc.engine.ratpack.Trans.*;
import static mscalc.engine.ratpack.TransH.*;

public class RationalMath {
    private RationalMath() { }

    public static Rational frac(Rational r) {
        var rat = toRatPtr(r);
        fracrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational integer(Rational r) {
        var rat = toRatPtr(r);
        intrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational pow(Rational x, Rational y) {
        var xrat = toRatPtr(x);
        var yrat = y.toCRational();
        powrat(xrat, yrat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(xrat.deref());
    }

    public static Rational root(Rational base, Rational root) {
        return pow(base, invert(root));
    }

    public static Rational factorial(Rational r) {
        var rat = toRatPtr(r);
        factrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational exp(Rational r) {
        var rat = toRatPtr(r);
        exprat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational ln(Rational r) {
        var rat = toRatPtr(r);
        lograt(rat, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational log10(Rational r) {
        var rat = toRatPtr(r);
        log10rat(rat, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational invert(Rational r) {
        return Rational.of(1).dividedBy(r);
    }

    public static Rational abs(Rational r) {
        return new Rational(r.p().abs(), r.q().abs());
    }

    public static Rational sin(Rational r, AngleType angleType) {
        var rat = toRatPtr(r);
        sinanglerat(rat, angleType, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational cos(Rational r, AngleType angleType) {
        var rat = toRatPtr(r);
        cosanglerat(rat, angleType, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational tan(Rational r, AngleType angleType) {
        var rat = toRatPtr(r);
        tananglerat(rat, angleType, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational asin(Rational r, AngleType angleType) {
        var rat = toRatPtr(r);
        asinanglerat(rat, angleType, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational acos(Rational r, AngleType angleType) {
        var rat = toRatPtr(r);
        acosanglerat(rat, angleType, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational atan(Rational r, AngleType angleType) {
        var rat = toRatPtr(r);
        atananglerat(rat, angleType, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational sinh(Rational r) {
        var rat = toRatPtr(r);
        sinhrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational cosh(Rational r) {
        var rat = toRatPtr(r);
        coshrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational tanh(Rational r) {
        var rat = toRatPtr(r);
        tanhrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational asinh(Rational r) {
        var rat = toRatPtr(r);
        asinhrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational acosh(Rational r) {
        var rat = toRatPtr(r);
        acoshrat(rat, RATIONAL_BASE, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    public static Rational atanh(Rational r) {
        var rat = toRatPtr(r);
        atanhrat(rat, RATIONAL_PRECISION);
        return Rational.fromCRational(rat.deref());
    }

    /// <summary>
    /// Calculate the modulus after division, the sign of the result will match the sign of b.
    /// </summary>
    /// <remarks>
    /// When one of the operand is negative
    /// the result will differ from the C/C++ operator '%'
    /// use <see cref="Rational::operator%"/> instead to calculate the remainder after division.
    /// </remarks>
    public static Rational mod(Rational x, Rational y) {
        var xRat = toRatPtr(x);
        var yRat = y.toCRational();

        modrat(xRat, yRat);

        return Rational.fromCRational(xRat.deref());
    }

    private static Ptr<RatPack.RAT> toRatPtr(Rational r) {
        return new Ptr<>(r.toCRational());
    }
}
