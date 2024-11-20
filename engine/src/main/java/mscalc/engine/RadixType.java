package mscalc.engine;

// This is expected to be in same order as IDM_HEX, IDM_DEC, IDM_OCT, IDM_BIN
public enum RadixType {
    Unknown(-1),
    Hex(0),
    Decimal(1),
    Octal(2),
    Binary(3);

    private final int value;

    RadixType(int value) {
        this.value = value;
    }

    public boolean hasDigit(int digit) {
        return switch (this) {
            case Unknown -> false;
            case Hex -> (0 <= digit && digit < 16);
            case Decimal -> (0 <= digit && digit < 10);
            case Octal -> (0 <= digit && digit < 8);
            case Binary -> (0 <= digit && digit < 2);
        };
    }

    public int toInt() {
        return this.value;
    }

    public static RadixType fromInt(int value) {
        return switch (value) {
            case 0 -> Hex;
            case 1 -> Decimal;
            case 2 -> Octal;
            case 3 -> Binary;
            default -> Unknown;
        };
    }
}
