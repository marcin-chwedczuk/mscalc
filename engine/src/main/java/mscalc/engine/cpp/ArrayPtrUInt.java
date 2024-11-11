package mscalc.engine.cpp;

public class ArrayPtrUInt {
    private final int[] arr;
    private int curr = 0;

    public ArrayPtrUInt(int[] arr) {
        this.arr = arr;
    }
    private ArrayPtrUInt(int[] arr, int curr) {
        this.arr = arr;
        this.curr = curr;
    }

    public boolean atBeginning() {
        return curr == 0;
    }

    public uint deref() { return uint.of(arr[curr]); }
    public uint derefPlusPlus() {
        try {
            return uint.of(arr[curr]); // TODO:
        } finally {
            advance();
        }
    }
    public uint derefMinusMinus() {
        try {
            return uint.of(arr[curr]); // TODO:
        } finally {
            advance(-1);
        }
    }

    public void advance() { curr++; }
    public void advance(int steps) {
        curr += steps;
    }

    public void set(uint value) {
        arr[curr] = value.raw();
    }

    public ArrayPtrUInt clone() {
        return new ArrayPtrUInt(arr, curr);
    }

    public uint at(int index) {
        return uint.of(arr[curr + index]);
    }

    public void setAt(int index, uint value) {
        arr[curr + index] = value.raw();
    }

    public uint minusMinusDeref() {
        advance(-1);
        return deref();
    }


}
