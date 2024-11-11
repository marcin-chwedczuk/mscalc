package mscalc.engine.cpp;

// In C++ when you mix singed & unsigned numbers in an expression,
// unsigned always *wins* and the result is unsigned.
public class uint {
    public static final uint ZERO = of(0);
    public static final uint ONE = of(1);
    private final int value;

    public uint(int value) {
        this.value = value;
    }

    public static uint of(int value) {
        return new uint(value);
    }

    public int raw() { return value; }

    public uint add(uint other) {
        return new uint(this.value + other.value);
    }

    public uint subtract(uint other) {
        return new uint(this.value - other.value);
    }

    public int compareTo(uint other) {
        return Integer.compareUnsigned(this.value, other.value);
    }

    public int toInt() {
        return value;
    }

    public ulong toULong() {
        return ulong.of(Integer.toUnsignedLong(value));
    }

    public uint modulo(uint other) {
        return uint.of(Integer.remainderUnsigned(this.value, other.value));
    }

    public uint divide(uint other) {
        return uint.of(Integer.divideUnsigned(this.value, other.value));
    }

    public boolean notEq(int other) {
        return Integer.compareUnsigned(this.value, other) != 0;
    }

    public uint bitNeg() {
        return uint.of(~value);
    }

    public uint bitAnd(uint other) {
        return uint.of(this.value & other.value);
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean toBool() {
        return value != 0;
    }

    public ulong multiply(uint other) {
        return ulong.of(this.value * other.toULong().raw());
    }

    public uint bitOr(uint other) {
        return uint.of(this.value | other.raw());
    }

    public uint bitXor(uint other) {
        return uint.of(this.value ^ other.raw());
    }
}
