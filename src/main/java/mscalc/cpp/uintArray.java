package mscalc.cpp;

public class uintArray {
    private int[] array;

    public uintArray(int size) {
        this.array = new int[size];
    }

    private uintArray(int[] array) {
        this.array = array;
    }

    public uint at(int idx) {
        return uint.of(array[idx]);
    }

    public void set(int idx, uint value) {
        array[idx] = value.raw();
    }

    public uintArray clone() {
        return new uintArray(this.array.clone());
    }

    public int length() {
        return this.array.length;
    }

    public ArrayPtrUInt pointer() {
        return new ArrayPtrUInt(array);
    }
}
