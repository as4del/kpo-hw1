package ru.hse.ledger.domain;


import java.util.Objects;

public abstract class Vehicle extends Asset {
    private final int ease;
    private final Roadworthiness condition;

    protected Vehicle(String number, String name, int ease, Roadworthiness condition) {
        super(number, name);
        if (ease < 1 || ease > 10) {
            throw new IllegalArgumentException("Простота должна быть от 1 до 10");
        }
        this.ease = ease;
        this.condition = Objects.requireNonNull(condition, "Состояние");
    }

    public final int ease() {
        return ease;
    }

    public final Roadworthiness condition() {
        return condition;
    }
}
