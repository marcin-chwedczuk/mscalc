package mscalc.cpp;

public class ulong {
    public static final ulong ZERO = of(0);
    public static final ulong ONE = of(1);

    public static ulong of(long value) {
        return new ulong(value);
    }

    private final long value;

    public ulong(long value) {
        this.value = value;
    }

    public long raw() { return value; }

    public uint toUInt() {
        return uint.of((int)value);
    }

    public int compareTo(long other) {
        return Long.compareUnsigned(this.value, other);
    }

    public ulong multiply(uint other) {
        return multiply(other.toULong());
    }

    public ulong multiply(ulong other) {
        return ulong.of(this.value * other.value);
    }

    public ulong modulo(uint other) {
        return modulo(other.toULong());
    }

    public ulong modulo(ulong other) {
        return ulong.of(Long.remainderUnsigned(this.value, other.value));
    }

    public ulong divide(uint other) {
        return divide(other.toULong());
    }

    public ulong divide(ulong other) {
        return ulong.of(Long.divideUnsigned(this.value, other.value));
    }

    public boolean notEq(long other) {
        return Long.compareUnsigned(this.value, other) != 0;
    }

    public ulong plus(uint other) {
        return ulong.of(this.value + other.toULong().value);
    }

    public ulong plus(ulong other) {
        return ulong.of(this.value + other.value);
    }

    public ulong shiftRight(int n) {
        return ulong.of(value >>> n);
    }

    public boolean toBool() {
        return value != 0;
    }
}

