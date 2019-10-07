package com.example.pid;
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
