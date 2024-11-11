package mscalc.engine.cpp;

public class Ptr<T> {
    private T value;

    public Ptr() {
        this(null);
    }

    public Ptr(T value) {
        this.value = value;
    }

    public T deref() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }

    @Override
    public String toString() {
        return "Ptr{" +
                "value=" + value +
                '}';
    }
}
