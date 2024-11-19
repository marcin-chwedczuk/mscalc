package mscalc.engine;


public enum CalculatorPrecision {
    StandardModePrecision(16),
    ScientificModePrecision(32),
    ProgrammerModePrecision(64);

    private int value;

    CalculatorPrecision(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }
}
