package ru.geekbrains.monads;


import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MonadTest {

    @Test
    public void testLeftIdentity() {
        Function<Integer, Monad<Integer>> f = i -> Monad.apply(i);
        assertEquals(Monad.apply(10).flatMap(f), f.apply(10));
    }

    @Test
    public void testRightIdentity() {
        Monad<Integer> M = Monad.apply(8);
        assertEquals(M.flatMap(Monad::apply), M);
    }

    @Test
    public void testAssociativity() {
        Monad<Integer> M = Monad.apply(8);
        Function<Integer, Monad<Integer>> f = Monad::apply;
        Function<Integer, Monad<String>> g = i -> Monad.apply(i.toString());
        assertEquals(M.flatMap(f).flatMap(g),M.flatMap(x -> f.apply(x).flatMap(g)));
    }

}