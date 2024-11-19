package mscalc.engine;

public enum MemoryCommand {
    MemorizeNumber(330),
    MemorizedNumberLoad(331),
    MemorizedNumberAdd(332),
    MemorizedNumberSubtract(333),
    MemorizedNumberClearAll(334),
    MemorizedNumberClear(335);

    private int value;

    MemoryCommand(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }
}


