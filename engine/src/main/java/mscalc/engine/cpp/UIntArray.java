package mscalc.engine.cpp;

public class UIntArray {
    public static UIntArray ofValues(int... values) {
        return new UIntArray(values);
    }

    public static void copyTo(UIntArray dest, UIntArray src) {
        if (dest.length() < src.length())
            throw new IllegalArgumentException();

        for (int i = 0; i < src.length(); i++) {
            dest.set(i, src.at(i));
        }
    }

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

    public UIntArray copyFirst(int nItems) {
        int[] dest = new int[nItems];
        System.arraycopy(this.array, 0, dest, 0, nItems);
        return new UIntArray(dest);
    }

    public int length() {
        return this.array.length;
    }

    public UIntArrayPtr pointer() {
        return new UIntArrayPtr(array);
    }
}
