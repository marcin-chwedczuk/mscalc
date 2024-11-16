package mscalc.engine;

public class Pair<T,S> {
    public static <T,S> Pair<T,S> entry(T key, S value) {
        return new Pair<>(key, value);
    }

    private T key;
    private S value;

    public Pair(T key, S value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() { return key; }
    public S getValue() { return value; }

    public void setKey(T key) { this.key = key; }
    public void setValue(S value) { this.value = value; }
}
