package ru.geekbrains.monads;

import java.util.function.Function;

/**
 * Монада State
 *
 * @param <A>
 * @param <S>
 */
public class State<S, A> {

    private Function<S, StateTuple<S, A>> runner;

    private State(Function<S, StateTuple<S, A>> runner) {
        this.runner = runner;
    }

    public static <S, A> State<S, A> of(Function<S, StateTuple<S, A>> f) {
        return new State<>(f);
    }

    public static <S, A> State<S, A> pure(A a) {
        return new State<>(s -> new StateTuple<>(s, a));
    }

    public static <S> State<S, S> get() {
        return new State<>(s -> new StateTuple<>(s, s));
    }

    public static <S, A> State<S, A> gets(Function<S, A> f) {
        return new State<>(s -> new StateTuple<>(s, f.apply(s)));
    }

    public static <S, A> State<S, A> put(S sNew) {
        return new State<>(s -> new StateTuple<>(sNew, null));
    }

    public static <S, A> State<S, A> modify(Function<S, S> f) {
        return new State<>(s -> new StateTuple<>(f.apply(s), null));
    }

    public StateTuple<S, A> apply(S s) {
        return runner.apply(s);
    }

    public <B> State<S, B> map(Function<? super A, ? extends B> f) {
        return new State<>(s -> {
            StateTuple<S, A> value = runner.apply(s);
            return new StateTuple<>(value.state, f.apply(value.value));
        });
    }

    public <B> State<S, B> flatMap(Function<A, State<S, B>> f) {
        return new State<>(s -> {
            StateTuple<S, A> value = runner.apply(s);
            return f.apply(value.value).apply(value.state);
        });
    }

}
