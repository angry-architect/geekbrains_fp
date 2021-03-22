package ru.geekbrains.shuntingyard;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import ru.geekbrains.monads.IO;
import ru.geekbrains.monads.Nothing;
import ru.geekbrains.monads.State;
import ru.geekbrains.monads.StateTuple;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;

public class ShuntingYardFP {

    private static final Scanner scanner = new Scanner(System.in);

    private static final List<String> op = Arrays.asList("+", "-", "*", "/", "^");

    private static final Function<String, Nothing> println = str -> {
        System.out.println(str);
        return Nothing.instance();
    };

    public static void main(String[] args) {
         IO.apply(() -> "Введите выражение:")
                .map(println)
                .map(v -> scanner.nextLine())
                .map(ShuntingYardFP::parse)
                .map(ShuntingYardFP::toRPN)
                .map(ShuntingYardFP::rpnToString)
                .map(println)
                .run().peekLeft(System.out::println);
    }

    static Deque<String> toRPN(final List<String> tokens) {

        State<Tuple2<Deque<String>, Deque<String>>, String> state = State.of(s -> StateTuple.of(s, ""));

        Iterator<String> iterator = tokens.iterator();

        StateTuple<Tuple2<Deque<String>, Deque<String>>, String> stateTuple =
                traverse(state, iterator)
                        .flatMap(s -> transferRest())
                        .apply(Tuple.of(newDeque(), newDeque()));

        return stateTuple.state._2;
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> traverse(State<Tuple2<Deque<String>, Deque<String>>, String> state, Iterator<String> iterator) {
        if (!iterator.hasNext()) return state;
        String token = iterator.next();
        return traverse(state.flatMap(s -> processToken(token)), iterator);
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> transferRest() {
        return State.modify(state -> {
            while (!state._1.isEmpty()) {
                state._2.push(state._1.pop());
            }
            return state;
        });
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> processToken(String token) {
        return Match(token).of(
                Case($(is("(")), processLeftBracket(token)),
                Case($(is(")")), processRightBracket(token)),
                Case($(ShuntingYardFP::isOp), processOperator(token)),
                Case($(), processDigit(token))
        );
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> processLeftBracket(String token) {
        return State.modify(state -> {
            state._1.push(token);
            return state;
        });
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> processRightBracket(String token) {
        return State.modify(state -> {
            while (!"(".equals(state._1.peek())) {
                state._2.push(state._1.pop());
            }
            state._1.pop();
            return state;
        });
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> processOperator(String token) {
        return State.modify(state -> {
            Predicate<String> predicate = associativity(token) == ShuntingYardFP.Associativity.LEFT ?
                    op -> precedence(op) >= precedence(token) : op -> precedence(op) > precedence(token);
            while (!state._1.isEmpty() && predicate.test(state._1.peek())) {
                state._2.push(state._1.pop());
            }
            state._1.push(token);
            return state;
        });
    }

    static State<Tuple2<Deque<String>, Deque<String>>, String> processDigit(String token) {
        return State.modify(state -> {
            state._2.push(token);
            return state;
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
                return ShuntingYardFP.Associativity.LEFT;
            case "^":
                return ShuntingYardFP.Associativity.RIGHT;
        }
        return ShuntingYardFP.Associativity.LEFT;
    }

    public static Deque<String> newDeque() {
        return new ArrayDeque<>();
    }

}
