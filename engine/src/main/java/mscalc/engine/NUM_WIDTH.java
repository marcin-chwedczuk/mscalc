package mscalc.engine;

// This is expected to be in same order as IDM_QWORD, IDM_DWORD etc.
enum NUM_WIDTH
{
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
}
