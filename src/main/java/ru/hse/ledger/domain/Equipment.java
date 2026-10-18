package ru.hse.ledger.domain;


public abstract class Equipment extends Asset {
    protected Equipment(String number, String name) {
        super(number, name);
    }
}
