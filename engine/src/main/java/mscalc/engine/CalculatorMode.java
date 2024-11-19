package mscalc.engine;

public enum CalculatorMode {
    Standard(0),
    Scientific(1);

    private int value;

    CalculatorMode(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }
}
