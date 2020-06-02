package com.alaitp.keyword.websocket.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Builder<T> {
    private final Supplier<T> supplier;

    private List<Consumer<T>> instanceModifiers = new ArrayList<>();

    public Builder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Builder<T> of(Supplier<T> supplier) {
        return new Builder<T>(supplier);
    }

    public <U> Builder<T> with(BiConsumer<T, U> consumer, U value) {
        Consumer<T> c = instance -> consumer.accept(instance, value);
        instanceModifiers.add(c);
        return this;
    }

    public T build() {
        T value = supplier.get();
        instanceModifiers.forEach(modifier -> modifier.accept(value));
        instanceModifiers.clear();
        return value;
    }
}
