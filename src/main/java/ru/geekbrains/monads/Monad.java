package ru.geekbrains.monads;

import java.util.Objects;
import java.util.function.Function;

public class Monad<A> {

    private final A value;

    private Monad(A value) {
        this.value = value;
    }

    public static <A> Monad<A> apply(A value) {
        return new Monad<>(value);
    }

    <B> Monad<B> flatMap(Function<A, Monad<B>> function) {
        return function.apply(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monad<?> monad = (Monad<?>) o;
        return Objects.equals(value, monad.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
