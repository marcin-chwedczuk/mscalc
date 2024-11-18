package mscalc.engine;

// This is expected to be in same order as IDM_QWORD, IDM_DWORD etc.
enum NUM_WIDTH
{
    UNDEFINED(-1),
    QWORD_WIDTH(0), // Number width of 64 bits mode (default)
    DWORD_WIDTH(1), // Number width of 32 bits mode
    WORD_WIDTH(2),  // Number width of 16 bits mode
    BYTE_WIDTH(3);  // Number width of 16 bits mode

    private final int value;

    private NUM_WIDTH(int v) {
        this.value = v;
    }

    public int toInt() {
        return value;
    }

    public static NUM_WIDTH fromInt(int n) {
        return switch (n) {
            case 0 -> QWORD_WIDTH;
            case 1 -> DWORD_WIDTH;
            case 2 -> WORD_WIDTH;
            case 3 -> BYTE_WIDTH;
            default -> UNDEFINED;
        };
    }
}
