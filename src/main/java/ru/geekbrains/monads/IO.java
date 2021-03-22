package ru.geekbrains.monads;

import io.vavr.control.Either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Монада IO
 *
 * @param <A>
 */
public class IO<A> {

    private final Effect<A> effect;

    private IO(Effect<A> effect) {
        this.effect = effect;
    }

    public Either<Exception, A> run() {
        try {
            return Either.right(effect.run());
        } catch (Exception ex) {
            return Either.left(ex);
        }
    }

    public A unsafeRun() {
        return effect.run();
    }

    public <B> IO<B> flatMap(Function<A, IO<B>> function) {
        return IO.apply(() -> function.apply(effect.run()).unsafeRun());
    }

    public static <T> IO<T> apply(Effect<T> effect) {
        return new IO<>(effect);
    }

    public <B> IO<B> map(Function<A, B> function) {
        return this.flatMap(result -> IO.apply(() -> function.apply(result)));
    }

    public IO<A> peek(Consumer<A> function) {
        return this.flatMap(result -> IO.apply(() -> {
            function.accept(result);
            return result;
        }));
    }

}
