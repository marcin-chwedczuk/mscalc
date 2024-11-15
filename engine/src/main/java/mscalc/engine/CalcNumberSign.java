package mscalc.engine;

public class CalcNumberSign {
    private boolean isNegative = false;
    private final StringBuilder value = new StringBuilder();

    public StringBuilder value() {
        return value;
    }

    public void clear() {
        value.setLength(0);
        isNegative = false;
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public boolean isNegative() {
        return isNegative;
    }

    public void setNegative(boolean isNegative) {
        this.isNegative = isNegative;
    }

    public boolean removeLastCharacter() {
        if (isEmpty()) return false;
        value.deleteCharAt(value.length()-1);
        return true;
    }
}
