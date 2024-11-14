package mscalc.engine.cpp;

public class UIntArray {
    private int[] array;

    public UIntArray(int size) {
        this.array = new int[size];
    }

    public UIntArray(int[] array) {
        this.array = array.clone();
    }

    public uint at(int idx) {
        return uint.of(array[idx]);
    }

    public void set(int idx, uint value) {
        array[idx] = value.raw();
    }

    public UIntArray copy() {
        return new UIntArray(this.array.clone());
    }

    public int length() {
        return this.array.length;
    }

    public UIntArrayPtr pointer() {
        return new UIntArrayPtr(array);
    }
}
