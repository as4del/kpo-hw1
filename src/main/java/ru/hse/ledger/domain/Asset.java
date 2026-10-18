package ru.hse.ledger.domain;


import java.util.Objects;

public abstract class Asset implements Inventoried {
    private final String number;
    private final String name;

    protected Asset(String number, String name) {
        this.number = required(number, "Номер");
        this.name = required(name, "Название");
    }

    private static String required(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " не может быть пустым");
        }
        return value.strip();
    }

    @Override
    public final String number() {
        return number;
    }

    @Override
    public final String name() {
        return name;
    }
}
