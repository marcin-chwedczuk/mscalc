package mscalc.engine;

// This is expected to be in same order as IDM_HEX, IDM_DEC, IDM_OCT, IDM_BIN
public enum RadixType {
    Hex(0), Decimal(1), Octal(2), Binary(3);

    private final int value;

    private RadixType(int value) {
        this.value = value;
    }

    // TODO: Fix it later
    public int cppValue() {
        return this.value;
    }
}
