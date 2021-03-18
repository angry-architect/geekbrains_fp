package ru.geekbrains.monads;

public interface Effect<T> {
    T run();
}
