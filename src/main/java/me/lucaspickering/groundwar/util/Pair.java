package me.lucaspickering.groundwar.util;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<T, U> {

    private final U second;
    private final T first;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns a {@link Collector} that will convert a {@link java.util.stream.Stream} of pairs
     * into a {@link Map} of those same pairs.
     * @param <T> the type of the first value in each pair, to become the type of each key
     * @param <U> the type of the second value in each pair, to become the type of each value
     * @return a {@link Collector} that will turn pairs into a {@link Map}
     */
    public static <T, U> Collector<Pair<T, U>, ?, Map<T, U>> mapCollector() {
        return Collectors.toMap(Pair::first, Pair::second);
    }

    public T first() {
        return first;
    }

    public U second() {
        return second;
    }
}
