package ru.geekbrains.monads;

public class StateTuple<S, V> {
    public final S state;
    public final V value;

    public StateTuple(S state, V value) {
        this.state = state;
        this.value = value;
    }

    public static <S,V> StateTuple<S,V> of(S state, V value) {
        return new StateTuple<>(state, value);
    }
}
