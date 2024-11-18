package mscalc.engine;

// This is expected to be in same order as IDM_HEX, IDM_DEC, IDM_OCT, IDM_BIN
public enum RadixType {
    Unknown(-1), Hex(0), Decimal(1), Octal(2), Binary(3);

    private final int value;

    private RadixType(int value) {
        this.value = value;
    }

    // TODO: Fix it later
    public int cppValue() {
        return this.value;
    }

    public static RadixType fromCppValue(int value) {
        return switch (value) {
            case 0 -> Hex;
            case 1 -> Decimal;
            case 2 -> Octal;
            case 3 -> Binary;
            default -> Unknown;
        };
    }
}
