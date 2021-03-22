package ru.geekbrains.monads;


public final class Nothing {

    private static final Nothing instance = new Nothing();

    public static Nothing instance() {
        return instance;
    }

}
