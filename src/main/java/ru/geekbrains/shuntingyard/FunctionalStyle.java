package ru.geekbrains.shuntingyard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Deque;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionalStyle {

    private static final Scanner scanner = new Scanner(System.in);

    private static final List<String> op = Arrays.asList("+", "-", "*", "/", "^");

    public static void main(String[] args) {
        IO.apply(() -> "Введите выражение:")
                .mapToVoid(System.out::println)
                .map(v -> scanner.nextLine())
                .map(FunctionalStyle::parse)
                .map(FunctionalStyle::toRPN)
                .map(FunctionalStyle::rpnToString)
                .mapToVoid(System.out::println)
                .unsafeRun();
    }

    static Deque<String> toRPN(final List<String> tokens) {

        State<Tuple2<Deque<String>, Deque<String>>, String> state = State.of(s -> new StateTuple<>(s, ""));

        Iterator<String> iterator = tokens.iterator();

        StateTuple<Tuple2<Deque<String>, Deque<String>>, String> stateTuple =
                traverse(state, iterator)
                        .flatMap(s -> transferRest())
                        .apply(Tuple2.of(newDeque(), newDeque()));

        return stateTuple.state.output;

    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> traverse(State<Tuple2<Deque<String>, Deque<String>>, String> state, Iterator<String> iterator) {
        if (!iterator.hasNext()) return state;
        String token = iterator.next();
        return traverse(state.flatMap(s -> processToken(token)), iterator);
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> transferRest() {
        return State.modify(t -> {
            while (!t.stack.isEmpty()) {
                t.output.push(t.stack.pop());
            }
            return t;
        });
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> processToken(String token) {
        return State.modify(t -> {
            if ("(".equals(token)) {
                t.stack.push(token);
            } else if (")".equals(token)) {
                while (!"(".equals(t.stack.peek())) {
                    t.output.push(t.stack.pop());
                }
                t.stack.pop();
            } else if (isOp(token)) {
                Predicate<String> predicate = associativity(token) == Associativity.LEFT ?
                        op -> precedence(op) >= precedence(token) : op -> precedence(op) > precedence(token);
                while (!t.stack.isEmpty() && predicate.test(t.stack.peek())) {
                    t.output.push(t.stack.pop());
                }
                t.stack.push(token);
            } else {
                t.output.push(token);
            }
            return t;
        });
    }

    static boolean isOp(String symbol) {
        return op.contains(symbol);
    }

    static List<String> parse(String expression) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("([0-9]*)([+\\-*/^]*)([()]*)");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group.isEmpty()) continue;
                tokens.add(group.trim());
            }
        }
        return tokens;
    }

    public interface Effect<T> {
        T run();
    }

    /**
     * Монада IO
     *
     * @param <A>
     */
    public static class IO<A> {
        private final Effect<A> effect;

        private IO(Effect<A> effect) {
            this.effect = effect;
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

        public IO<Void> mapToVoid(Consumer<A> function) {
            return this.flatMap(result -> IO.apply(() -> {
                function.accept(result);
                return null;
            }));
        }

        public IO<A> peek(Consumer<A> function) {
            return this.flatMap(result -> IO.apply(() -> {
                function.accept(result);
                return result;
            }));
        }

    }

    /**
     * Монада State
     *
     * @param <A>
     * @param <S>
     */
    public static class State<S, A> {

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

    public static class StateTuple<S, V> {
        final S state;
        final V value;

        public StateTuple(S state, V value) {
            this.state = state;
            this.value = value;
        }
    }

    public static class Tuple2<V1, V2> {
        final V1 stack;
        final V2 output;

        private Tuple2(V1 value1, V2 value2) {
            this.stack = value1;
            this.output = value2;
        }

        public static <V1, V2>  Tuple2<V1, V2> of(V1 value1, V2 value2) {
            return new Tuple2<>(value1, value2);
        }

    }

    static String rpnToString(Deque<String> strings) {
        return strings.stream().reduce("", (s1, s2) -> s2 + " " + s1);
    }

    public static int precedence(final String op) {
        switch (op) {
            case "+":
            case "-":
                return 2;
            case "*":
            case "/":
                return 3;
            case "^":
                return 4;
        }
        return 0;
    }

    public enum Associativity {
        LEFT, RIGHT
    }

    public static Associativity associativity(final String op) {
        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
                return Associativity.LEFT;
            case "^":
                return Associativity.RIGHT;
        }
        return Associativity.LEFT;
    }

    public static Deque<String> newDeque() {
        return new ArrayDeque<>();
    }

}

