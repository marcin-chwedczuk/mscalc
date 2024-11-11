package mscalc.engine.cpp;

public class ArrayPtrInt {
    private final int[] arr;
    private int curr = 0;

    public ArrayPtrInt(int[] arr) {
        this.arr = arr;
    }

    public int deref() { return arr[curr]; }

    public void advance() { curr++; }
    public void advance(int steps) {
        curr += steps;
    }

    public void set(int value) {
        arr[curr] = value;
    }
}
